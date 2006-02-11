/*      PANDA -- a simple transaction monitor

Copyright (C) 1998-1999 Ogochan.
              2000-2003 Ogochan & JMA (Japan Medical Association).

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

package org.montsuqi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

public class SystemEnvironment {

	private static final Logger logger;

	static {
		logger = Logger.getLogger(SystemEnvironment.class);
	}

	private SystemEnvironment() {
		// inhibit instantiation
	}

	public static boolean isMacOSX() {
		return System.getProperty("os.name").toLowerCase().startsWith("mac os x"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static boolean isWindows() {
		String osName = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
		return osName.startsWith("windows"); //$NON-NLS-1$
	}

	private final static boolean isMS932;
	static {
		if (Locale.getDefault().getLanguage().equals("ja")) { //$NON-NLS-1$
			isMS932 = isWindows();
		} else {
			isMS932 = false;
		}
	}

	public static boolean isMS932() {
		return isMS932;
	}

	public static void setMacMenuTitle(String title) {
		if (title != null && isMacOSX()) {
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", title); //$NON-NLS-1$
		}
	}

	public static File createFilePath(String[] elements) {
		File path = new File(elements[0]);
		for (int i = 1; i < elements.length; i++) {
			path = new File(path, elements[i]);
		}
		return path;
	}

	public static boolean getUseBrowserSetting() {
		if ( ! isWindows()) {
			return false;
		}
		Properties deploymentProperties = new Properties();
		String home = System.getProperty("user.home");
		File deploymentDirectory = createFilePath(new String[] {
			home, "Application Data", "Sun", "Java", "Deployment"
		});
		File deploymentPropertiesFile = new File(deploymentDirectory, "deployment.properties");
		try {
			FileInputStream fis = new FileInputStream(deploymentPropertiesFile);
			deploymentProperties.load(fis);
			return ! "false".equals(deploymentProperties.getProperty("deployment.security.browser.keystore.use"));
		} catch (FileNotFoundException e) {
			logger.info("{0} not fould", deploymentPropertiesFile);
			return false;
		} catch (IOException e) {
			logger.info("{0} could not be read", deploymentPropertiesFile);
			return false;
		}
	}

	public static File getTrustStorePath() {
		String home = System.getProperty("user.home");
		if (isWindows()) {
			File deploymentDirectory = createFilePath(new String[] {
				home, "Application Data", "Sun", "Java", "Deployment"
			});
			File securityDirectory = new File(deploymentDirectory, "security");
			return new File(securityDirectory, "trusted.jssecacerts");
		} else if (isMacOSX()){
			File path = createFilePath(new String[] {
				home, "Library", "Caches", "Java Applets", "security"	
			});
			return new File(path, "deployment.certs");
		} else {
			File path = createFilePath(new String[] {
				home, ".java", "deployment", "security"
			});
			return new File(path, "trusted.cacerts");
		}
	}
}
