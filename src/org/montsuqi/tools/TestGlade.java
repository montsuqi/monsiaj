/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.montsuqi.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import org.montsuqi.monsia.Interface;
import org.montsuqi.widgets.Window;

/**
 *
 * @author yusuke mihara
 */
public class TestGlade {
	public static String getPreffix(String fileName) {
		if (fileName == null) {
			return null;
		}
		int point = fileName.lastIndexOf(".");
		if (point != -1) {
			return fileName.substring(0, point);
		}
		return fileName;
	}
	
	public static void main(String[] args)
	{
		Window[] windows = new Window[args.length];
		for (int i = 0; i < args.length; i++) {
			try {
				Interface iface;
				File gladeFile = new File(args[i]);
				InputStream input = new FileInputStream(gladeFile);
				String wname = getPreffix(gladeFile.getName());
				iface = Interface.parseInput(input);
				windows[i] = (Window) iface.getWidget(wname);
				windows[i].setVisible(true);
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}