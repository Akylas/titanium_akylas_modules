package akylas.mapboxgl;

import com.mapbox.mapboxsdk.plugins.cluster.clustering.ClusterItem;
import com.mapbox.mapboxsdk.plugins.cluster.clustering.algo.StaticCluster;

public class AkylasCluster<T extends ClusterItem> extends StaticCluster<T> {
    public ClusterProxy proxy = null;
    public AkylasCluster() {
        super();
    }

}
