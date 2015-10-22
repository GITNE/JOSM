// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.actions;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openstreetmap.josm.JOSMFixture;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.actions.search.SearchAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmReader;

/**
 * Unit tests of {@link JoinAreasAction} class.
 */
public class JoinAreasActionTest {

    /**
     * Setup test.
     */
    @BeforeClass
    public static void setUp() {
        JOSMFixture.createUnitTestFixture().init(true);
    }

    /**
     * Non-regression test for bug #10511.
     * @throws IOException if any I/O error occurs
     * @throws IllegalDataException if OSM parsing fails
     */
    @Test
    public void testTicket10511() throws IOException, IllegalDataException {
        try (InputStream is = new FileInputStream(TestUtils.getRegressionDataFile(10511, "10511_mini.osm"))) {
            DataSet ds = OsmReader.parseDataSet(is, null);
            Main.map.mapView.addLayer(new OsmDataLayer(ds, null, null));
            // FIXME enable this test after we fix the bug. Test disabled for now
            // new JoinAreasAction().join(ds.getWays());
        }
    }

    /**
     * Non-regression test for bug #11992.
     * @throws IOException if any I/O error occurs
     * @throws IllegalDataException if OSM parsing fails
     */
    @Test
    public void testTicket11992() throws IOException, IllegalDataException {
        try (InputStream is = new FileInputStream(TestUtils.getRegressionDataFile(11992, "shapes.osm"))) {
            DataSet ds = OsmReader.parseDataSet(is, null);
            assertEquals(10, ds.getWays().size());
            Main.map.mapView.addLayer(new OsmDataLayer(ds, null, null));
            for (String ref : new String[]{"A", "B", "C", "D", "E"}) {
                System.out.print("Joining ways " + ref);
                SearchAction.search("type:way ref="+ref, SearchAction.SearchMode.replace);
                assertEquals(2, ds.getSelectedWays().size());
                Main.main.menu.joinAreas.join(ds.getSelectedWays());
                assertEquals(1, ds.getSelectedWays().size());
                System.out.println(" ==> OK");
            }
        }
    }
}
