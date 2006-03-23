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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
	private static final Map ENTITY_REPLACEMENTS = new HashMap();
	static {
		ENTITY_REPLACEMENTS.put("\"", "&dquot;");
		ENTITY_REPLACEMENTS.put("<", "&lt;");
		ENTITY_REPLACEMENTS.put(">", "&gt;");
		ENTITY_REPLACEMENTS.put("&", "&amp;");
	}
	private static final Pattern ENTITY_PATTERN = Pattern.compile("[\"<>&]");

	public static String escapeHTML(String src) {
		Matcher m = ENTITY_PATTERN.matcher(src);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			final String matched = m.group();
			final String replacement;
			if (ENTITY_REPLACEMENTS.containsKey(matched)) {
				replacement = (String)ENTITY_REPLACEMENTS.get(matched);
			} else {
				replacement = null;
			}
			m.appendReplacement(sb, replacement);
		}
		m.appendTail(sb);
		return sb.toString();
	}
}
