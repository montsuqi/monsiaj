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

package org.montsuqi.monsia;

import org.montsuqi.util.Logger;
import org.xml.sax.Attributes;

abstract class ParserState {

	private Logger logger;

	ParserState(String name) {
		this.name = name;
		logger = Logger.getLogger(ParserState.class);
	}

	abstract void startElement(String uri, String localName, String qName, Attributes attrs);
	abstract void endElement(String uri, String localName, String qName);

	private final String name;

	protected void warnShouldFindClosing(String element, String found) {
		Object[] args = { element, found };
		logger.warn("should find </{0}> here, found </{1}>", args); //$NON-NLS-1$
	}

	protected void warnShouldBeEmpty(String element, String found) {
		Object[] args = { element, found };
		logger.warn("<{0}> element should be empty, found <{1}>", args); //$NON-NLS-1$
	}

	protected void warnShouldHaveNoAttributes(String element) {
		logger.warn("<{0}> should have no attributes", element); //$NON-NLS-1$
	}

	protected void warnInvalidPropertiesDefinedHere(String element) {
		logger.warn("non {0} properties defined here", element); //$NON-NLS-1$
	}

	public String getName() {
		return name;
	}
}
