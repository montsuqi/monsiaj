/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.montsuqi.monsiaj.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import org.montsuqi.monsiaj.monsia.Interface;
import org.montsuqi.monsiaj.widgets.TopWindow;
import org.montsuqi.monsiaj.widgets.Window;

/**
 *
 * @author yusuke mihara
 */
public class TestGlade {

    public static void main(String[] args) {
        try {
            TopWindow topWindow = new TopWindow();
            File gladeFile = new File(args[0]);
            InputStream input = new FileInputStream(gladeFile);
            Interface xml = Interface.parseInput(input);
            String fname = gladeFile.getName();
            Window window = (Window)xml.getWidget(fname.substring(0, fname.indexOf(".")));
            topWindow.setXml(xml);
            topWindow.showWindow(window);
            topWindow.ReScale();
            topWindow.validate();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
