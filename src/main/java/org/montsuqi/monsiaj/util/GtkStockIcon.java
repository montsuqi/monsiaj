/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.org.montsuqi.monsiaj.util;

import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 *
 * @author mihara
 */
public class GtkStockIcon {
    
    static public Icon get(final String iconName) {
        URL url = GtkStockIcon.class.getResource("/org/montsuqi/widgets/images/stock-icons/" + iconName + ".png");
        if (url == null) {
            return null;
        }
        return new ImageIcon(url);
    }
}
