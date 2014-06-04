package akylas.map;

import android.graphics.Point;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class GeoUtils {
    private static final double LN2 = 0.6931471805599453;
    private static final int WORLD_PX_HEIGHT = 256;
    private static final int WORLD_PX_WIDTH = 256;
    private static final int ZOOM_MAX = 21;

    public double getBoundsZoomLevel(LatLngBounds bounds) {
        return getBoundsZoomLevel(bounds, WORLD_PX_HEIGHT, WORLD_PX_WIDTH);
    }

    public double getBoundsZoomLevel(LatLngBounds bounds, int mapWidthPx,
            int mapHeightPx) {

        LatLng ne = bounds.northeast;
        LatLng sw = bounds.southwest;

        double latFraction = (latRad(ne.latitude) - latRad(sw.latitude))
                / Math.PI;

        double lngDiff = ne.longitude - sw.longitude;
        double lngFraction = ((lngDiff < 0) ? (lngDiff + 360) : lngDiff) / 360;

        double latZoom = zoom(mapHeightPx, WORLD_PX_HEIGHT, latFraction);
        double lngZoom = zoom(mapWidthPx, WORLD_PX_WIDTH, lngFraction);

        double result = Math.min(latZoom, lngZoom);
        return Math.min(result, ZOOM_MAX);
    }

    private double latRad(double lat) {
        double sin = Math.sin(lat * Math.PI / 180);
        double radX2 = Math.log((1 + sin) / (1 - sin)) / 2;
        return Math.max(Math.min(radX2, Math.PI), -Math.PI) / 2;
    }

    private double zoom(int mapPx, int worldPx, double fraction) {
        return Math.floor(Math.log(mapPx / worldPx / fraction) / LN2);
    }
    /**
     * 
     * @param x  view coord relative to left
     * @param y  view coord relative to top
     * @param vw MapView
     * @return GeoPoint
     */

    private LatLng geoPointFromScreenCoords(int x, int y, GoogleMap map, View mapView){
        if (x < 0 || y < 0 || x > mapView.getMeasuredWidth() || y > mapView.getMeasuredHeight()){
            return null; // coord out of bounds
        }
        // Get the top left GeoPoint
        Projection projection = map.getProjection();
        LatLng geoPointTopLeft = projection.fromScreenLocation(new Point(0,0));
        // Get the top left Point (includes osmdroid offsets)
//        Point topLeftPoint = projection.toScreenLocation(geoPointTopLeft);
        // get the GeoPoint of any point on screen 
        return projection.fromScreenLocation(new Point(x,y));
    }

    /**
     * 
     * @param gp GeoPoint
     * @param vw Mapview
     * @return a 'Point' in screen coords relative to top left
     */

    private Point pointFromGeoPoint(LatLng gp, GoogleMap map, View mapView){

        Projection projection = map.getProjection();
        Point rtnPoint = projection.toScreenLocation(gp);
        // Get the top left GeoPoint
        LatLng geoPointTopLeft = projection.fromScreenLocation(new Point(0,0));
        // Get the top left Point (includes osmdroid offsets)
        Point topLeftPoint = projection.toScreenLocation(geoPointTopLeft);
        rtnPoint.x-= topLeftPoint.x; // remove offsets
        rtnPoint.y-= topLeftPoint.y;
        if (rtnPoint.x > mapView.getWidth() || rtnPoint.y > mapView.getHeight() || 
                rtnPoint.x < 0 || rtnPoint.y < 0){
            return null; // gp must be off the screen
        }
        return rtnPoint;
    }
}
