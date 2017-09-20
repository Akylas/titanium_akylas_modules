package akylas.carto;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiMessenger.CommandNoReturn;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.TiDimension;
import org.appcelerator.titanium.proxy.TiViewProxy;

import akylas.carto.AnnotationProxy.AkMarker;
import akylas.map.common.AkylasMarker;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import com.carto.core.MapPos;
import com.carto.styles.BillboardOrientation;
import com.carto.styles.MarkerStyle;
import com.carto.styles.MarkerStyleBuilder;
import com.carto.styles.PointStyleBuilder;
import com.carto.vectorelements.Marker;
import com.carto.vectorelements.Point;
import com.carto.vectorelements.VectorElement;
import com.carto.utils.BitmapUtils;

public class CartoMarker extends AkylasMarker<MapPos> {
    private static final String TAG = "GoogleMapMarker";
    private MarkerStyleBuilder markerOptions = null;
    private MarkerStyle markerStyle = null;
    private AkMarker marker;

    private static final String defaultIconSize = "30dip"; // The height
//    private static final String defaultIconImageHeight = "40dip"; // The height
    // of the
    // default
    // marker icon
    // The height of the marker icon in the unit of "px". Will use it to analyze
    // the touch event to find out
    // the correct clicksource for the click event.
//    private int iconImageWidth = 0;
//  private int iconImageHeight = 0;
  private int iconSize = 0;

    public CartoMarker(final AnnotationProxy p) {
        super(p);
    }

    private void handleCustomView(Object obj) {
        if (obj instanceof TiViewProxy) {
            TiBlob imageBlob = ((TiViewProxy) obj).toImage(null, 1);
            if (imageBlob != null) {
                Bitmap image = ((TiBlob) imageBlob).getImage();
                if (image != null) {
                    markerOptions.setBitmap(BitmapUtils.createBitmapFromAndroidBitmap(image));
//                    setIconImageWidth(image.getWidth());
//                    setIconImageHeight(image.getHeight());
                    return;
                }
            }
        }
        Log.w(TAG, "Unable to get the image from the custom view: " + obj);
    }

//    private void setIconImageHeight(int h) {
//        if (h >= 0) {
//            iconImageHeight = h;
//        } else { // default maker icon
//            TiDimension dimension = new TiDimension(defaultIconImageHeight,
//                    TiDimension.TYPE_UNDEFINED);
//            // TiDimension needs a view to grab the window manager, so we'll
//            // just use the decorview of the current window
//            View view = TiApplication.getAppCurrentActivity().getWindow()
//                    .getDecorView();
//            iconImageHeight = dimension.getAsPixels(view);
//        }
//    }
    private void setIconSize(int w) {
        if (w >= 0) {
            iconSize = w;
        } else { // default maker icon
            TiDimension dimension = new TiDimension(defaultIconSize,
                    TiDimension.TYPE_UNDEFINED);
            // TiDimension needs a view to grab the window manager, so we'll
            // just use the decorview of the current window
            View view = TiApplication.getAppCurrentActivity().getWindow()
                    .getDecorView();
            iconSize = dimension.getAsPixels(view);
        }
        
        if (markerOptions != null) {
            markerOptions.setSize(iconSize);
        }
        if (marker != null) {
            invalidate();

        }
    }

//    public int getIconImageHeight() {
//        return  iconImageHeight;
//    }
    public int getIconSize() {
        return  iconSize;
    }
    protected void handleSetImage(Bitmap bitmap) {
//        if (bitmap == null) {
//            setIconImageWidth(-1);
//            setIconImageHeight(-1);
//            return;
//        }
//        setIconImageWidth(bitmap.getWidth());
//        setIconImageHeight(bitmap.getHeight());
        setIconSize(Math.max(bitmap.getWidth(), bitmap.getHeight()));
        com.carto.graphics.Bitmap icon = BitmapUtils.createBitmapFromAndroidBitmap(bitmap);
        if (markerOptions != null) {
//            markerOptions.setBitmap(icon);
        }
        if (marker != null) {
//            marker.(icon);
            invalidate();
        }
    };

