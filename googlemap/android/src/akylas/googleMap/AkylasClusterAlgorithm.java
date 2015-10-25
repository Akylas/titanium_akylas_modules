package akylas.googlemap;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.algo.GridBasedAlgorithm;
import com.google.maps.android.clustering.algo.StaticCluster;

public class AkylasClusterAlgorithm extends GridBasedAlgorithm<AnnotationProxy> {
    ClusterProxy proxy;

    public void prepareClusterMarkerOptions(Cluster<AnnotationProxy> cluster,
            MarkerOptions markerOptions) {
        if (proxy != null) {
            proxy.prepareClusterMarkerOptions(cluster, markerOptions);
        }
        // TODO Auto-generated method stub

    }

    public void setProxy(ClusterProxy clusterProxy) {
        this.proxy = clusterProxy;

    }
    
    @Override
    protected StaticCluster<AnnotationProxy> createCluster() {
        return new AkylasCluster<AnnotationProxy>();
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

}
