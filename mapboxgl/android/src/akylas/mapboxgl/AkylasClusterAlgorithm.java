package akylas.mapboxgl;

import java.util.Set;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.plugins.cluster.clustering.Cluster;
import com.mapbox.mapboxsdk.plugins.cluster.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.mapbox.mapboxsdk.plugins.cluster.clustering.algo.StaticCluster;


public class AkylasClusterAlgorithm extends NonHierarchicalDistanceBasedAlgorithm<AnnotationProxy> {
    ClusterProxy proxy;
    private boolean visible = true;

    public void prepareClusterMarkerOptions(Cluster<AnnotationProxy> cluster,
            MarkerOptions markerOptions) {
        if (proxy != null) {
            proxy.prepareClusterMarkerOptions(cluster, markerOptions);
        }
    }

    public void setProxy(ClusterProxy clusterProxy) {
        this.proxy = clusterProxy;

    }
    
    @Override
    protected StaticCluster<AnnotationProxy> createCluster() {
        return new AkylasCluster<AnnotationProxy>();
    }
    @Override
    public Set<? extends Cluster<AnnotationProxy>> getClusters(double zoom, LatLngBounds visibleBounds) {
        if (!visible) {
            return null;
        }
        return super.getClusters(zoom, visibleBounds);
    }

    @Override
    public double getMinZoom() {
        if (proxy != null) {
        return proxy.mMinZoom;
        }
        return -1;
    }

    @Override
    public double getMaxZoom() {
        if (proxy != null) {
        return proxy.mMaxZoom;
        }
        return -1;
    }
    
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }
}
