package akylas.googlemap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import android.content.Context;

public class ClusterRenderer extends DefaultClusterRenderer<AnnotationProxy> {
    private final GoogleMapView tiMapView;
    public ClusterRenderer(Context context, GoogleMap map,
            ClusterManager<AnnotationProxy> clusterManager, GoogleMapView tiMapView) {
        super(context, map, clusterManager);
        this.tiMapView = tiMapView;
        setAnimationType(ANIMATION_FADE);
    }
//    @Override
//    protected void onBeforeClusterItemRendered(AnnotationProxy person, MarkerOptions markerOptions) {
//        // Draw a single person.
//        // Set the info window to show their name.
////        mImageView.setImageResource(person.profilePhoto);
////        Bitmap icon = mIconGenerator.makeIcon();
////        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(person.name);
//    }
    
//    @Override
//    protected boolean shouldAnimate() {
//        return false;
//    }
    
    @Override
    protected Marker getMarkerForClusterItem(Algorithm algorithm, AnnotationProxy item) {
        if (algorithm instanceof AkylasClusterAlgorithm) {
            ClusterProxy proxy = ((AkylasClusterAlgorithm)algorithm).proxy;
            if (proxy != null) {
                return ((GoogleMapView) proxy.getMapView()).addAnnotationToMap(item);
            }
        }
        return null;
    }
    
    @Override
    protected void onBeforeClusterRendered(Cluster<AnnotationProxy> cluster, Algorithm<AnnotationProxy> algorithm, MarkerOptions markerOptions) {
        if (algorithm instanceof AkylasClusterAlgorithm) {
            ((AkylasClusterAlgorithm)algorithm).prepareClusterMarkerOptions(cluster, markerOptions);
        }
        
        // Draw multiple people.
        // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
//        List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
//        int width = mDimension;
//        int height = mDimension;
//
//        for (Person p : cluster.getItems()) {
//            // Draw 4 at most.
//            if (profilePhotos.size() == 4) break;
//            Drawable drawable = getResources().getDrawable(p.profilePhoto);
//            drawable.setBounds(0, 0, width, height);
//            profilePhotos.add(drawable);
//        }
//        MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
//        multiDrawable.setBounds(0, 0, width, height);
//
//        mClusterImageView.setImageDrawable(multiDrawable);
//        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
//        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        // Always render clusters.
        return cluster.getSize() > 1;
    }
    
    @Override
    protected void removeMarker(Marker m) {
        tiMapView.removeMarker(m);
        
        super.removeMarker(m);
    }
}
