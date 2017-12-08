package akylas.carto;

import java.util.HashMap;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiImageHelper;
import org.appcelerator.titanium.view.TiDrawableReference;

import com.carto.core.MapPos;
import com.carto.projections.Projection;
import com.carto.styles.BillboardOrientation;
import com.carto.styles.MarkerStyle;
import com.carto.styles.MarkerStyleBuilder;
import com.carto.utils.BitmapUtils;
import com.carto.vectorelements.Marker;
import com.carto.vectorelements.VectorElement;

import akylas.map.common.AkylasMapBaseModule;
import akylas.map.common.AkylasMapBaseView;

//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.maps.android.clustering.ClusterItem;

import akylas.map.common.BaseAnnotationProxy;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;

@Kroll.proxy(creatableInModule = AkylasCartoModule.class)
public class AnnotationProxy extends BaseAnnotationProxy<MapPos> {

    // static class AkMarker extends Marker {
    // AnnotationProxy proxy;
    // public AkMarker(AnnotationProxy proxy, MapPos pos, MarkerStyle style) {
    // super(pos, style);
    // this.proxy = proxy;
    // }
    // public AnnotationProxy getProxy() {
    // return this.proxy;
    // }
    //
    // }
    @Override
    public String getApiName() {
        return "Akylas.Carto.Annotation";
    }

    @Override
    public void infoWindowDidClose() {
        if (mapView != null) {
            ((CartoView) mapView).infoWindowDidClose(infoView);
        }
        infoView = null;
    }

    @Override
    public void removeFromMap() {
        // if (marker != null) {
        // ((VectorElementMarker) marker).removeFromMap();
        // }
    }

    @Override
    public CartoMarker getMarker() {
        return (CartoMarker) marker;
    }
    
    public VectorElement getVectorElement() {
        if (marker != null) {
            return ((CartoMarker) marker).getMarker();

        }
        return null;
    }

    public MarkerStyle getMarkerBuildStyle() {
        if (marker != null) {
            return getMarker().getMarkerBuildStyle();
        }
        return null;
    }

    public Marker createMarker(Projection baseProjection) {
        MapPos pos = baseProjection.fromWgs84(this.getPosition());
        Marker marker = new Marker(new MapPos(pos.getX(), pos.getY()),
                getMarkerBuildStyle());
        marker.setVisible(visible);
        marker.setRotation(getMarker().getHeading());
        return marker;
        // return map.addMarker(getMarkerOptions());
    }

    public static MarkerStyle getMarkerStyle(CartoView mapView, Projection baseProjection, HashMap data, boolean selected) {
        MarkerStyleBuilder builder = new MarkerStyleBuilder();
//        MapPos pos = AkylasCartoModule.latlongFromDict(data);
        if (data.containsKey(TiC.PROPERTY_ZINDEX)) {
            builder.setPlacementPriority(
                    TiConvert.toInt(data, TiC.PROPERTY_ZINDEX));
        }
        if (data.containsKey(AkylasCartoModule.PROPERTY_FLAT)) {
            BillboardOrientation orientation = TiConvert.toBoolean(data,
                    AkylasCartoModule.PROPERTY_FLAT)
                            ? BillboardOrientation.BILLBOARD_ORIENTATION_GROUND
                            : BillboardOrientation.BILLBOARD_ORIENTATION_FACE_CAMERA;
            builder.setOrientationMode(orientation);
        }
        if (data.containsKey(AkylasMapBaseModule.PROPERTY_ANCHOR)) {
            PointF anchor = TiConvert
                    .toPointF(data.get(AkylasMapBaseModule.PROPERTY_ANCHOR));
            final float anchorX = (anchor != null) ? anchor.x : 0.5f;
            final float anchorY = (anchor != null) ? anchor.y : 1.0f;
            builder.setAnchorPoint((anchorX - 0.5f) * 2, -(anchorY - 0.5f) * 2);
        }
        TiDrawableReference imageref = null;
        if (selected && data.containsKey(AkylasMapBaseModule.PROPERTY_SELECTED_IMAGE)) {
             imageref = TiDrawableReference.fromObject(mapView.getProxy(), data.get(AkylasMapBaseModule.PROPERTY_SELECTED_IMAGE));
        } else if (data.containsKey(TiC.PROPERTY_IMAGE)) {
             imageref = TiDrawableReference.fromObject(mapView.getProxy(), data.get(TiC.PROPERTY_IMAGE));
        }
        if (imageref != null) {
            Bitmap bitmap = TiImageHelper.getBitmap(data, imageref);
            if(bitmap != null) {
                builder.setBitmap(BitmapUtils .createBitmapFromAndroidBitmap(bitmap));
            }
        }
        return builder.buildStyle();
    }
    public static Marker createMarker(CartoView mapView, Projection baseProjection, HashMap data, boolean selected) {
        MapPos pos = AkylasCartoModule.latlongFromDict(data);
        Marker marker = new Marker(new MapPos(pos.getX(), pos.getY()), getMarkerStyle(mapView, baseProjection, data, selected));
        if (data.containsKey(TiC.PROPERTY_HEADING)) {
            marker.setRotation(-TiConvert.toFloat(data,TiC.PROPERTY_HEADING, 0));
        }
        if (data.containsKey(TiC.PROPERTY_VISIBLE)) {
            marker.setVisible(TiConvert.toBoolean(data,TiC.PROPERTY_VISIBLE, true));
        }
        return marker;
    }

}
