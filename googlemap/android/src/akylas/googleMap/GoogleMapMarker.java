package akylas.googlemap;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiMessenger.CommandNoReturn;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.TiDimension;
import org.appcelerator.titanium.proxy.TiViewProxy;

import akylas.map.common.AkylasMarker;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GoogleMapMarker extends AkylasMarker<LatLng> {
    private static final String TAG = "GoogleMapMarker";
    private MarkerOptions markerOptions = null;
    private Marker marker;

    private static final String defaultIconImageWidth = "30dip"; // The height
    private static final String defaultIconImageHeight = "40dip"; // The height
    // of the
    // default
    // marker icon
    // The height of the marker icon in the unit of "px". Will use it to analyze
    // the touch event to find out
    // the correct clicksource for the click event.
    private int iconImageWidth = 0;
    private int iconImageHeight = 0;

    public GoogleMapMarker(final AnnotationProxy p) {
        super(p);
    }

    private void handleCustomView(Object obj) {
        if (obj instanceof TiViewProxy) {
            TiBlob imageBlob = ((TiViewProxy) obj).toImage(null, 1);
            if (imageBlob != null) {
                Bitmap image = ((TiBlob) imageBlob).getImage();
                if (image != null) {
                    markerOptions
                            .icon(BitmapDescriptorFactory.fromBitmap(image));
                    setIconImageWidth(image.getWidth());
                    setIconImageHeight(image.getHeight());
                    return;
                }
            }
        }
        Log.w(TAG, "Unable to get the image from the custom view: " + obj);
    }

    private void setIconImageHeight(int h) {
        if (h >= 0) {
            iconImageHeight = h;
        } else { // default maker icon
            TiDimension dimension = new TiDimension(defaultIconImageHeight,
                    TiDimension.TYPE_UNDEFINED);
            // TiDimension needs a view to grab the window manager, so we'll
            // just use the decorview of the current window
            View view = TiApplication.getAppCurrentActivity().getWindow()
                    .getDecorView();
            iconImageHeight = dimension.getAsPixels(view);
        }
    }
    private void setIconImageWidth(int w) {
        if (w >= 0) {
            iconImageWidth = w;
        } else { // default maker icon
            TiDimension dimension = new TiDimension(defaultIconImageWidth,
                    TiDimension.TYPE_UNDEFINED);
            // TiDimension needs a view to grab the window manager, so we'll
            // just use the decorview of the current window
            View view = TiApplication.getAppCurrentActivity().getWindow()
                    .getDecorView();
            iconImageWidth = dimension.getAsPixels(view);
        }
    }

    public int getIconImageHeight() {
        return  iconImageHeight;
    }
    public int getIconImageWidth() {
        return  iconImageWidth;
    }
    protected void handleSetImage(Bitmap bitmap) {
        if (bitmap == null) {
            setIconImageWidth(-1);
            setIconImageHeight(-1);
            return;
        }
        setIconImageWidth(bitmap.getWidth());
        setIconImageHeight(bitmap.getHeight());
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);
        if (markerOptions != null) {
            markerOptions.icon(icon);
        }
        if (marker != null) {
            marker.setIcon(icon);
            invalidate();
        }
    };

    @Override
    public void setMarkerColor(int color) {
        if (color != Color.TRANSPARENT && marker != null) {
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(hsv[0]));
        }
    }

    public MarkerOptions getMarkerOptions() {
        if (markerOptions == null) {

            markerOptions = new MarkerOptions()
                    .position((LatLng) proxy.getPosition())
                    .rotation(proxy.heading)
                    .zIndex(proxy.zIndex)
//                    .title(proxy.annoTitle)
//                    .snippet(proxy.annoSubtitle)
                    .draggable(proxy.draggable)
                    .flat(proxy.flat);

            markerOptions.alpha(proxy.opacity);
            if (proxy.opacity == 0) {
                markerOptions.visible(false);
            } else {
                markerOptions.visible(proxy.visible);
            }
            // if (bitmap != null) {
            // } else {
            if (getImage() == null) {
                int color = proxy.getCurrentTintColor();
                if (color != Color.TRANSPARENT) {
                    float[] hsv = new float[3];
                    Color.colorToHSV(color, hsv);
                    markerOptions.icon(
                            BitmapDescriptorFactory.defaultMarker(hsv[0]));
                }
            }
            // }

            if (proxy.anchor != null) {
                PointF anchor = proxy.anchor;
                markerOptions.anchor(anchor.x, anchor.y);
            }
            if (proxy.calloutAnchor != null) {
                PointF anchor = proxy.calloutAnchor;
                markerOptions.infoWindowAnchor(anchor.x, anchor.y);
            }
        }

        // KrollDict dict = proxy.getProperties();
        // customView, image and pincolor must be defined before adding to
        // mapview. Once added, their values are final.
        // if (dict.containsKey(AkylasMapModule.PROPERTY_CUSTOM_VIEW)) {
        // handleCustomView(dict.get(AkylasMapModule.PROPERTY_CUSTOM_VIEW));
        // } else
        return markerOptions;
    }

    public void setMarker(Marker m) {
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
            marker.remove();
            marker.setTag(null);
            marker = null;
        }
    }

    @Override
    public double getLatitude() {
        if (marker != null) {
            return marker.getPosition().latitude;
        }
        return 0;
    }

    @Override
    public double getLongitude() {
        if (marker != null) {
            return marker.getPosition().longitude;
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
    public void setPosition(final LatLng point) {
        if (point == null) {
            return;
        }
        if (markerOptions != null) {
            markerOptions.position(point);
        }
        if (marker == null) {
            return;
        }
        final long duration = proxy.animationDuration();
        runInUiThread(new CommandNoReturn() {
            public void execute() {
                if (proxy == null) {
                    return;
                }
                GoogleMapView mapView = (GoogleMapView) proxy.getMapView();
                if (mapView != null) {
                    mapView.updateMarkerPosition(marker, point, duration);
                }
            }
        });

    }

    @Override
    public void setVisible(final boolean visible) {
        if (markerOptions != null) {
            markerOptions.visible(visible);
        }
        if (marker == null) {
            return;
        }
        runInUiThread(new CommandNoReturn() {
            public void execute() {
                marker.setVisible(visible);
            }
        });
    }

    @Override
    public void setHeading(final float heading) {
        if (markerOptions != null) {
            markerOptions.rotation(heading);
        }
        if (marker == null) {
            return;
        }
        runInUiThread(new CommandNoReturn() {
            public void execute() {
                if (proxy == null) {
                    return;
                }
                GoogleMapView mapView = (GoogleMapView) proxy.getMapView();
                if (mapView != null) {
                    mapView.updateMarkerHeading(marker, heading);
                }
            }
        });

    }

    public void setAlpha(final float alpha) {
        if (markerOptions != null) {
            markerOptions.alpha(alpha);
        }
        if (marker == null) {
            return;
        }
        runInUiThread(new CommandNoReturn() {
            public void execute() {
                marker.setAlpha(alpha);
            }
        });
    }

    @Override
    public void setDraggable(final boolean draggable) {
        if (markerOptions != null) {
            markerOptions.draggable(draggable);
        }
        if (marker != null) {
            runInUiThread(new CommandNoReturn() {
                public void execute() {
                    marker.setDraggable(draggable);
                }
            });
        }
    }

    @Override
    public void setFlat(final boolean flat) {
        if (markerOptions != null) {
            markerOptions.flat(flat);
        }
        if (marker != null) {
            runInUiThread(new CommandNoReturn() {
                public void execute() {
                    marker.setFlat(flat);
                }
            });
        }
    }
    
    @Override
    public void setZIndex(final float value) {
        if (markerOptions != null) {
            markerOptions.zIndex(value);
        }
        if (marker != null) {
            runInUiThread(new CommandNoReturn() {
                public void execute() {
                    marker.setZIndex(value);
                }
            });
        }
    }

    @Override
    public void invalidate() {
        if (marker == null) {
            return;
        }
        final boolean oldVisible = marker.isVisible();
        runInUiThread(new CommandNoReturn() {
            public void execute() {
                marker.setVisible(!oldVisible);
                marker.setVisible(oldVisible);
            }
        });
    }

    @Override
    public void setAnchor(PointF anchor) {
        final float anchorX = (anchor != null) ? anchor.x : 0.5f;
        final float anchorY = (anchor != null) ? anchor.y : 1.0f;
        if (markerOptions != null) {
            markerOptions.anchor(anchorX, anchorY);
        }
        if (marker == null) {
            return;
        }
        runInUiThread(new CommandNoReturn() {
            public void execute() {
                marker.setAnchor(anchorX, anchorY);
            }
        });
    }

    @Override
    public void setWindowAnchor(PointF anchor) {
        final float anchorX = (anchor != null) ? anchor.x : 0.5f;
        final float anchorY = (anchor != null) ? anchor.y : 0.0f;
        if (markerOptions != null) {
            markerOptions.infoWindowAnchor(anchorX, anchorY);
        }
        if (marker == null) {
            return;
        }
        runInUiThread(new CommandNoReturn() {
            public void execute() {
                marker.setInfoWindowAnchor(anchorX, anchorY);
            }
        });
    }
}
