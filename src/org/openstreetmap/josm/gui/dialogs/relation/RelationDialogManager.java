// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.gui.dialogs.relation;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.layer.Layer.LayerChangeListener;


/**
 * RelationDialogManager keeps track of the open relation editors.
 *
 */
public class RelationDialogManager extends WindowAdapter implements LayerChangeListener{

    /** keeps track of open relation editors */
    static RelationDialogManager relationDialogManager;

    /**
     * Replies the singleton {@see RelationDialogManager}
     * 
     * @return the singleton {@see RelationDialogManager}
     */
    static public RelationDialogManager getRelationDialogManager() {
        if (RelationDialogManager.relationDialogManager == null) {
            RelationDialogManager.relationDialogManager = new RelationDialogManager();
            Layer.listeners.add(RelationDialogManager.relationDialogManager);
        }
        return RelationDialogManager.relationDialogManager;
    }

    /**
     * Helper class for keeping the context of a relation editor. A relation editor
     * is open for a specific relation managed by a specific {@see OsmDataLayer}
     *
     */
    static private class DialogContext {
        public Relation relation;
        public OsmDataLayer layer;

        public DialogContext(OsmDataLayer layer, Relation relation) {
            this.layer = layer;
            this.relation = relation;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((layer == null) ? 0 : layer.hashCode());
            result = prime * result + ((relation == null) ? 0 : relation.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            DialogContext other = (DialogContext) obj;
            if (layer == null) {
                if (other.layer != null)
                    return false;
            } else if (!layer.equals(other.layer))
                return false;
            if (relation == null) {
                if (other.relation != null)
                    return false;
            } else if (!relation.equals(other.relation))
                return false;
            return true;
        }

        public boolean matchesLayer(OsmDataLayer layer) {
            if (layer == null) return false;
            return this.layer.equals(layer);
        }

        @Override
        public String toString() {
            return "[Context: layer=" + layer.getName() + ",relation=" + relation.id + "]";
        }
    }

    /** the map of open dialogs */
    private HashMap<DialogContext, RelationEditor> openDialogs;

    /**
     * constructor
     */
    public RelationDialogManager(){
        openDialogs = new HashMap<DialogContext, RelationEditor>();
    }

    /**
     * Register the relation editor for a relation managed by a
     * {@see OsmDataLayer}.
     * 
     * @param layer the layer
     * @param relation the relation
     * @param editor the editor
     */
    public void register(OsmDataLayer layer, Relation relation, RelationEditor editor) {
        if (relation == null) {
            relation = new Relation();
        }
        DialogContext context = new DialogContext(layer, relation);
        openDialogs.put(context, editor);
        editor.addWindowListener(this);
    }

    /**
     * Replies true if there is an open relation editor for the relation managed
     * by the given layer. Replies false if relation is null.
     * 
     * @param layer  the layer
     * @param relation  the relation. May be null.
     * @return true if there is an open relation editor for the relation managed
     * by the given layer; false otherwise
     */
    public boolean isOpenInEditor(OsmDataLayer layer, Relation relation) {
        if (relation == null) return false;
        DialogContext context = new DialogContext(layer, relation);
        return openDialogs.keySet().contains(context);

    }

    /**
     * Replies the editor for the relation managed by layer. Null, if no such editor
     * is currently open. Returns null, if relation is null.
     * 
     * @param layer the layer
     * @param relation the relation
     * @return the editor for the relation managed by layer. Null, if no such editor
     * is currently open.
     * 
     * @see #isOpenInEditor(OsmDataLayer, Relation)
     */
    public RelationEditor getEditorForRelation(OsmDataLayer layer, Relation relation) {
        if (relation == null) return null;
        DialogContext context = new DialogContext(layer, relation);
        return openDialogs.get(context);
    }

    /**
     * called when a layer is removed
     * 
     */
    public void layerRemoved(Layer oldLayer) {
        if (oldLayer == null || ! (oldLayer instanceof OsmDataLayer))
            return;
        OsmDataLayer dataLayer = (OsmDataLayer)oldLayer;

        Iterator<Entry<DialogContext,RelationEditor>> it = openDialogs.entrySet().iterator();
        while(it.hasNext()) {
            Entry<DialogContext,RelationEditor> entry = it.next();
            if (entry.getKey().matchesLayer(dataLayer)) {
                RelationEditor editor = entry.getValue();
                it.remove();
                editor.setVisible(false);
                editor.dispose();
            }
        }
    }

    public void activeLayerChange(Layer oldLayer, Layer newLayer) {
        // do nothing
    }

    public void layerAdded(Layer newLayer) {
        // do nothing
    }

    @Override
    public void windowClosed(WindowEvent e) {
        RelationEditor editor = (RelationEditor)e.getWindow();
        DialogContext context = null;
        for (DialogContext c : openDialogs.keySet()) {
            if (openDialogs.get(c).equals(editor)) {
                context = c;
                break;
            }
        }
        if (context != null) {
            openDialogs.remove(context);
        }
    }

    /**
     * Positions an {@see RelationEditor} centered on the screen
     * 
     * @param editor the editor
     */
    protected void centerOnScreen(RelationEditor editor) {
        Point p = new Point(0,0);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        p.x = (d.width - editor.getSize().width)/2;
        p.y = (d.height - editor.getSize().height)/2;
        p.x = Math.max(p.x,0);
        p.y = Math.max(p.y,0);
        editor.setLocation(p);
    }

    /**
     * Replies true, if there is another open {@see RelationEditor} whose
     * upper left corner is close to <code>p</code>.
     * 
     * @param p  the reference point to check
     * @return true, if there is another open {@see RelationEditor} whose
     * upper left corner is close to <code>p</code>.
     */
    protected boolean hasEditorWithCloseUpperLeftCorner(Point p) {
        for (RelationEditor editor: openDialogs.values()) {
            Point corner = editor.getLocation();
            if (p.x >= corner.x -5 && corner.x + 5 >= p.x
                    && p.y >= corner.y -5 && corner.y + 5 >= p.y)
                return true;
        }
        return false;
    }

    /**
     * Positions a {@see RelationEditor} close to the center of the screen, in such
     * a way, that it doesn't entirely cover another {@see RelationEditor}
     * 
     * @param editor
     */
    protected void positionCloseToScreenCenter(RelationEditor editor) {
        Point p = new Point(0,0);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        p.x = (d.width - editor.getSize().width)/2;
        p.y = (d.height - editor.getSize().height)/2;
        p.x = Math.max(p.x,0);
        p.y = Math.max(p.y,0);
        while(hasEditorWithCloseUpperLeftCorner(p)) {
            p.x += 20;
            p.y += 20;
        }
        editor.setLocation(p);
    }

    /**
     * Positions a {@see RelationEditor} on the screen. Tries to center it on the
     * screen. If it hide another instance of an editor at the same position this
     * method tries to reposition <code>editor</code> by moving it slightly down and
     * slightly to the right.
     * 
     * @param editor the editor
     */
    public void positionOnScreen(RelationEditor editor) {
        if (editor == null) return;
        if (openDialogs.isEmpty()) {
            centerOnScreen(editor);
        } else {
            positionCloseToScreenCenter(editor);
        }
    }

}
