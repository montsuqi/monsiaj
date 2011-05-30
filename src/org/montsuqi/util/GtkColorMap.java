/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.montsuqi.util;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author mihara
 */
public class GtkColorMap {

    static final private HashMap<String, Color> colorMap = new HashMap<String, Color>();

    static {
        try {
            final ClassLoader loader = GtkColorMap.class.getClassLoader();
            final InputStream is = loader.getClass().getResourceAsStream("/org/montsuqi/widgets/rgb.txt");
            if (is == null) {
                throw new IOException("");
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
            os.close();

            Pattern pcolor = Pattern.compile("^\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\t+(.*)$");
            String[] lines = os.toString().split("\n");
            for (String line : lines) {
                if (line.startsWith("!")) {
                    continue;
                }
                Matcher mcolor = pcolor.matcher(line);
                if (mcolor.find()) {
                    Color color = new Color(
                            Integer.parseInt(mcolor.group(1)),
                            Integer.parseInt(mcolor.group(2)),
                            Integer.parseInt(mcolor.group(3)));
                    colorMap.put(mcolor.group(4), color);
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
    static public Color getColor(String colorName) {
        Color color = colorMap.get(colorName);
        if (color == null) {
            color = Color.WHITE;
        }
        return color;
    }
}
