// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.command;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.visitor.NameVisitor;

/**
 * Command that basically replaces one OSM primitive by another of the
 * same type.
 *
 * @author Imi
 */
public class ChangeCommand extends Command {

    private final OsmPrimitive osm;
    private final OsmPrimitive newOsm;

    public ChangeCommand(OsmPrimitive osm, OsmPrimitive newOsm) {
        super();
        this.osm = osm;
        this.newOsm = newOsm;
    }

    @Override public boolean executeCommand() {
        super.executeCommand();
        osm.cloneFrom(newOsm);
        osm.modified = true;
        return true;
    }

    @Override public void fillModifiedData(Collection<OsmPrimitive> modified, Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
        modified.add(osm);
    }

    @Override public MutableTreeNode description() {
        NameVisitor v = new NameVisitor();
        osm.visit(v);
        return new DefaultMutableTreeNode(new JLabel(tr("Change {0} {1}", tr(v.className), v.name), v.icon, JLabel.HORIZONTAL));
    }
}
