/*
 * monsiaj
 * org.montsuqi.client.Messages
 * Copyright (C) 2003 crouton
 *
 * $Id: Messages.java,v 1.1 2003-09-16 18:20:38 ozawa Exp $
 */
package org.montsuqi.client;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author crouton
 */
public class Messages {

	private static final String BUNDLE_NAME = "org.montsuqi.pandaclient"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE =
		ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * 
	 */
	private Messages() {
	}
	/**
	 * @param key
	 * @return
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
