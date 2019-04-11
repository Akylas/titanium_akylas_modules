package akylas.carto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.TiMessenger.CommandNoReturn;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.util.TiUIHelper.FontDesc;

import com.carto.core.MapBounds;
import com.carto.core.MapPos;
import com.carto.core.MapRange;
import com.carto.datasources.LocalVectorDataSource;
import com.carto.layers.ClusterElementBuilder;
import com.carto.layers.ClusteredVectorLayer;
import com.carto.layers.Layer;
import com.carto.layers.VectorLayer;
import com.carto.styles.MarkerStyle;
import com.carto.styles.MarkerStyleBuilder;
import com.carto.utils.BitmapUtils;
import com.carto.vectorelements.Marker;
import com.carto.vectorelements.VectorElement;
import com.carto.vectorelements.VectorElementVector;

import akylas.map.common.BaseAnnotationProxy;
import akylas.map.common.BaseClusterProxy;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

@Kroll.proxy(creatableInModule = AkylasCartoModule.class, propertyAccessors = {
        "maxDistance", "minDistance" })
public class ClusterProxy extends BaseClusterProxy<MapPos, MapBounds> {

    // private AkylasClusterAlgorithm _algorithm;
    private int maxDistance = 100;
    private int minDistance = 20;
    private boolean showText = true;
    private float strokeWidth = 2;
    private FontDesc font;
    private FontDesc selectedFont;
    private int color = Color.WHITE;
    private int strokeColor = -1;
    private int selectedColor = -1;
    private int selectedStrokeColor = -1;

    ClusteredVectorLayer layer;
    LocalVectorDataSource source;

    @Override
    public String getApiName() {
        return "Akylas.Carto.Cluster";
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
        ((CartoView) mapView).infoWindowDidClose(infoView);
        infoView = null;
    }

    @Override
    public void cluster() {
//        if (mapView != null) {
            // runInUiThread(new CommandNoReturn() {
            // public void execute() {
            // ((CartoView)
            // mapView).getClusterManager().clusterAlgo(_algorithm);
            // }
            // }, false);
//        }
    }

    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case "maxDistance":
            maxDistance = TiConvert.toInt(newValue);
            // if (_algorithm != null) {
            // ((AkylasClusterAlgorithm) _algorithm).maxDistanceAtZoom =
            // maxDistance;
            // cluster();
            // }
            break;
        case "minDistance":
            minDistance = TiConvert.toInt(newValue);
            if (layer != null) {
                layer.setMinimumClusterDistance(minDistance);
            }
            // if (_algorithm != null) {
            // ((AkylasClusterAlgorithm) _algorithm).minDistance = minDistance;
            // cluster();
            // }
            break;
        case "showText":
            showText = TiConvert.toBoolean(newValue, true);
            // if (_algorithm != null) {
            // cluster();
            // }
            break;
        case "strokeWidth":
            strokeWidth = TiConvert.toFloat(newValue);
            // if (_algorithm != null) {
            // cluster();
            // }
            break;
        case "font":
            font = TiUIHelper.getFontStyle(getActivity(),
                    (HashMap<String, Object>) newValue);
            // if (_algorithm != null) {
            // cluster();
            // }
            break;
        case "selectedFont":
            selectedFont = TiUIHelper.getFontStyle(getActivity(),
                    (HashMap<String, Object>) newValue);
            // if (_algorithm != null) {
            // cluster();
            // }
            break;

