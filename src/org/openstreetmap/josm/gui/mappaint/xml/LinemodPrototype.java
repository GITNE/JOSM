// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.gui.mappaint.xml;

import org.openstreetmap.josm.gui.mappaint.Range;

public class LinemodPrototype extends LinePrototype implements Comparable<LinemodPrototype> {

    public enum WidthMode { ABSOLUTE, PERCENT, OFFSET }

    public WidthMode widthMode;
    public boolean over;

    public LinemodPrototype(LinemodPrototype s, Range range) {
        super(s, range);
        this.over = s.over;
        this.widthMode = s.widthMode;
    }

    /**
     * Constructs a new {@code LinemodPrototype}.
     */
    public LinemodPrototype() {
        init();
    }

    @Override
    public final void init() {
        super.init();
        over = true;
        widthMode = WidthMode.ABSOLUTE;
    }

    /**
     * get width for overlays
     * @param ref reference width
     * @return width according to {@link #widthMode} with a minimal value of 1
     */
    public float getWidth(float ref) {
        float res;
        if (widthMode == WidthMode.ABSOLUTE) {
            res = width;
        } else if (widthMode == WidthMode.OFFSET) {
            res = ref + width;
        } else {
            if (width < 0) {
                res = 0;
            } else {
                res = ref*width/100;
            }
        }
        return res <= 0 ? 1 : res;
    }

    @Override
    public int getWidth() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(LinemodPrototype s) {
        if (s.priority != priority)
            return s.priority > priority ? 1 : -1;
            if (!over && s.over)
                return -1;
            // we have no idea how to order other objects :-)
            return 0;
    }
}
