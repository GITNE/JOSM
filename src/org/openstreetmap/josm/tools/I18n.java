// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.tools;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Vector;

/**
 * Internationalisation support.
 *
 * @author Immanuel.Scholz
 */
public class I18n {

    /* Base name for translation data. Used for detecting available translations */
    private static final String TR_BASE = "org.openstreetmap.josm.i18n.Translation_";

    /**
     * Set by MainApplication. Changes here later will probably mess up everything, because
     * many strings are already loaded.
     */
    public static org.xnap.commons.i18n.I18n i18n;

    public static final String tr(String text, Object... objects) {
        if (i18n == null)
            return MessageFormat.format(text, objects);
        return i18n.tr(text, objects);
    }

    public static final String tr(String text) {
        if (i18n == null)
            return text;
        return i18n.tr(text);
    }

    public static final String marktr(String text) {
        return text;
    }

    public static final String trn(String text, String pluralText, long n, Object... objects) {
        if (i18n == null)
            return n == 1 ? tr(text, objects) : tr(pluralText, objects);
        return i18n.trn(text, pluralText, n, objects);
    }

    public static final String trn(String text, String pluralText, long n) {
        if (i18n == null)
            return n == 1 ? tr(text) : tr(pluralText);
        return i18n.trn(text, pluralText, n);
    }

    /**
     * Get a list of all available JOSM Translations.
     * @return an array of locale objects.
     */
    public static final Locale[] getAvailableTranslations() {
        Vector<Locale> v = new Vector<Locale>();
        Locale[] l = Locale.getAvailableLocales();
        for (int i = 0; i < l.length; i++) {
            String cn = TR_BASE + l[i];
            try {
                Class.forName(cn);
                v.add(l[i]);
            } catch (ClassNotFoundException e) {
            }
        }
        l = new Locale[v.size()];
        l = v.toArray(l);
        Arrays.sort(l, new Comparator<Locale>() {
            public int compare(Locale o1, Locale o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        return l;
    }
}
