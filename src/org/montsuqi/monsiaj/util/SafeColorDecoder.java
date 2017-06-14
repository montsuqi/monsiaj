/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.montsuqi.monsiaj.util;

import java.awt.Color;

/**
 *
 * @author mihara
 */
public class SafeColorDecoder {
    public static Color decode(String str) {
        try {
            return Color.decode(str);
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }
        return null;
    }
}
