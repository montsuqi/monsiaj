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

package org.montsuqi.widgets;

import java.awt.Color;
import java.io.PrintStream;
import javax.swing.JTextPane;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/** <p>A TextPane subclass which provides output/error output streams to write on.</p>
 *
 * <p>You can obtain output and error output streams by getOut() and getErr()
 * respectively. After obtaining them, you can print on it as usual.</p>
 * 
 */
public class ConsolePane extends JTextPane {

	private TextPaneOutputStream conOut;
	private TextPaneOutputStream conErr;
	private PrintStream out;
	private PrintStream err;

	/** Constructs a ConsolePane instance.</p>
	 */
	public ConsolePane() {
		super();
		setEditable(false);
		initStreams();
	}

	/** <p>Constructs a ConsolePane instance with the given document.</p>
	 * 
	 * @param doc an instance of StyledDocument.
	 */
	public ConsolePane(StyledDocument doc) {
		super(doc);
		setEditable(false);
		initStreams();
	}

	/** <p>Obtains the attribute set used for the output stream.</p>
	 * <p>You can change its attributes to change the style of text printed
	 * to the output stream.</p>
	 * @return the attribute set.
	 */
	public MutableAttributeSet getOutAttributeSet() {
		return conOut.getAttributeSet();
	}

	/** <p>Obtains the attribute set used for the error output stream.</p>
	 * <p>You can change its attributes to change the style of text printed
	 * to the error output stream.</p>
	 * @return the attribute set.
	 */
	public MutableAttributeSet getErrAttributeSet() {
		return conErr.getAttributeSet();
	}

	private void initStreams() {
		conOut = new TextPaneOutputStream(this);
		StyleConstants.setForeground(getOutAttributeSet(), Color.BLUE);
		out = new PrintStream(conOut);

		conErr = new TextPaneOutputStream(this);
		StyleConstants.setForeground(getErrAttributeSet(), Color.RED);
		err = new PrintStream(conErr);
	}

	/** <p>Obtains the output stream.</p>
	 * 
	 * @return the output stream(instance of TextPaneOutputStream).
	 */
	public PrintStream getOut() {
		return out;
	}

	/** <p>Obtains the error output stream.</p>
	 * 
	 * @return the error output stream(instance of TextPaneOutputStream).
	 */
	public PrintStream getErr() {
		return err;
	}
}
