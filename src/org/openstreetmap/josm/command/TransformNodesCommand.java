// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.command;

import static org.openstreetmap.josm.tools.I18n.trn;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.Icon;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.visitor.AllNodesVisitor;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Abstract class with common services for nodes rotation and scaling commands.
 *
 * @author Olivier Croquette &lt;ocroquette@free.fr&gt;
 */
public abstract class TransformNodesCommand extends Command {

    /**
     * The nodes to transform.
     */
    protected Collection<Node> nodes = new LinkedList<>();

    /**
     * List of all old states of the nodes.
     */
    protected Map<Node, OldNodeState> oldStates = new HashMap<>();

    /**
     * Stores the state of the nodes before the command.
     */
    protected final void storeOldState() {
        for (Node n : this.nodes) {
            oldStates.put(n, new OldNodeState(n));
        }
    }

    /**
     * Creates a TransformNodesObject.
     * Find out the impacted nodes and store their initial state.
     */
    public TransformNodesCommand(Collection<OsmPrimitive> objects) {
        this.nodes = AllNodesVisitor.getAllNodes(objects);
        storeOldState();
    }

    /**
     * Handling of a mouse event (e.g. dragging event).
     * @param currentEN the current world position of the mouse
     */
    public abstract void handleEvent(EastNorth currentEN);

    /**
     * Implementation for the nodes transformation.
     * No parameters are given here, you should handle the user input in handleEvent()
     * and store it internally.
     */
    protected abstract void transformNodes();

    /**
     * Finally apply the transformation of the nodes.
     * This is called when the user is happy with the current state of the command
     * and its effects.
     */
    @Override
    public boolean executeCommand() {
        transformNodes();
        flagNodesAsModified();
        return true;
    }

    /**
     * Flag all nodes as modified.
     */
    public void flagNodesAsModified() {
        for (Node n : nodes) {
            n.setModified(true);
        }
    }

    /**
     * Restore the state of the nodes from the backup.
     */
    @Override
    public void undoCommand() {
        for (Node n : nodes) {
            OldNodeState os = oldStates.get(n);
            n.setCoor(os.getLatlon());
            n.setModified(os.isModified());
        }
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified, Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
    }

    @Override
    public Collection<? extends OsmPrimitive> getParticipatingPrimitives() {
        return nodes;
    }

    @Override
    public String getDescriptionText() {
        return trn("Transform {0} node", "Transform {0} nodes", nodes.size(), nodes.size());
    }

    @Override
    public Icon getDescriptionIcon() {
        return ImageProvider.get("data", "node");
    }

    /**
     * Get the nodes with the current transformation applied.
     * @return nodes with the current transformation applied
     */
    public Collection<Node> getTransformedNodes() {
        return nodes;
    }

    /**
     * Get the center of the nodes under modification.
     * It's just the barycenter.
     * @return center east/north of the nodes under modification
     * @see org.openstreetmap.josm.tools.Geometry#getCentroid(java.util.List)
     */
    public EastNorth getNodesCenter() {
        EastNorth sum = new EastNorth(0, 0);

        for (Node n : nodes) {
            EastNorth en = n.getEastNorth();
            sum = sum.add(en.east(), en.north());
        }
        return new EastNorth(sum.east()/this.nodes.size(), sum.north()/this.nodes.size());

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
        result = prime * result + ((oldStates == null) ? 0 : oldStates.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        TransformNodesCommand other = (TransformNodesCommand) obj;
        if (nodes == null) {
            if (other.nodes != null)
                return false;
        } else if (!nodes.equals(other.nodes))
            return false;
        if (oldStates == null) {
            if (other.oldStates != null)
                return false;
        } else if (!oldStates.equals(other.oldStates))
            return false;
        return true;
    }
}
