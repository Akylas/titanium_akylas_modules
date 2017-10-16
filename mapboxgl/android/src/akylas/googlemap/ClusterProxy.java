package akylas.googlemap;

import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.TiMessenger.CommandNoReturn;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.util.TiUIHelper.FontDesc;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;

import akylas.map.common.BaseClusterProxy;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

@Kroll.proxy(creatableInModule = AkylasGooglemapModule.class, propertyAccessors = {
        "maxDistance",
        "minDistance"
    })
public class ClusterProxy extends BaseClusterProxy<LatLng, LatLngBounds> {

    private AkylasClusterAlgorithm _algorithm;
    private int maxDistance = 100;
    private int minDistance = -1;
    private boolean showText = true;
    private float strokeWidth = 2;
    private FontDesc font;
    private FontDesc selectedFont;
    private int color = Color.WHITE;
    private int strokeColor = -1;
    private int selectedColor = -1;
    private int selectedStrokeColor = -1;

    @Override
    public String getApiName() {
        return "Akylas.GoogleMap.Cluster";
    }
    
    
    private static final String DEFAULT_TEMPLATE_TYPE = "Akylas.GoogleMap.Annotation";

    @Override
    protected String defaultProxyTypeFromTemplate() {
        return DEFAULT_TEMPLATE_TYPE;
    }


    protected Class annotationClass() {
        return AnnotationProxy.class;
    }

    @Override
    public void infoWindowDidClose() {
        ((GoogleMapView) mapView).infoWindowDidClose(infoView);
        infoView = null;
    }

    @Override
    public void cluster() {
        if (mapView != null) {
//            runInUiThread(new CommandNoReturn() {
//                public void execute() {
                    ((GoogleMapView) mapView).getClusterManager().clusterAlgo(_algorithm);
//                }
//            }, false);
        }
    }

    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case "maxDistance":
            maxDistance = TiConvert.toInt(newValue);
            if (_algorithm != null) {
                ((AkylasClusterAlgorithm) _algorithm).maxDistanceAtZoom = maxDistance;
                cluster();
            }
            break;
        case "minDistance":
            minDistance = TiConvert.toInt(newValue);
            if (_algorithm != null) {
                ((AkylasClusterAlgorithm) _algorithm).minDistance = minDistance;
                cluster();
            }
            break;
        case "showText":
            showText = TiConvert.toBoolean(newValue, true);
            if (_algorithm != null) {
                cluster();
            }
            break;
        case "strokeWidth":
            strokeWidth = TiConvert.toFloat(newValue);
            if (_algorithm != null) {
                cluster();
            }
            break;
        case "font":
            font = TiUIHelper.getFontStyle(getActivity(),
                    (HashMap<String, Object>) newValue);
            if (_algorithm != null) {
                cluster();
            }
            break;
        case "selectedFont":
            selectedFont = TiUIHelper.getFontStyle(getActivity(),
                    (HashMap<String, Object>) newValue);
            if (_algorithm != null) {
                cluster();
            }
            break;

        case "color":
            color = TiConvert.toColor(newValue);
            if (_algorithm != null) {
                cluster();
            }
            break;
        case "selectedColor":
            selectedColor = TiConvert.toColor(newValue);
            if (_algorithm != null) {
                cluster();
            }
            break;
        case "strokeColor":
            strokeColor = TiConvert.toColor(newValue);
            if (_algorithm != null) {
                cluster();
            }
            break;
        case "selectedStrokeColor":
            selectedStrokeColor = TiConvert.toColor(newValue);
            if (_algorithm != null) {
                cluster();
            }
            break;
        case TiC.PROPERTY_VISIBLE:
            visible = TiConvert.toBoolean(newValue, true);
            if (_algorithm != null) {
                _algorithm.setVisible(visible);
                cluster();
            }
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }

    @Override
    protected void onAnnotationsAdded(List toAdd) {
        if (toAdd != null) {
            getOrCreateAlgorithm().addItems(toAdd);
            cluster();
        }
    }

    @Override
    protected void onAnnotationsRemoved(List removed) {
        if (_algorithm != null && removed != null) {
            _algorithm.removeItems(removed);
            cluster();
        }
    }

    public AkylasClusterAlgorithm getOrCreateAlgorithm() {
        if (_algorithm == null) {
            _algorithm = new AkylasClusterAlgorithm();
            _algorithm.setProxy(this);
            _algorithm.maxDistanceAtZoom = maxDistance;
            _algorithm.minDistance = minDistance;
        }
        return _algorithm;
    }

    public AkylasClusterAlgorithm getAlgorithm() {
        return _algorithm;
    }

    public void onDeselect() {

    }

    public void onSelect() {
    }

    private Bitmap generateBitmap(Cluster<AnnotationProxy> cluster) {
        Bitmap result = null;
        if (selected) {
            if (selectedImageref != null) {
                result = selectedImageref.getBitmap();
            } else if (imageref != null) {
                result = imageref.getBitmap();
            }
        } else if (imageref != null) {
            result = imageref.getBitmap();
        }
        if (result != null && !showText) {
            return result;
        }
        Canvas canvas;
        Paint paint = new Paint();
        int color = -1;
        if (result == null) {
            result = Bitmap.createBitmap(30, 30, Bitmap.Config.ARGB_8888);
            result.eraseColor(Color.TRANSPARENT);
            canvas = new Canvas(result);

            // first drawCircle
            color = tintColor;
            if (selected && selectedTintColor != -1) {
                color = selectedTintColor;
            }
            if (color != -1) {
                paint.setColor(color);
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(strokeWidth);
                canvas.drawCircle(15, 15, 15, paint);
            }

            if (strokeWidth > 0) {
                color = strokeColor;
                if (selected && selectedStrokeColor != -1) {
                    color = selectedStrokeColor;
                }
                if (color != -1) {
                    paint.setColor(color);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(strokeWidth);
                    canvas.drawCircle(15, 15, 15, paint);
                }
            }
        } else {
            canvas = new Canvas(result);
        }
        FontDesc font = this.font;
        if (selected && selectedFont != null) {
            font = selectedFont;
        }
        color = this.color;
        if (selected && selectedColor != -1) {
            color = selectedColor;
        }
        if (color != -1 && font != null) {
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(strokeWidth);
            paint.setTextSize(font.size);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(font.typeface);
            canvas.drawText(String.valueOf(cluster.getSize()), 0, 0, paint);
        }

        return result;
    }

    public void prepareClusterMarkerOptions(Cluster<AnnotationProxy> cluster,
            MarkerOptions markerOptions) {
        if (cluster instanceof AkylasCluster) {
            ((AkylasCluster) cluster).proxy = this;
        }
        
        markerOptions.rotation(heading)
                .draggable(draggable).visible(visible).flat(flat);


        if (anchor != null) {
            markerOptions.anchor(anchor.x, anchor.y);
        }
        if (calloutAnchor != null) {
            markerOptions.infoWindowAnchor(calloutAnchor.x, 1-calloutAnchor.y);
        }

        BitmapDescriptor icon = BitmapDescriptorFactory
                .fromBitmap(generateBitmap(cluster));
        if (markerOptions != null) {
            markerOptions.icon(icon);
        }
    }
    
    public void removeFromMap() {
        if (mapView != null) {
            ((GoogleMapView) mapView).getClusterManager().removeClusterAlgorithm(_algorithm);
        }
    }
}
