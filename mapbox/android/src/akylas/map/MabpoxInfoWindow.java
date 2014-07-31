package akylas.map;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiUIHelper;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.views.InfoWindow;

public class MabpoxInfoWindow extends InfoWindow {
    protected AnnotationProxy proxy;
//    private static int mTitleViewId = 0;
//    private static int mDescriptionViewId = 0;
//    private static int mSubDescriptionViewId = 0;
//    private static int mLeftViewId = 0;
//    private static int mRightViewId = 0;
//    private static int mInfoViewId = 0;
    
//    public static class InfoView extends LinearLayout {
//        TextView mTitleView;
//        TextView mDescriptionView;
//        TextView mSubDescriptionView;
//        TiCompositeLayout mLeftView;
//        TiCompositeLayout mRightView;
//
//        public InfoView(Context context) {
//            super(context);
//            if (mTitleView == null) {
//                setResIds(context);
//            }
//        }
//
//        public InfoView(Context context, AttributeSet attrs) {
//            super(context, attrs);
//            if (mTitleView == null) {
//                setResIds(context);
//            }
//        }
//
//        /**
//         * Given a context, set the resource ids for the layout
//         * of the InfoWindow.
//         * @param context
//         */
//        private void setResIds(Context context) {
//            if (mTitleViewId == 0) {
//                try {
//                    mTitleViewId = TiRHelper.getResource("id.ak_map_tooltip_title");
//                    mDescriptionViewId = TiRHelper.getResource("id.ak_map_tooltip_description");
//                    mSubDescriptionViewId = TiRHelper.getResource("id.ak_map_tooltip_subdescription");
//                    mLeftViewId = TiRHelper.getResource("id.ak_map_tooltip_leftview");
//                    mRightViewId = TiRHelper.getResource("id.ak_map_tooltip_rightview");
//                } catch (ResourceNotFoundException e) {
//                    e.printStackTrace();
//                }
//                
//            }
//            mTitleView = (TextView) findViewById(mTitleViewId);
//            mDescriptionView = (TextView) findViewById(mDescriptionViewId);
//            mSubDescriptionView = (TextView) findViewById(mSubDescriptionViewId);
//            mLeftView = (TiCompositeLayout) findViewById(mLeftViewId);
//            mRightView = (TiCompositeLayout) findViewById(mRightViewId);
//        }
//
//
//        public void setText(final String title) {
//            mTitleView.setText(title);
//        }
//
//        public void setDescription(final String snippet) {
//            mDescriptionView.setText(snippet);
//        }
//
//        public void setSubDescription(final String subDesc) {
//            if ("".equals(subDesc)) {
//                mSubDescriptionView.setVisibility(View.GONE);
//            } else {
//                mSubDescriptionView.setText(subDesc);
//                mSubDescriptionView.setVisibility(View.VISIBLE);
//            }
//        }
//
//        public void updateWithMarker(Marker marker) {
//            setText(marker.getTitle());
//            setDescription(marker.getDescription());
//            setSubDescription(marker.getSubDescription());
//        }
//    }

    public MabpoxInfoWindow(Context context, AnnotationProxy proxy) {
        super(context);
        this.proxy = proxy;
    }
    
    @Override
    protected ViewGroup createContainerView(final Context context) {
        InfoWindowContainerView view = (InfoWindowContainerView) super.createContainerView(context);
        KrollDict dict = proxy.getProperties();
        
        view.setBorderRadius(TiUIHelper.getInPixels(dict, AkylasMapModule.PROPERTY_CALLOUT_BORDER_RADIUS, 4));
        view.setBackgroundColor(dict.optColor(AkylasMapModule.PROPERTY_CALLOUT_BACKGROUND_COLOR, Color.WHITE));
        return view;
    }
    
    @Override
    protected View createInfoView(final Context context) {
        return proxy.getOrCreateMapInfoView();
//        if (mInfoViewId == 0) {
//            try {
//                mInfoViewId = TiRHelper.getResource("layout.ak_map_pin_info");
//            } catch (ResourceNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
//        LayoutInflater inflater =
//                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        return inflater.inflate(mInfoViewId, null);
    }
}