    @Override
    public void setMarkerColor(int color) {
        if (markerOptions != null) {
            markerOptions.setColor(new com.carto.graphics.Color(color));
        }
        if (marker != null) {
            invalidate();

        }
    }

    public MarkerStyleBuilder getMarkerOptions() {
        if (markerOptions == null) {

            markerOptions = new MarkerStyleBuilder();
//                    .position((LatLng) proxy.getPosition())
//                    .rotation(proxy.heading)
//                    .zIndex(proxy.zIndex)
////                    .title(proxy.annoTitle)
////                    .snippet(proxy.annoSubtitle)
//                    .draggable(proxy.draggable)
//                    .flat(proxy.flat);

//            markerOptions.alpha(proxy.opacity);
//            if (proxy.opacity == 0) {
//                markerOptions.visible(false);
//            } else {
//                markerOptions.visible(proxy.visible);
//            }
//             if (bitmap != null) {
//             } else {
            markerOptions.setSize(30);
            markerOptions.setColor(new com.carto.graphics.Color(proxy.getCurrentTintColor()));
            if (getImage() == null) {
                int color = proxy.getCurrentTintColor();
                if (color != Color.TRANSPARENT) {
//                    float[] hsv = new float[3];
//                    Color.colorToHSV(color, hsv);
                    markerOptions.setColor(new com.carto.graphics.Color(color));
                }
//            }
             }

            if (proxy.anchor != null) {
                PointF anchor = proxy.anchor;
                markerOptions.setAnchorPoint(anchor.x, anchor.y);
            }
//            if (proxy.calloutAnchor != null) {
//                PointF anchor = proxy.calloutAnchor;
//                markerOptions.infoWindowAnchor(anchor.x, anchor.y);
//            }
        }

        // KrollDict dict = proxy.getProperties();
        // customView, image and pincolor must be defined before adding to
        // mapview. Once added, their values are final.
        // if (dict.containsKey(AkylasMapModule.PROPERTY_CUSTOM_VIEW)) {
        // handleCustomView(dict.get(AkylasMapModule.PROPERTY_CUSTOM_VIEW));
        // } else
        return markerOptions;
    }
    public MarkerStyle getMarkerBuildStyle() {
        if (markerStyle == null) {
            return getMarkerOptions().buildStyle();
        }
        return markerStyle;
    }

    public void setMarker(AkMarker m) {
        marker = m;
    }

    public Marker getMarker() {
        return marker;
    }

    public AnnotationProxy getProxy() {
        return (AnnotationProxy) proxy;
    }

    @Override
    public void removeFromMap() {
        if (marker != null) {
//            marker.remove();
//            marker.setTag(null);
            marker = null;
        }
    }

    @Override
    public double getLatitude() {
        if (marker != null) {
            return marker.getBounds().getCenter().getX();
        }
        return 0;
    }

    @Override
    public double getLongitude() {
        if (marker != null) {
            return marker.getBounds().getCenter().getY();
        }
        return 0;
    }

    @Override
    public double getAltitude() {
        return 0;
    }

//    public void showInfoWindow() {
//        if (marker == null || !proxy.canShowInfoWindow()) {
//            return;
//        }
//        runInUiThread(new CommandNoReturn() {
//            public void execute() {
//                if (marker != null) {
//                    marker.showInfoWindow();
//                }
//            }
//        });
//    }
//
//    public void hideInfoWindow() {
//        if (marker == null || !proxy.canShowInfoWindow()) {
//            return;
//        }
//        runInUiThread(new CommandNoReturn() {
//            public void execute() {
//                if (marker != null) {
//                    marker.hideInfoWindow();
//                    if (proxy != null) {
//                        proxy.infoWindowDidClose();
//                    }
//                }
//            }
//        });
//    }

