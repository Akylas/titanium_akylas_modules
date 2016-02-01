package akylas.zoomableimage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiPoint;
import org.appcelerator.titanium.bitmappool.TiBitmapPool;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.transition.TransitionHelper;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.view.MaskableView;
import org.appcelerator.titanium.view.TiDrawableReference;
import org.appcelerator.titanium.view.TiUINonViewGroupView;
import org.appcelerator.titanium.view.TiUIView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.OnImageEventListener;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.OnScaleChangeListener;
import com.davemorrissey.labs.subscaleview.decoder.DecoderFactory;
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder;
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;

public class ZoomableImageView extends TiUINonViewGroupView
        implements OnImageEventListener, OnScaleChangeListener {
    private static final String TAG = "TiUIZoomableImageView";

    // private TiDrawableReference loadingRef = null;
    private TiDrawableReference currentRef = null;

    // private boolean localLoadSync = false;
    private boolean onlyTransitionIfRemote = false;

    protected static final int TIFLAG_NEEDS_ZOOM = 0x00000001;

    private TiDrawableReference imageSource;
    private TiDrawableReference defaultImageSource;

    private HashMap transitionDict = null;

    private int orientation = 0;
    private int wantedScaleType = SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE;
    private int zoomStyle = SubsamplingScaleImageView.ZOOM_FOCUS_CENTER;
    private boolean quickScaleEnabled = true;
    private boolean zoomEnabled = true;
    private boolean panEnabled = true;
    private float currentScale = -1;
    private float maxScale = 2F;
    private float minScale = -1;
    private boolean parallelLoadingEnabled = true;

    public class TiImageDecoder implements ImageDecoder {
        private final TiDrawableReference ref;
        private Picasso picasso;

        public TiImageDecoder(TiDrawableReference ref) {
            this.ref = ref;
            this.picasso = TiApplication.getPicassoInstance();
        }

        @Override
        public Bitmap decode(Context context, Uri uri) throws Exception {
            return ref.getBitmap();
        }

        @Override
        public boolean isCached(Context context, Uri source) {
            return true;
        }
    }

    public class TiImageRegionDecoder implements ImageRegionDecoder {
        private final TiDrawableReference ref;
        private final OkHttpClient client;
        private BitmapRegionDecoder decoder;
        private final Object decoderLock = new Object();

        public TiImageRegionDecoder(TiDrawableReference ref) {
            this.ref = ref;
            this.client = TiApplication.getPicassoHttpClient(TiConvert
                    .toHashMap(proxy.getProperty(TiC.PROPERTY_HTTP_OPTIONS)));
        }

        @Override
        public Point init(Context context, Uri uri) throws Exception {
            InputStream inputStream = null;
            if (ref.isNetworkUrl()) {
                OkHttpDownloader downloader = new OkHttpDownloader(client);
                inputStream = downloader.load(Uri.parse(ref.getUrl()), 0)
                        .getInputStream();
            } else {
                inputStream = ref.getInputStream();
            }

            this.decoder = BitmapRegionDecoder.newInstance(inputStream, false);

            return new Point(this.decoder.getWidth(), this.decoder.getHeight());
        }

        @Override
        public Bitmap decodeRegion(Rect rect, int sampleSize) {
            synchronized (this.decoderLock) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = sampleSize;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                Bitmap bitmap = this.decoder.decodeRegion(rect, options);
                if (bitmap == null) {
                    throw new RuntimeException(
                            "Region decoder returned null bitmap - image format may not be supported");
                } else {
                    return bitmap;
                }
            }
        }

        @Override
        public boolean isReady() {
            return this.decoder != null && !this.decoder.isRecycled();
        }

        @Override
        public void recycle() {
            this.decoder.recycle();
        }
    }

    private final DecoderFactory mBitmapDecoderFactory = new DecoderFactory<TiImageDecoder>() {

        @Override
        public TiImageDecoder make()
                throws IllegalAccessException, InstantiationException {

            return new TiImageDecoder(currentRef);
        }
    };

    private final DecoderFactory mRegionDecoderFactory = new DecoderFactory<TiImageRegionDecoder>() {

        @Override
        public TiImageRegionDecoder make()
                throws IllegalAccessException, InstantiationException {

            return new TiImageRegionDecoder(currentRef);
        }
    };

    public class TiZoomableImageView extends MaskableView
            implements OnClickListener {
        private static final String TAG = "TiImageView";

        private OnClickListener clickListener;

        private InternalImageView imageView;

        private TiDrawableReference queuedSourced = null;
        private HashMap queuedTransition = null;
        private AnimatorSet currentTransitionSet = null;

        // Flags to help determine whether width/height is defined, so we can
        // scale appropriately
        private boolean viewWidthDefined;
        private boolean viewHeightDefined;

        private InternalImageView oldImageView = null;

        private class InternalImageView extends SubsamplingScaleImageView {
            public InternalImageView(Context context) {
                super(context);
            }
        }

        public TiZoomableImageView(Context context) {
            super(context);

            imageView = cloneImageView();
            addView(imageView, getImageLayoutParams());

            super.setOnClickListener(this);
        }

        private InternalImageView cloneImageView() {
            InternalImageView newImageView = new InternalImageView(
                    getContext());
            newImageView.setBitmapDecoderFactory(mBitmapDecoderFactory);
            newImageView.setRegionDecoderFactory(mRegionDecoderFactory);
            newImageView.setOnTouchListener(ZoomableImageView.this);
            newImageView.setOnImageEventListener(ZoomableImageView.this);
            newImageView.setOnScaleChangeListener(ZoomableImageView.this);

            newImageView.setParallelLoadingEnabled(parallelLoadingEnabled);
            newImageView.setZoomEnabled(zoomEnabled);
            newImageView.setPanEnabled(panEnabled);
            newImageView.setQuickScaleEnabled(quickScaleEnabled);
            newImageView.setMaxScale(maxScale);
            newImageView.setMinScale(
                    minScale == -1 ? newImageView.getMinScale() : minScale);
            newImageView.setOrientation(orientation);
            newImageView.setMinimumScaleType(zoomStyle);
            newImageView.setMinimumScaleType(wantedScaleType);
            return newImageView;
        }

        /**
         * Constructs a new TiImageView object.
         * 
         * @param context
         *            the associated context.
         * @param proxy
         *            the associated proxy.
         */
        public TiZoomableImageView(@NonNull Context context, TiUIView tiView) {
            this(context);
        }

        public Bitmap getImageBitmap() {
            return imageView.getCurrentBitmap();
        }

        /**
         * Sets a Bitmap as the content of imageView
         * 
         * @param bitmap
         *            The bitmap to set. If it is null, it will clear the
         *            previous image.
         */
        public void setImageSource(TiDrawableReference ref) {
            Bitmap bitmap = imageView.getCurrentBitmap();
            if (bitmap != null) {
                TiBitmapPool.decrementRefCount(bitmap);
            }
            if (ref != null) {
                imageView.setImage(ImageSource.uri(ref.getUrl()));
            } else {
                imageView.setImage(null);

            }
        }

        /**
         * Sets a Bitmap as the content of imageView
         * 
         * @param bitmap
         *            The bitmap to set. If it is null, it will clear the
         *            previous image.
         */
        public void setImageSourceWithTransition(TiDrawableReference ref,
                HashMap transition) {
            if (transition == null) {
                setImageSource(ref);
            } else {
                if (currentTransitionSet != null) {
                    queuedTransition = transition;
                    queuedSourced = ref;
                    return;
                }
                InternalImageView newImageView = cloneImageView();
                newImageView.setImage(ImageSource.uri(ref.getUrl()));
                transitionToImageView(newImageView, transition);
            }
        }

        private void onTransitionEnd() {
            currentTransitionSet = null;
            if (queuedSourced != null) {
                setImageSourceWithTransition(queuedSourced, queuedTransition);
                queuedTransition = null;
                queuedSourced = null;
            }
        }

        private ViewGroup.LayoutParams getImageLayoutParams() {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            return params;
        }

        /**
         * Sets a Bitmap as the content of imageView
         * 
         * @param bitmap
         *            The bitmap to set. If it is null, it will clear the
         *            previous image.
         */
        public void transitionToImageView(InternalImageView newImageView,
                HashMap transition) {
            oldImageView = imageView;
            imageView = newImageView;
            oldImageView.setOnImageEventListener(null);
            oldImageView.setClickable(false);
            oldImageView.setOnTouchListener(null);// we need to remove it or we
                                                  // will receive a cancel event
                                                  // on remove view

            TransitionHelper.CompletionBlock onDone = new TransitionHelper.CompletionBlock() {

                @Override
                public void transitionDidFinish(boolean success) {
                    if (oldImageView != null) {
                        if (oldImageView.getCurrentBitmap() != null) {
                            TiBitmapPool.decrementRefCount(
                                    oldImageView.getCurrentBitmap());
                        }
                        oldImageView.recycle();
                        oldImageView = null;
                    }
                    onTransitionEnd();
                }
            };
            Animator anim = null;
            currentTransitionSet = TransitionHelper.transitionViews(this,
                    newImageView, oldImageView, onDone, transition,
                    (oldImageView != null) ? oldImageView.getLayoutParams()
                            : getImageLayoutParams(),
                    anim);
        }

        public void cancelCurrentTransition() {
            // if (currentTransitionSet != null)
            // {
            // currentTransitionSet.cancel();
            queuedTransition = null;
            queuedSourced = null;
            currentTransitionSet = null;
            // }
        }

        public void setOnClickListener(OnClickListener clickListener) {
            this.clickListener = clickListener;
        }

        public void onClick(View view) {
            boolean sendClick = true;
            if (sendClick && clickListener != null) {
                clickListener.onClick(view);
            }
        }

        private float getImageRatio() {
            float ratio = 0;
            Bitmap bitmap = getImageBitmap();
            if (bitmap != null && bitmap.getHeight() > 0) {
                return (float) bitmap.getWidth() / (float) bitmap.getHeight();
            }

            return ratio;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            int maxWidth = 0;
            int maxHeight = 0;

            int measuredWidth = getMeasuredWidth();
            int measuredHeight = getMeasuredHeight();
            maxWidth = Math.max(maxWidth, measuredWidth);
            maxHeight = Math.max(maxHeight, measuredHeight);
            if (getImageBitmap() != null) {
                int w = MeasureSpec.getSize(widthMeasureSpec);
                int wm = MeasureSpec.getMode(widthMeasureSpec);
                int h = MeasureSpec.getSize(heightMeasureSpec);
                int hm = MeasureSpec.getMode(heightMeasureSpec);

                measureChild(imageView, widthMeasureSpec, heightMeasureSpec);
                measuredWidth = imageView.getMeasuredWidth();
                measuredHeight = imageView.getMeasuredHeight();
                if (measuredWidth > 0 && measuredHeight > 0) {
                    if (hm == MeasureSpec.EXACTLY && (wm == MeasureSpec.AT_MOST
                            || wm == MeasureSpec.UNSPECIFIED)) {
                        maxHeight = Math.max(h,
                                Math.max(maxHeight, measuredHeight));
                        float ratio = getImageRatio();
                        maxWidth = (int) Math.floor(maxHeight * ratio);
                    } else if (wm == MeasureSpec.EXACTLY
                            && (hm == MeasureSpec.AT_MOST
                                    || hm == MeasureSpec.UNSPECIFIED)) {
                        maxWidth = Math.max(w,
                                Math.max(maxWidth, measuredWidth));
                        float ratio = getImageRatio();
                        if (ratio > 0)
                            maxHeight = (int) Math.floor(maxWidth / ratio);
                    } else {
                        maxWidth = Math.max(maxWidth, measuredWidth);
                        maxHeight = Math.max(maxHeight, measuredHeight);
                    }
                }
            }
            setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec),
                    resolveSize(maxHeight, heightMeasureSpec));
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right,
                int bottom) {

            int parentLeft = 0;
            int parentRight = right - left;
            int parentTop = 0;
            int parentBottom = bottom - top;

            imageView.layout(parentLeft, parentTop, parentRight, parentBottom);
            if (oldImageView != null) {
                // make sure the old image view remains in place
                int centerX = (parentRight - parentLeft) / 2;
                int centerY = (parentBottom - parentTop) / 2;
                int w = oldImageView.getWidth() / 2;
                int h = oldImageView.getHeight() / 2;
                oldImageView.layout(centerX - w, centerY - h, centerX + w,
                        centerY + h);
            }
        }

        public void setWidthDefined(boolean defined) {
            viewWidthDefined = defined;
        }

        public boolean getWidthDefined() {
            return viewWidthDefined;
        }

        public void setHeightDefined(boolean defined) {
            viewHeightDefined = defined;
        }

        public boolean getHeightDefined() {
            return viewHeightDefined;
        }

        public void release() {
            if (imageView != null) {
                imageView.recycle();
            }
        }
    }

    private static final ArrayList<String> KEY_SEQUENCE;
    static {
        ArrayList<String> tmp = new ArrayList<String>();
        tmp.add(TiC.PROPERTY_FILTER_OPTIONS);
        KEY_SEQUENCE = tmp;
    }

    @Override
    protected ArrayList<String> keySequence() {
        return KEY_SEQUENCE;
    }

    public ZoomableImageView(final TiViewProxy proxy) {
        super(proxy);
        // imageViewProxy = (ImageViewProxy) proxy;

        TiZoomableImageView view = new TiZoomableImageView(proxy.getActivity(),
                this) {
            @Override
            protected void onLayout(boolean changed, int left, int top,
                    int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                if (changed) {
                    if (changed) {
                        TiUIHelper.firePostLayoutEvent(ZoomableImageView.this);
                    }
                }
            }

            @Override
            public boolean dispatchTouchEvent(MotionEvent event) {
                if (ZoomableImageView.this.touchPassThrough(childrenHolder,
                        event))
                    return false;
                if (touchPassThrough == true)
                    return false;
                return super.dispatchTouchEvent(event);
            }

            @Override
            public void dispatchSetPressed(boolean pressed) {
                if (propagateSetPressed(this, pressed)) {
                    super.dispatchSetPressed(pressed);
                }
            }
        };

        setNativeView(view);
    }

    @Override
    public void setReusing(boolean value) {
        super.setReusing(value);
        if (value) {
            TiZoomableImageView view = getView();
            if (view != null)
                view.cancelCurrentTransition();
        }
    }

    // @Override
    // public void setProxy(TiViewProxy proxy) {
    // super.setProxy(proxy);
    // imageViewProxy = (ImageViewProxy) proxy;
    // }

    private TiZoomableImageView getView() {
        return (TiZoomableImageView) nativeView;
    }

    private SubsamplingScaleImageView getScaleImageView() {
        return getView().imageView;
    }

    private Bitmap getBitmap() {
        TiZoomableImageView view = getView();
        if (view != null)
            return view.getImageBitmap();
        else
            return null;
    }

    private void handleSetImageSource(final TiDrawableReference ref,
            final boolean shouldTransition) {
        TiZoomableImageView view = getView();
        if (view != null) {
            view.setImageSourceWithTransition(ref,
                    shouldTransition ? transitionDict : null);
            boolean widthDefined = view.getWidthDefined();
            boolean heightDefined = view.getHeightDefined();
            if ((!widthDefined || !heightDefined)) {
                // force re-calculating the layout dimension and the redraw of
                // the view
                // This is a trick to prevent getMeasuredWidth and Height to be
                // 0
                view.measure(
                        MeasureSpec.makeMeasureSpec(0,
                                widthDefined ? MeasureSpec.EXACTLY
                                        : MeasureSpec.UNSPECIFIED),
                        MeasureSpec.makeMeasureSpec(0,
                                heightDefined ? MeasureSpec.EXACTLY
                                        : MeasureSpec.UNSPECIFIED));
                view.requestLayout();
            }
        }
    }

    public boolean fireImageEvent(String eventName, KrollDict data) {
        return fireEvent(eventName, data, false, false);
    }

    private void fireLoad(String state, Bitmap bitmap) {
        if (hasListeners(TiC.EVENT_LOAD)) {
            KrollDict data = new KrollDict();
            if (bitmap != null) {
                data.put("image", TiBlob.blobFromObject(bitmap));
            }
            data.put(TiC.EVENT_PROPERTY_STATE, state);
            fireImageEvent(TiC.EVENT_LOAD, data);
        }
    }

    private void fireError(String message, String imageUrl) {
        if (hasListeners(TiC.EVENT_ERROR)) {
            KrollDict data = new KrollDict();

            data.putCodeAndMessage(TiC.ERROR_CODE_UNKNOWN, message);
            if (imageUrl != null) {
                data.put(TiC.PROPERTY_IMAGE, imageUrl);
            }
            fireImageEvent(TiC.EVENT_ERROR, data);
        }
    }

    private void setImageSource(Object object) {
        if (object instanceof TiDrawableReference) {
            imageSource = (TiDrawableReference) object;
        } else {
            imageSource = makeImageSource(object);
        }
        if (imageSource != null && proxy.hasListeners("startload", false)) {
            KrollDict data = new KrollDict();
            data.put(TiC.PROPERTY_IMAGE, imageSource.getUrl());
            proxy.fireEvent("startload", data, false, false);
        }
    }

    private TiDrawableReference makeImageSource(Object object) {
        TiDrawableReference source = TiDrawableReference.fromObject(proxy,
                object);
        // Check for orientation and decodeRetries only if an image is
        // specified
        boolean autoRotate = TiConvert
                .toBoolean(proxy.getProperty(TiC.PROPERTY_AUTOROTATE), false);
        if (autoRotate) {
            this.orientation = source.getOrientation();
            getScaleImageView().setOrientation(source.getOrientation());
        }
        int decodeRetries = TiConvert.toInt(
                proxy.getProperty(TiC.PROPERTY_DECODE_RETRIES),
                TiDrawableReference.DEFAULT_DECODE_RETRIES);
        source.setDecodeRetries(decodeRetries);
        source.httpOptions = TiConvert.toHashMap(proxy.getProperty(TiC.PROPERTY_HTTP_OPTIONS));

        return source;
    }

    private void setImageInternal() {

        if (imageSource == null || imageSource.isTypeNull()) {
            // here we can transition to the default image
            setDefaultImage(proxy.viewInitialised());
            currentRef = null;
            return;
        }

        if (reusing) {
            setDefaultImage(false);
        }

        currentRef = imageSource;
        handleSetImageSource(imageSource,
                !onlyTransitionIfRemote || !currentRef.isNetworkUrl());
        // TiImageHelper.downloadDrawable(imageViewProxy, imageref,
        // localLoadSync, this);
    }

    private void setDefaultImage(final boolean withTransition) {
        TiZoomableImageView view = getView();

        if (view == null) {
            return;
        }
        handleSetImageSource(defaultImageSource, withTransition);
    }

    @Override
    protected void didProcessProperties() {
        if ((mProcessUpdateFlags & TIFLAG_NEEDS_ZOOM) != 0) {
            mProcessUpdateFlags &= ~TIFLAG_NEEDS_ZOOM;
            if (currentScale != -1) {
                getScaleImageView().setScaleAndCenter(currentScale,
                        new PointF(0, 0));
            }
        }

        if ((mProcessUpdateFlags & TIFLAG_NEEDS_LAYOUT) != 0) {
            TiZoomableImageView view = getView();
            if (view != null) {
                view.setWidthDefined(!(layoutParams.autoSizeWidth()
                        && (layoutParams.optionLeft == null
                                || layoutParams.optionRight == null)));
                view.setHeightDefined(!(layoutParams.autoSizeHeight()
                        && (layoutParams.optionTop == null
                                || layoutParams.optionBottom == null)));
            }
        }
        if (imageSource == null) {
            setDefaultImage(false);
        }
        super.didProcessProperties();
    }

    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        // case TiC.PROPERTY_LOCAL_LOAD_SYNC:
        // localLoadSync = TiConvert.toBoolean(newValue);
        // break;
        case TiC.PROPERTY_SCALE_TYPE:
            setWantedScaleType(TiConvert.toInt(newValue));
            break;
        case TiC.PROPERTY_IMAGE_MASK:
            setImageMask(newValue);
            break;
        case TiC.PROPERTY_ONLY_TRANSITION_IF_REMOTE:
            onlyTransitionIfRemote = TiConvert.toBoolean(newValue);
            break;
        case TiC.PROPERTY_DEFAULT_IMAGE:
            defaultImageSource = makeImageSource(newValue);
            break;
        case TiC.PROPERTY_IMAGE:
            boolean changeImage = true;
            TiDrawableReference source = makeImageSource(newValue);
            if (imageSource != null) {
                if (imageSource.equals(source)) {
                    changeImage = false;
                }
            }
            if (changeImage) {
                setImageSource(source);
                setImageInternal();
            }
            break;
        case TiC.PROPERTY_TRANSITION:
            if (newValue instanceof HashMap) {
                transitionDict = (HashMap) newValue;
            } else {
                transitionDict = null;
            }
            break;
        case "minZoomScale": {
            minScale = TiConvert.toFloat(newValue, 1.0f);
            SubsamplingScaleImageView view = getScaleImageView();
            if (view != null) {
                view.setMinScale(maxScale);
            }
            mProcessUpdateFlags |= TIFLAG_NEEDS_ZOOM;
            break;
        }
        case "maxZoomScale": {
            maxScale = TiConvert.toFloat(newValue, 1.0f);
            SubsamplingScaleImageView view = getScaleImageView();
            if (view != null) {
                view.setMaxScale(maxScale);
            }
            mProcessUpdateFlags |= TIFLAG_NEEDS_ZOOM;
            break;
        }
        case "zoomScale": {
            currentScale = TiConvert.toFloat(newValue, 1.0f);

            mProcessUpdateFlags |= TIFLAG_NEEDS_ZOOM;
            break;
        }
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }

    private void setImageMask(Object mask) {
        TiZoomableImageView view = getView();
        if (view == null)
            return;

        boolean tileImage = proxy.getProperties()
                .optBoolean(TiC.PROPERTY_BACKGROUND_REPEAT, false);
        view.setMask(TiUIHelper.buildImageDrawable(nativeView.getContext(),
                mask, tileImage, proxy));
    }

    public TiBlob toBlob() {
        if (currentRef == null) {
            return null;
        }
        TiZoomableImageView view = getView();
        if (view != null) {
            Bitmap bitmap = view.getImageBitmap();
            if (bitmap != null) {
                return TiBlob.blobFromObject(bitmap, null,
                        currentRef.getCacheKey());
            }
        }
        return TiBlob.blobFromObject(currentRef.getBitmap(), null,
                currentRef.getCacheKey());
    }

    @Override
    public void release() {
        Bitmap bitmap = getBitmap();
        if (bitmap != null) {
            TiBitmapPool.decrementRefCount(bitmap);
        }
        TiZoomableImageView view = getView();
        if (view != null) {
            view.release();
        }
        if (currentRef != null) {
            TiApplication.getPicassoInstance()
                    .cancelTag(currentRef.getCacheKey());
        }
        super.release();
        defaultImageSource = null;
    }

    private void setWantedScaleType(int type) {
        switch (type) {
        case 2:
            wantedScaleType = SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP;
            break;
        case 3:
            wantedScaleType = SubsamplingScaleImageView.SCALE_TYPE_CUSTOM;
            break;
        case 1:
        default:
            wantedScaleType = SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE;
            break;
        }
        SubsamplingScaleImageView view = getScaleImageView();
        if (view != null) {
            view.setMinimumScaleType(wantedScaleType);
        }
    }

    public void resetScaleAndCenter() {
        SubsamplingScaleImageView view = getScaleImageView();
        if (view != null) {
            view.resetScaleAndCenter();
        }
    }

    public float getZoomScale() {
        SubsamplingScaleImageView view = getScaleImageView();
        if (view != null) {
            return view.getScale();
        }
        return 1.0f;

    }

    public float getMinZoomScale() {

        SubsamplingScaleImageView view = getScaleImageView();
        if (view != null) {
            return view.getMinScale();
        }
        return minScale;
    }

    public float getMaxZoomScale() {

        SubsamplingScaleImageView view = getScaleImageView();
        if (view != null) {
            return view.getMaxScale();
        }
        return maxScale;
    }

    public void setZoomScale(float scale, TiPoint point, Boolean animated) {
        SubsamplingScaleImageView view = getScaleImageView();
        if (view == null) {
            return;
        }
        PointF p = null;
        if (point != null) {
            final int w = nativeView.getMeasuredWidth();
            final int h = nativeView.getMeasuredHeight();
            p = point.computeFloat(w, h);
            p = view.viewToSourceCoord(p, p);
        } else {
            p = new PointF(0, 0);
        }
        if (animated) {
            view.animateScaleAndCenter(scale, p);
        } else {

        }
    }

    @Override
    public void onReady() {
        Bitmap bitmap = getBitmap();
        if (bitmap != null) {
            TiBitmapPool.incrementRefCount(bitmap);
        }
        fireLoad(TiC.PROPERTY_IMAGE, bitmap);
    }

    @Override
    public void onImageLoaded() {
    }

    @Override
    public void onPreviewLoadError(Exception e) {
    }

    @Override
    public void onImageLoadError(Exception e) {
        fireError(e.getLocalizedMessage(), currentRef.getUrl());

    }

    @Override
    public void onTileLoadError(Exception e) {
    }

    @Override
    public void onScaleChange(final float scale, final boolean animating, final boolean userInteraction) {
        currentScale = scale;
        if (!animating) {
            if (proxy.hasListeners("scale", false)) {
                KrollDict data = new KrollDict();
                data.put("zoomScale", scale);
                data.put("userInteraction", userInteraction);
                proxy.fireEvent("scale", data, false, false);
            }
        }
    }

}
