// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.tools;

import java.awt.Color;
import java.util.Collection;

public class Utils {

    public static <T> boolean exists(Iterable<? extends T> collection, Predicate<? super T> predicate) {
        for (T item : collection) {
            if (predicate.evaluate(item))
                return true;
        }
        return false;
    }

    public static <T> boolean exists(Iterable collection, Class<? extends T> klass) {
        for (Object item : collection) {
            if (klass.isInstance(item))
                return true;
        }
        return false;
    }

    public static <T> T find(Iterable<? extends T> collection, Predicate<? super T> predicate) {
        for (T item : collection) {
            if (predicate.evaluate(item))
                return item;
        }
        return null;
    }

    public static <T> T find(Iterable collection, Class<? extends T> klass) {
        for (Object item : collection) {
            if (klass.isInstance(item)) {
                @SuppressWarnings("unchecked") T res = (T) item;
                return res;
            }
        }
        return null;
    }

    /**
     * Get minimum of 3 values
     */
    public static int min(int a, int b, int c) {
        if (b < c) {
            if (a < b)
                return a;
            return b;
        } else {
            if (a < c) {
                return a;
            }
            return c;
        }
    }

    public static int max(int a, int b, int c, int d) {
        return Math.max(Math.max(a, b), Math.max(c, d));
    }

    /**
     * for convenience: test whether 2 objects are either both null or a.equals(b)
     */
    public static <T> boolean equal(T a, T b) {
        if (a == b)
            return true;
        return (a != null && a.equals(b));
    }

    /**
     * return the modulus in the range [0, n)
     */
    public static int mod(int a, int n) {
        if (n <= 0)
            throw new IllegalArgumentException();
        int res = a % n;
        if (res < 0) {
            res += n;
        }
        return res;
    }

    /**
     * Joins a list of strings (or objects that can be converted to string via
     * Object.toString()) into a single string with fields separated by sep.
     * @param sep the separator
     * @param values collection of objects, null is converted to the
     *  empty string
     * @return null if values is null. The joined string otherwise.
     */
    public static String join(String sep, Collection<?> values) {
        if (sep == null)
            throw new IllegalArgumentException();
        if (values == null)
            return null;
        if (values.isEmpty())
            return "";
        StringBuilder s = null;
        for (Object a : values) {
            if (a == null) {
                a = "";
            }
            if(s != null) {
                s.append(sep).append(a.toString());
            } else {
                s = new StringBuilder(a.toString());
            }
        }
        return s.toString();
    }

    /**
     * convert Color to String
     * (Color.toString() omits alpha value)
     */
    public static String toString(Color c) {
        if (c == null)
            return "null";
        if (c.getAlpha() == 255)
            return String.format("#%06x", c.getRGB() & 0x00ffffff);
        else
            return String.format("#%06x(alpha=%d)", c.getRGB() & 0x00ffffff, c.getAlpha());
    }
}