    public void runInUiThread(final CommandNoReturn command) {
        if (marker != null) {
            super.runInUiThread(command);
        }
    }

    @Override
    public void setPosition(final MapPos point) {
        if (point == null) {
            return;
        }
//        if (markerOptions != null) {
//            markerOptions.setPos(point);
//        }
        if (marker == null) {
            return;
        }
        final long duration = proxy.animationDuration();
        runInUiThread(new CommandNoReturn() {
            public void execute() {
                CartoView mapView = (CartoView) proxy.getMapView();
                if (mapView != null) {
                    mapView.updateMarkerPosition(marker, point, duration);
                }
            }
        });

    }

    @Override
    public void setVisible(final boolean visible) {
//        if (markerOptions != null) {
//            markerOptions.visible(visible);
//        }
//        if (marker == null) {
//            return;
//        }
//        runInUiThread(new CommandNoReturn() {
//            public void execute() {
//                marker.setVisible(visible);
//            }
//        });
    }

    @Override
    public void setHeading(final float heading) {
//        if (markerOptions != null) {
//            markerOptions.set(heading);
//        }
        if (marker == null) {
            return;
        }
        runInUiThread(new CommandNoReturn() {
            public void execute() {
                CartoView mapView = (CartoView) proxy.getMapView();
                if (mapView != null) {
                    mapView.updateMarkerHeading(marker, heading);
                }
            }
        });

    }

    public void setAlpha(final float alpha) {
//        if (markerOptions != null) {
//            markerOptions.alpha(alpha);
//        }
//        if (marker == null) {
//            return;
//        }
//        runInUiThread(new CommandNoReturn() {
//            public void execute() {
//                marker.setAlpha(alpha);
//            }
//        });
    }

    @Override
    public void setDraggable(final boolean draggable) {
//        if (markerOptions != null) {
//            markerOptions.draggable(draggable);
//        }
//        if (marker != null) {
//            runInUiThread(new CommandNoReturn() {
//                public void execute() {
//                    marker.setDraggable(draggable);
//                }
//            });
//        }
    }

    @Override
    public void setFlat(final boolean flat) {
        BillboardOrientation orientation = flat?BillboardOrientation.BILLBOARD_ORIENTATION_FACE_CAMERA:BillboardOrientation.BILLBOARD_ORIENTATION_GROUND;
        if (markerOptions != null) {
            markerOptions.setOrientationMode(orientation);
        }
        if (marker != null) {
           invalidate();
        }
    }
    
    @Override
    public void setZIndex(final float value) {
//        if (markerOptions != null) {
//            markerOptions.zIndex(value);
//        }
//        if (marker != null) {
//            runInUiThread(new CommandNoReturn() {
//                public void execute() {
//                    marker.setZIndex(value);
//                }
//            });
//        }
    }

    @Override
    public void invalidate() {
        if (marker == null) {
            return;
        }
        markerStyle = null;
        marker.setStyle(getMarkerBuildStyle());
        
    }

    @Override
    public void setAnchor(PointF anchor) {
        final float anchorX = (anchor != null) ? anchor.x : 0.5f;
        final float anchorY = (anchor != null) ? anchor.y : 1.0f;
        if (markerOptions != null) {
            markerOptions.setAnchorPoint(anchorX, anchorY);
        }
        if (marker == null) {
            return;
        }
        invalidate();
    }

    @Override
    public void setWindowAnchor(PointF anchor) {
//        final float anchorX = (anchor != null) ? anchor.x : 0.5f;
//        final float anchorY = (anchor != null) ? anchor.y : 0.0f;
//        if (markerOptions != null) {
//            markerOptions.infoWindowAnchor(anchorX, anchorY);
//        }
//        if (marker == null) {
//            return;
//        }
//        runInUiThread(new CommandNoReturn() {
//            public void execute() {
//                marker.setInfoWindowAnchor(anchorX, anchorY);
//            }
//        });
    }
}
