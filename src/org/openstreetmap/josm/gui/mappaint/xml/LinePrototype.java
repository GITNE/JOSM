// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.gui.mappaint.xml;

import java.awt.Color;

import org.openstreetmap.josm.data.osm.visitor.paint.MapPaintSettings;
import org.openstreetmap.josm.data.osm.visitor.paint.PaintColors;
import org.openstreetmap.josm.gui.mappaint.LineElemStyle;
import org.openstreetmap.josm.tools.I18n;

public class LinePrototype extends Prototype {

    protected int width;
    public int realWidth; //the real width of this line in meter
    public Color color;
    protected float[] dashed;
    public Color dashedColor;

    public LinePrototype(LinePrototype s, long maxScale, long minScale) {
        super(maxScale, minScale);
        this.width = s.width;
        this.realWidth = s.realWidth;
        this.color = s.color;
        this.dashed = s.dashed;
        this.dashedColor = s.dashedColor;
        this.priority = s.priority;
        this.conditions = s.conditions;
    }

    public LinePrototype() { init(); }

    public void init()
    {
        width = -1;
        realWidth = 0;
        dashed = new float[0];
        dashedColor = null;
        priority = 0;
        color = PaintColors.UNTAGGED.get();
    }

    public float[] getDashed() {
        return dashed;
    }

    public void setDashed(float[] dashed) {
        if (dashed.length == 0) {
            this.dashed = dashed;
            return;
        }

        boolean found = false;
        for (int i=0; i<dashed.length; i++) {
            if (dashed[i] > 0) {
                found = true;
            }
            if (dashed[i] < 0) {
                System.out.println(I18n.tr("Illegal dash pattern, values must be positive"));
            }
        }
        if (found) {
            this.dashed = dashed;
        } else {
            System.out.println(I18n.tr("Illegal dash pattern, at least one value must be > 0"));
        }
    }

    public int getWidth() {
        if (width == -1)
            return MapPaintSettings.INSTANCE.getDefaultSegmentWidth();
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public LineElemStyle createStyle() {
        return new LineElemStyle(minScale, maxScale, width, realWidth, color, dashed, dashedColor);
    }
}