        case "color":
            color = TiConvert.toColor(newValue);
            // if (_algorithm != null) {
            // cluster();
            // }
            break;
        case "selectedColor":
            selectedColor = TiConvert.toColor(newValue);
            // if (_algorithm != null) {
            // cluster();
            // }
            break;
        case "strokeColor":
            strokeColor = TiConvert.toColor(newValue);
            // if (_algorithm != null) {
            // cluster();
            // }
            break;
        case "selectedStrokeColor":
            selectedStrokeColor = TiConvert.toColor(newValue);
            // if (_algorithm != null) {
            // cluster();
            // }
            break;
        case TiC.PROPERTY_VISIBLE:
            visible = TiConvert.toBoolean(newValue, true);
            if (layer != null) {
                layer.setVisible(visible);
            }
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }

    @Override
    protected void onAnnotationsAdded(List toAdd) {
        if (toAdd != null && this.mapView != null) {
            // getOrCreateAlgorithm().addItems(toAdd);
            final Activity activity = this.getActivity();
            for (Object item : toAdd) {
                if (item instanceof AnnotationProxy) {
                    ((AnnotationProxy) item).setActivity(activity);
                    ((CartoView) this.mapView).addAnnotationToSource(
                            (AnnotationProxy) item, getSource());
                }
            }
            // cluster();
        }
    }

    @Override
    protected void onAnnotationsRemoved(List removed) {
        for (Object item : removed) {
            if (item instanceof AnnotationProxy) {
                ((CartoView) this.mapView).handleRemoveSingleAnnotation(
                        (AnnotationProxy) item, source);
            }
        }
        // if (_algorithm != null && removed != null) {
        // _algorithm.removeItems(removed);
        // cluster();
        // }
    }

    // public AkylasClusterAlgorithm getOrCreateAlgorithm() {
    // if (_algorithm == null) {
    // _algorithm = new AkylasClusterAlgorithm();
    // _algorithm.setProxy(this);
    // _algorithm.maxDistanceAtZoom = maxDistance;
    // _algorithm.minDistance = minDistance;
    // }
    // return _algorithm;
    // }

    // public AkylasClusterAlgorithm getAlgorithm() {
    // return _algorithm;
    // }

    public void onDeselect() {

    }

    public void onSelect() {
    }

    private Bitmap generateBitmap(VectorElementVector elements) {
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
            canvas.drawText(String.valueOf(elements.size()), 0, 0, paint);
        }

        return result;
    }

    //
    // // public void prepareClusterMarkerOptions(Cluster<AnnotationProxy>
    // cluster,
    // // MarkerOptions markerOptions) {
    // // if (cluster instanceof AkylasCluster) {
    // // ((AkylasCluster) cluster).proxy = this;
    // // }
    // //
    // // markerOptions.rotation(heading)
    // // .draggable(draggable).visible(visible).flat(flat);
    // //
    // //
    // // if (anchor != null) {
    // // markerOptions.anchor(anchor.x, anchor.y);
    // // }
    // // if (calloutAnchor != null) {
    // // markerOptions.infoWindowAnchor(calloutAnchor.x, 1-calloutAnchor.y);
    // // }
    // //
    // // BitmapDescriptor icon = BitmapDescriptorFactory
    // // .fromBitmap(generateBitmap(cluster));
    // // if (markerOptions != null) {
    // // markerOptions.icon(icon);
    // // }
    // // }
    /**
     * CLUSTER BUILDER
     **/
    private class MyClusterElementBuilder extends ClusterElementBuilder {

        private Map<Integer, MarkerStyle> markerStyles = new HashMap<>();
        private Bitmap markerBitmap;

        MyClusterElementBuilder() {
            // markerBitmap =
            // BitmapFactory.decodeResource(context.getResources(),
            // R.drawable.marker_black);
        }

        @Override
        public VectorElement buildClusterElement(MapPos pos,
                VectorElementVector elements) {

            // Try to reuse existing marker styles
            MarkerStyle style = markerStyles.get((int) elements.size());

            if (elements.size() == 1) {
                style = ((Marker) elements.get(0)).getStyle();
            }

            if (style == null) {

                
                MarkerStyleBuilder styleBuilder = new MarkerStyleBuilder();
                styleBuilder.setBitmap(BitmapUtils
                        .createBitmapFromAndroidBitmap(generateBitmap(elements)));
                styleBuilder.setSize(30);
                styleBuilder.setPlacementPriority((int) -elements.size());

                style = styleBuilder.buildStyle();

                markerStyles.put((int) elements.size(), style);
            }

            // Create marker for the cluster
            Marker marker = new Marker(pos, style);
            return marker;
        }
    }

    public void removeFromMap() {
        if (layer != null && mapView != null) {
            ((CartoView) mapView).getMapView().getLayers().remove(layer);
            source = null;
            layer = null;
            // ((CartoView)
            // mapView).getClusterManager().removeClusterAlgorithm(_algorithm);
        }
    }

    private LocalVectorDataSource getSource() {
        if (source == null && this.mapView != null) {
            source = new LocalVectorDataSource(
                    ((CartoView) this.mapView).getProjection());
            final Activity activity = this.getActivity();
            List<BaseAnnotationProxy> list = this.annotations();
            if (list != null) {
                for (BaseAnnotationProxy item : list) {
                    if (item instanceof AnnotationProxy) {
                        ((AnnotationProxy) item).setActivity(activity);
                        ((CartoView) this.mapView).addAnnotationToSource(
                                (AnnotationProxy) item, source);
                    }
                }
            }

        }
        return source;
    }

    @Override
    public void setMinZoom(Object value) {
        super.setMinZoom(value);
        if (layer != null) {
            layer.setVisibleZoomRange(new MapRange(mMinZoom, mMaxZoom));
        }
    }

    @Override
    public void setMaxZoom(Object value) {
        super.setMaxZoom(value);
        if (layer != null) {
            layer.setVisibleZoomRange(new MapRange(mMinZoom, mMaxZoom));
        }
    }

    @Override
    public void setVisible(Object value) {
        super.setVisible(value);
        if (layer != null) {
            layer.setVisible(visible);
        }
    }

    public VectorLayer getLayer() {
        if (layer == null) {
            layer = new ClusteredVectorLayer(getSource(),
                    new MyClusterElementBuilder());
            layer.setMinimumClusterDistance(minDistance);
            layer.setVisibleZoomRange(new MapRange(mMinZoom, mMaxZoom));
            layer.setVisible(visible);
        }
        return layer;
    }
}
