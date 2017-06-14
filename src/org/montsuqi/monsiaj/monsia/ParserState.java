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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;

/** <p>State ofject for interface definition parser(SAX Handler)s.</p>
 */
abstract class ParserState {

	protected static final Logger logger = LogManager.getLogger(ParserState.class);

	/** <p>Constructs a state with given <var>name</var>.</p>
	 * 
	 * @param name name of a parser state.
	 */
	ParserState(String name) {
		this.name = name;
	}

	/** <p>Receives notification of the beginning of an element.</p>
	 * @param uri the namespace URI if any.
	 * @param localName the element's locale name (name without prefix).
	 * @param qName the element's qualified name (name with prefix).
	 * @param attrs attributes of the element.
	 */
	abstract void startElement(String uri, String localName, String qName, Attributes attrs);

	/** <p>Receives notification of the end of an element.</p>
	 * @param uri the namespace URI if any.
	 * @param localName the element's locale name (name without prefix).
	 * @param qName the element's qualified name (name with prefix).
	 */
	abstract void endElement(String uri, String localName, String qName);

	private final String name;


	/** <p>A helper method to warn that the parser finds an unexpected element closing tag.</p>
	 * @param element expected element to close.
	 * @param found acutual element name of the closing tag encountered.
	 */
	protected void warnShouldFindClosing(String element, String found) {
		Object[] args = { element, found };
		logger.warn("should find </{0}> here, found </{1}>", args); 
	}

	/** <p>A helper method to warn that some element found in an element which should be
	 * empty.</p>
	 * @param element current element, which should be empty.
	 * @param found an element found in <var>element</var>.
	 */
	protected void warnShouldBeEmpty(String element, String found) {
		Object[] args = { element, found };
		logger.warn("<{0}> element should be empty, found <{1}>", args); 
	}

	/** <p>A helper method to warn that an element which should have no attributes detects
	 * some attributes specified.</p>
	 * 
	 * @param element current element.
	 */
	protected void warnShouldHaveNoAttributes(String element) {
		logger.warn("<{0}> should have no attributes", element); 
	}

	/** <p>A helper method to warn that wrong type of property is being set at the current
	 * position.</p>
	 * @param element current element.
	 */
	protected void warnInvalidPropertiesDefinedHere(String element) {
		logger.warn("non {0} properties defined here", element); 
	}

	/** <p>Returns the name of this state.</p>
	 * 
	 * @return name of this state.
	 */
	public String getName() {
		return name;
	}
}
