// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.command;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.GridBagLayout;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.data.osm.visitor.CollectBackReferencesVisitor;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.PrimitiveNameFormatter;
import org.openstreetmap.josm.tools.DontShowAgainInfo;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * A command to delete a number of primitives from the dataset.
 * @author imi
 */
public class DeleteCommand extends Command {

    /**
     * The primitives that get deleted.
     */
    private final Collection<? extends OsmPrimitive> toDelete;

    /**
     * Constructor for a collection of data
     */
    public DeleteCommand(Collection<? extends OsmPrimitive> data) {
        super();
        this.toDelete = data;
    }

    /**
     * Constructor for a single data item. Use the collection constructor to delete multiple
     * objects.
     */
    public DeleteCommand(OsmPrimitive data) {
        this.toDelete = Collections.singleton(data);
    }

    @Override public boolean executeCommand() {
        super.executeCommand();
        for (OsmPrimitive osm : toDelete) {
            osm.delete(true);
        }
        return true;
    }

    @Override public void fillModifiedData(Collection<OsmPrimitive> modified, Collection<OsmPrimitive> deleted,
            Collection<OsmPrimitive> added) {
        deleted.addAll(toDelete);
    }

    @Override public MutableTreeNode description() {
        if (toDelete.size() == 1) {
            OsmPrimitive primitive = toDelete.iterator().next();
            return new DefaultMutableTreeNode(
                    new JLabel(
                            tr("Delete {1} {0}",
                                    new PrimitiveNameFormatter().getName(primitive),
                                    OsmPrimitiveType.from(primitive).getLocalizedDisplayNameSingular()
                            ),
                            ImageProvider.get(OsmPrimitiveType.from(primitive)),
                            JLabel.HORIZONTAL));
        }

        String cname = null;
        String apiname = null;
        String cnamem = null;
        for (OsmPrimitive osm : toDelete) {
            if (cname == null) {
                apiname = OsmPrimitiveType.from(osm).getAPIName();
                cname = OsmPrimitiveType.from(osm).getLocalizedDisplayNameSingular();
                cnamem = OsmPrimitiveType.from(osm).getLocalizedDisplayNamePlural();
            } else if (!cname.equals(OsmPrimitiveType.from(osm).getLocalizedDisplayNameSingular())) {
                apiname = "object";
                cname = trn("object", "objects", 1);
                cnamem = trn("object", "objects", 2);
            }
        }
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new JLabel(tr("Delete {0} {1}", toDelete.size(), trn(
                cname, cnamem, toDelete.size())), ImageProvider.get("data", apiname), JLabel.HORIZONTAL));
        for (OsmPrimitive osm : toDelete) {
            root.add(new DefaultMutableTreeNode(
                    new JLabel(
                            new PrimitiveNameFormatter().getName(osm),
                            ImageProvider.get(OsmPrimitiveType.from(osm)),
                            JLabel.HORIZONTAL)
            )
            );
        }
        return root;
    }

    /**
     * Delete the primitives and everything they reference.
     *
     * If a node is deleted, the node and all ways and relations the node is part of are deleted as
     * well.
     *
     * If a way is deleted, all relations the way is member of are also deleted.
     *
     * If a way is deleted, only the way and no nodes are deleted.
     *
     * @param selection The list of all object to be deleted.
     * @return command A command to perform the deletions, or null of there is nothing to delete.
     */
    public static Command deleteWithReferences(Collection<? extends OsmPrimitive> selection) {
        CollectBackReferencesVisitor v = new CollectBackReferencesVisitor(Main.main.getCurrentDataSet());
        for (OsmPrimitive osm : selection) {
            osm.visit(v);
        }
        v.data.addAll(selection);
        if (v.data.isEmpty())
            return null;
        if (!checkAndConfirmOutlyingDeletes(v.data))
            return null;
        return new DeleteCommand(v.data);
    }

    private static int testRelation(Relation ref, OsmPrimitive osm) {
        PrimitiveNameFormatter formatter = new PrimitiveNameFormatter();
        String role = new String();
        for (RelationMember m : ref.members) {
            if (m.member == osm) {
                role = m.role;
                break;
            }
        }
        if (role.length() > 0)
            return new ExtendedDialog(
                    Main.parent,
                    tr("Conflicting relation"),
                    tr("Selection \"{0}\" is used by relation \"{1}\" with role {2}.\nDelete from relation?",
                            formatter.getName(osm), formatter.getName(ref), role),
                            new String[] {tr("Delete from relation"), tr("Cancel")},
                            new String[] {"dialogs/delete.png", "cancel.png"}).getValue();
        else
            return new ExtendedDialog(Main.parent,
                    tr("Conflicting relation"),
                    tr("Selection \"{0}\" is used by relation \"{1}\".\nDelete from relation?",
                            formatter.getName(osm), formatter.getName(ref)),
                            new String[] {tr("Delete from relation"), tr("Cancel")},
                            new String[] {"dialogs/delete.png", "cancel.png"}).getValue();
    }

    public static Command delete(Collection<? extends OsmPrimitive> selection) {
        return delete(selection, true);
    }

    /**
     * Try to delete all given primitives.
     *
     * If a node is used by a way, it's removed from that way. If a node or a way is used by a
     * relation, inform the user and do not delete.
     *
     * If this would cause ways with less than 2 nodes to be created, delete these ways instead. If
     * they are part of a relation, inform the user and do not delete.
     *
     * @param selection The objects to delete.
     * @param alsoDeleteNodesInWay <code>true</code> if nodes should be deleted as well
     * @return command A command to perform the deletions, or null of there is nothing to delete.
     */
    public static Command delete(Collection<? extends OsmPrimitive> selection, boolean alsoDeleteNodesInWay) {
        if (selection.isEmpty())
            return null;

        Collection<OsmPrimitive> del = new HashSet<OsmPrimitive>(selection);
        Collection<Way> waysToBeChanged = new HashSet<Way>();
        HashMap<OsmPrimitive, Collection<OsmPrimitive>> relationsToBeChanged = new HashMap<OsmPrimitive, Collection<OsmPrimitive>>();

        if (alsoDeleteNodesInWay) {
            // Delete untagged nodes that are to be unreferenced.
            Collection<OsmPrimitive> delNodes = new HashSet<OsmPrimitive>();
            for (OsmPrimitive osm : del) {
                if (osm instanceof Way) {
                    for (Node n : ((Way) osm).nodes) {
                        if (!n.isTagged()) {
                            CollectBackReferencesVisitor v = new CollectBackReferencesVisitor(Main.main.getCurrentDataSet(), false);
                            n.visit(v);
                            v.data.removeAll(del);
                            if (v.data.isEmpty()) {
                                delNodes.add(n);
                            }
                        }
                    }
                }
            }
            del.addAll(delNodes);
        }

        if (!checkAndConfirmOutlyingDeletes(del))
            return null;

        for (OsmPrimitive osm : del) {
            CollectBackReferencesVisitor v = new CollectBackReferencesVisitor(Main.main.getCurrentDataSet(), false);
            osm.visit(v);
            for (OsmPrimitive ref : v.data) {
                if (del.contains(ref)) {
                    continue;
                }
                if (ref instanceof Way) {
                    waysToBeChanged.add((Way) ref);
                } else if (ref instanceof Relation) {
                    if (testRelation((Relation) ref, osm) == 1) {
                        Collection<OsmPrimitive> relset = relationsToBeChanged.get(ref);
                        if (relset == null) {
                            relset = new HashSet<OsmPrimitive>();
                        }
                        relset.add(osm);
                        relationsToBeChanged.put(ref, relset);
                    } else
                        return null;
                } else
                    return null;
            }
        }

        Collection<Command> cmds = new LinkedList<Command>();
        for (Way w : waysToBeChanged) {
            Way wnew = new Way(w);
            wnew.removeNodes(del);
            if (wnew.nodes.size() < 2) {
                del.add(w);

                CollectBackReferencesVisitor v = new CollectBackReferencesVisitor(Main.main.getCurrentDataSet(), false);
                w.visit(v);
                for (OsmPrimitive ref : v.data) {
                    if (del.contains(ref)) {
                        continue;
                    }
                    if (ref instanceof Relation) {
                        Boolean found = false;
                        Collection<OsmPrimitive> relset = relationsToBeChanged.get(ref);
                        if (relset == null) {
                            relset = new HashSet<OsmPrimitive>();
                        } else {
                            for (OsmPrimitive m : relset) {
                                if (m == w) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (!found) {
                            if (testRelation((Relation) ref, w) == 1) {
                                relset.add(w);
                                relationsToBeChanged.put(ref, relset);
                            } else
                                return null;
                        }
                    } else
                        return null;
                }
            } else {
                cmds.add(new ChangeCommand(w, wnew));
            }
        }

        Iterator<OsmPrimitive> iterator = relationsToBeChanged.keySet().iterator();
        while (iterator.hasNext()) {
            Relation cur = (Relation) iterator.next();
            Relation rel = new Relation(cur);
            for (OsmPrimitive osm : relationsToBeChanged.get(cur)) {
                rel.removeMembersFor(osm);
            }
            cmds.add(new ChangeCommand(cur, rel));
        }

        // #2707: ways to be deleted can include new nodes (with node.id == 0).
        // Remove them from the way before the way is deleted. Otherwise the
        // deleted way is saved (or sent to the API) with a dangling reference to a node
        // Example:
        //  <node id='2' action='delete' visible='true' version='1' ... />
        //  <node id='1' action='delete' visible='true' version='1' ... />
        //  <!-- missing node with id -1 because new deleted nodes are not persisted -->
        //   <way id='3' action='delete' visible='true' version='1'>
        //     <nd ref='1' />
        //     <nd ref='-1' />  <!-- heres the problem -->
        //     <nd ref='2' />
        //   </way>
        for (OsmPrimitive primitive : del) {
            if (! (primitive instanceof Way)) {
                continue;
            }
            Way w = (Way)primitive;
            if (w.id == 0) { // new ways with id == 0 are fine,
                continue;    // process existing ways only
            }
            Way wnew = new Way(w);
            ArrayList<Node> nodesToStrip = new ArrayList<Node>();
            // lookup new nodes which have been added to the set of deleted
            // nodes ...
            for (Node n : wnew.nodes) {
                if (n.id == 0 && del.contains(n)) {
                    nodesToStrip.add(n);
                }
            }
            // .. and remove them from the way
            //
            wnew.nodes.removeAll(nodesToStrip);
            if (!nodesToStrip.isEmpty()) {
                cmds.add(new ChangeCommand(w,wnew));
            }
        }

        if (!del.isEmpty()) {
            cmds.add(new DeleteCommand(del));
        }

        return new SequenceCommand(tr("Delete"), cmds);
    }

    public static Command deleteWaySegment(WaySegment ws) {
        List<Node> n1 = new ArrayList<Node>(), n2 = new ArrayList<Node>();

        n1.addAll(ws.way.nodes.subList(0, ws.lowerIndex + 1));
        n2.addAll(ws.way.nodes.subList(ws.lowerIndex + 1, ws.way.nodes.size()));

        if (n1.size() < 2 && n2.size() < 2)
            return new DeleteCommand(Collections.singleton(ws.way));

        Way wnew = new Way(ws.way);
        wnew.nodes.clear();

        if (n1.size() < 2) {
            wnew.nodes.addAll(n2);
            return new ChangeCommand(ws.way, wnew);
        } else if (n2.size() < 2) {
            wnew.nodes.addAll(n1);
            return new ChangeCommand(ws.way, wnew);
        } else {
            Collection<Command> cmds = new LinkedList<Command>();

            wnew.nodes.addAll(n1);
            cmds.add(new ChangeCommand(ws.way, wnew));

            Way wnew2 = new Way();
            if (wnew.keys != null) {
                wnew2.keys = new HashMap<String, String>(wnew.keys);
            }
            wnew2.nodes.addAll(n2);
            cmds.add(new AddCommand(wnew2));

            return new SequenceCommand(tr("Split way segment"), cmds);
        }
    }

    /**
     * Check whether user is about to delete data outside of the download area.
     * Request confirmation if he is.
     */
    private static boolean checkAndConfirmOutlyingDeletes(Collection<OsmPrimitive> del) {
        Area a = Main.main.getCurrentDataSet().getDataSourceArea();
        if (a != null) {
            for (OsmPrimitive osm : del) {
                if (osm instanceof Node && osm.id != 0) {
                    Node n = (Node) osm;
                    if (!a.contains(n.getCoor())) {
                        JPanel msg = new JPanel(new GridBagLayout());
                        msg.add(new JLabel(
                                "<html>" +
                                // leave message in one tr() as there is a grammatical connection.
                                tr("You are about to delete nodes outside of the area you have downloaded." +
                                        "<br>" +
                                        "This can cause problems because other objects (that you don't see) might use them." +
                                        "<br>" +
                                "Do you really want to delete?") + "</html>"));
                        return DontShowAgainInfo.show("delete_outside_nodes", msg, false, JOptionPane.YES_NO_OPTION, JOptionPane.YES_OPTION);
                    }

                }
            }
        }
        return true;
    }
}
