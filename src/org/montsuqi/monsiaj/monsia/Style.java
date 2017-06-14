/*      PANDA -- a simple transaction monitor

 Copyright (C) 1998-1999 Ogochan.
 2000-2003 Ogochan & JMA (Japan Medical Association).
 2002-2006 OZAWA Sakuro.

 This module is part of PANDA.

 PANDA is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY.  No author or distributor accepts responsibility
 to anyone for the consequences of using it or for whether it serves
 any particular purpose or works at all, unless he says so in writing.
 Refer to the GNU General Public License for full details.

 Everyone is granted permission to copy, modify and redistribute
 PANDA, but only under the conditions described in the GNU General
 Public License.  A copy of this license is supposed to have been given
 to you along with PANDA so you can know your rights and
 responsibilities.  It should be in a file named COPYING.  Among other
 things, the copyright notice and this notice must be preserved on all
 copies.
 */
package org.montsuqi.monsiaj.monsia;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.SystemColor;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.montsuqi.monsiaj.util.SafeColorDecoder;

public class Style {

    public static final Style DEFAULT_STYLE;

    static {
        DEFAULT_STYLE = new Style(null);
    }
    private final String name;
    private Color background;
    private Color foreground;
    private Font font;

    private Style(String name) {
        this.name = name;
        foreground = SystemColor.textText;
        background = SystemColor.text;
    }

    public void apply(Component widget) {
        if (font != null) {
            widget.setFont(font);
        }
        if (foreground != null) {
            widget.setForeground(foreground);
        }
        if (background != null) {
            widget.setBackground(background);
        }
    }

    public static Map load(InputStream in) throws IOException {
        Map<String, Style> styles = new HashMap<>();
        Properties props = new Properties();
        props.load(in);
        for (Map.Entry e : props.entrySet()) {
            String name = (String) e.getKey();
            String value = (String) e.getValue();
            int dot = name.indexOf('.');
            if (dot < 1) {
                continue;
            }
            String styleName = name.substring(0, dot);
            String attribute = name.substring(dot + 1);
            Style style;
            if (!styles.containsKey(styleName)) {
                style = new Style(styleName);
                styles.put(styleName, style);
            } else {
                style = (Style) styles.get(styleName);
            }
            if ("font".equals(attribute)) {
                Font font = Font.decode(value);
                style.setFont(font);
            } else if ("foreground".equals(attribute) || "fg".equals(attribute)) {  //$NON-NLS-2$
                Color fg = SafeColorDecoder.decode(value);
                style.setForeground(fg);
            } else if ("background".equals(attribute) || "bg".equals(attribute)) {  //$NON-NLS-2$
                Color bg = SafeColorDecoder.decode(value);
                style.setBackground(bg);
            }
        }
        return styles;
    }

    private void setFont(Font font) {
        this.font = font;
    }

    private void setForeground(Color fg) {
        this.foreground = fg;
    }

    private void setBackground(Color bg) {
        this.background = bg;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("style[");
        buf.append(name);
        buf.append(": foreground=");
        buf.append(foreground);
        buf.append(", background=");
        buf.append(background);
        buf.append(", font=");
        buf.append(font);
        buf.append("]");
        return buf.toString();
    }
}
