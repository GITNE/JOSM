// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.data.osm;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Provides some node order , based on coordinates, nodes with equal coordinates are equal.
 *
 * @author viesturs
 */
public class NodePositionComparator implements Comparator<Node>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Node n1, Node n2) {

        if (n1.getCoor().equalsEpsilon(n2.getCoor()))
            return 0;

        int dLat = Double.compare(n1.getCoor().lat(), n2.getCoor().lat());
        return dLat != 0 ? dLat : Double.compare(n1.getCoor().lon(), n2.getCoor().lon());
    }
}
