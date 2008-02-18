// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.gui.layer.markerlayer;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;

/**
 * Marker class with button look-and-feel.
 * 
 * @author Frederik Ramm <frederik@remote.org>
 *
 */
public class ButtonMarker extends Marker {

	private Rectangle buttonRectangle;
	
	public ButtonMarker(LatLon ll, String buttonImage, double offset) {
		super(ll, null, buttonImage, offset);
		buttonRectangle = new Rectangle(0, 0, symbol.getIconWidth(), symbol.getIconHeight());
	}
	
	public ButtonMarker(LatLon ll, String text, String buttonImage, double offset) {
		super(ll, text, buttonImage, offset);
		buttonRectangle = new Rectangle(0, 0, symbol.getIconWidth(), symbol.getIconHeight());
	}
	
	@Override public boolean containsPoint(Point p) {
		Point screen = Main.map.mapView.getPoint(eastNorth);
		buttonRectangle.setLocation(screen.x+4, screen.y+2);
		return buttonRectangle.contains(p);
	}
	
	@Override public void paint(Graphics g, MapView mv, boolean mousePressed, String show) {
		Point screen = mv.getPoint(eastNorth);
		buttonRectangle.setLocation(screen.x+4, screen.y+2);
		symbol.paintIcon(mv, g, screen.x+4, screen.y+2);
		Border b;
		Point mousePosition = mv.getMousePosition();
		
		if (mousePosition != null) {
			// mouse is inside the window
			if (mousePressed) {
				b = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
			} else {
				b = BorderFactory.createBevelBorder(BevelBorder.RAISED);
			}
			Insets inset = b.getBorderInsets(mv);
			Rectangle r = new Rectangle(buttonRectangle);
			r.grow((inset.top+inset.bottom)/2, (inset.left+inset.right)/2);
			b.paintBorder(mv, g, r.x, r.y, r.width, r.height);
		}
		if ((text != null) && (show.equalsIgnoreCase("show")) && Main.pref.getBoolean("marker.buttonlabels"))
			g.drawString(text, screen.x+4, screen.y+2);
	}
}
