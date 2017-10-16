package akylas.googlemap;

import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.algo.StaticCluster;

public class AkylasCluster<T extends ClusterItem> extends StaticCluster<T> {
    public ClusterProxy proxy = null;
    public AkylasCluster() {
        super();
    }

}
