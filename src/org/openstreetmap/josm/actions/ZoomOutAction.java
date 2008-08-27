// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.Main;

public final class ZoomOutAction extends JosmAction {

	public ZoomOutAction() {
		super(tr("Zoom out"), "dialogs/zoomout", tr("Zoom out"),
		        KeyEvent.VK_MINUS, 0, true);
		setEnabled(true);
	}

	public void actionPerformed(ActionEvent e) {
		double zoom = Main.map.mapView.getScale();
		Main.map.mapView.zoomTo(Main.map.mapView.getCenter(), zoom /.9);
	}
}
