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

/** Acknowledgement:
 * This class is heavily inspired from Sakuraba's LatticeLayout.
 * http://www5.airnet.ne.jp/sakuraba/java/softwares/library/lattice/latticelayout.html
 */

package org.montsuqi.widgets;

import java.awt.Insets;
import java.io.Serializable;

/** <p>Layout constraints used in LatticeLayout.</p>
 */
public class LatticeConstraints implements Cloneable, Serializable {
	public int x;
	public int y;
	public int width;
	public int height;
	public int halign;
	public int valign;
	public int fill;
	public int shrink;
	public int left;
	public int right;
	public int top;
	public int bottom;
	public final static int NONE = 0;
	public final static int VERTICAL = 1;
	public final static int HORIZONTAL = 2;
	public final static int BOTH = 3;
	public final static int CENTER = 0;
	public final static int TOP = 1;
	public final static int BOTTOM = 2;
	public final static int LEFT = 3;
	public final static int RIGHT = 4;

	public LatticeConstraints() {
		this(0, 0, 1, 1, CENTER, CENTER, NONE, NONE, 0, 0, 0, 0);
	}

	public LatticeConstraints(int x, int y, int width, int height) {
		this(x, y, width, height, CENTER, CENTER, NONE, NONE, 0, 0, 0, 0);
	}

	public LatticeConstraints(int x, int y, int width, int height, Insets insets) {
		this(x, y, width, height, CENTER, CENTER, NONE, NONE, insets.top, insets.left, insets.bottom, insets.right);
	}

	public LatticeConstraints(int x, int y, int width, int height, int fill, int adjust, Insets insets) {
		this(x, y, width, height, CENTER, CENTER, fill, adjust, insets.top, insets.left, insets.bottom, insets.right);
	}

	public LatticeConstraints(int x, int y, int width, int height, int halign, int valign, int fill, int adjust, Insets insets) {
		this(x, y, width, height, valign, halign, fill, adjust, insets.top, insets.left, insets.bottom, insets.right);
	}

	public LatticeConstraints(int x, int y, int width, int height, int halign, int valign, int fill, int adjust, int top, int left, int bottom, int right) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.halign = halign;
		this.valign = valign;
		this.fill = fill;
		this.shrink = adjust;
		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;
	}

	public Object clone () {
		try { 
			LatticeConstraints c = (LatticeConstraints)super.clone();
			return c;
		} catch (CloneNotSupportedException e) { 
			throw new InternalError();
		}
	}
}
