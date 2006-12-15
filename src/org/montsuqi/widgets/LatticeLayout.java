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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** <p>A layout manager that lays out component in lattices(grids).</p>
 * <p><em>Acknowledgement:
 * This class is heavily inspired from
 * <a href="http://www5.airnet.ne.jp/sakuraba/java/softwares/library/lattice/latticelayout.html">Sakuraba's
 * LatticeLayout</a>.</em></p>
 */
public class LatticeLayout implements LayoutManager2, Serializable {

	private Map components;
	private int horizontalLattices = 1;
	private int verticalLattices = 1;

	private static final int PREFERRED_SIZE = 0;
	private static final int MINIMUM_SIZE = 1;
	private static final int MAXIMUM_SIZE = 2;

	private final LatticeConstraints defaultConstraints = new LatticeConstraints();

	public LatticeLayout() {
		this(1, 1);
	}

	public LatticeLayout(int h, int v) {
		components = new HashMap();
		horizontalLattices = h;
		verticalLattices = v;
	}

	public void addLayoutComponent(String name, Component comp) {
		throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$
	}

	public void addLayoutComponent(Component comp, Object constraints) {
		if (constraints instanceof LatticeConstraints) {
			setConstraints(comp, (LatticeConstraints)constraints);
		} else if (constraints != null) {
			throw new IllegalArgumentException("cannot add to layout: constraints must be a LatticeConstraint"); //$NON-NLS-1$
		}
	}

	public void removeLayoutComponent(Component comp) {
		components.remove(comp);
	}

	public void setConstraints(Component comp, LatticeConstraints constraints) {
		components.put(comp, constraints.clone());
		// automatically increaces lattice count
		horizontalLattices = Math.max(horizontalLattices, constraints.x + constraints.width);
		verticalLattices = Math.max(verticalLattices, constraints.y + constraints.height);
	}

	public LatticeConstraints getConstraints(Component comp) {
		LatticeConstraints constraints = lookupConstraints(comp);
		return (LatticeConstraints)constraints.clone();
	}

	protected LatticeConstraints lookupConstraints(Component comp) {
		LatticeConstraints constraints = (LatticeConstraints)components.get(comp);
		if (constraints == null) {
			setConstraints(comp, defaultConstraints);
			constraints = (LatticeConstraints)components.get(comp);
		}
		return constraints;
	}

	private Dimension getComponentLayoutSize(Component comp, int preferred) {
		if (preferred == PREFERRED_SIZE) {
			return comp.getPreferredSize();
		} else if (preferred == MINIMUM_SIZE) {
			return comp.getMinimumSize();
		} else {
			return comp.getMaximumSize();
		}
	}

	private Dimension getLayoutSize(Container parent, int preferred) {
		Dimension size = new Dimension(0, 0);
		for (int i = 0, n = parent.getComponentCount(); i < n; i++) {
			Component c = parent.getComponent(i);
			if (c.isVisible()) {
				Dimension d = getComponentLayoutSize(c, preferred);
				size.width = Math.max(d.width, size.width);
				size.height = Math.max(d.height, size.height);
			}
		}
		size.width = size.width * horizontalLattices;
		size.height = size.height * verticalLattices;
		return size;
	}

	public Dimension preferredLayoutSize(Container parent) {
		return getLayoutSize(parent, PREFERRED_SIZE);
	}

	public Dimension minimumLayoutSize(Container parent) {
		return getLayoutSize(parent, MINIMUM_SIZE);
	}

	public Dimension maximumLayoutSize(Container parent) {
		return getLayoutSize(parent, MAXIMUM_SIZE);
	}

	public float getLayoutAlignmentX(Container parent) {
		return 0.5f;
	}

	public float getLayoutAlignmentY(Container parent) {
		return 0.5f;
	}

	public void invalidateLayout(Container target) {
		/* do nothing */
	}

	public void layoutContainer(Container parent) {
		Dimension parentSize = parent.getSize();
		Insets insets = parent.getInsets();

		double latticeWidth = (double)(parentSize.width - insets.left - insets.right) / horizontalLattices;
		double latticeHeight = (double)(parentSize.height - insets.top - insets.bottom) / verticalLattices;

		for (int i = 0, n = parent.getComponentCount(); i < n; i++) {
			Component c = parent.getComponent(i);
			Dimension pref = c.getPreferredSize();
			LatticeConstraints lc = lookupConstraints(c);

			int x = (int)(lc.x * latticeWidth) + lc.left + insets.left;
			int y = (int)(lc.y * latticeHeight) + lc.top + insets.top;
			int w = (int)(lc.width * latticeWidth) - lc.left - lc.right;
			int h = (int)(lc.height * latticeHeight) - lc.top - lc.bottom;

			if (w > pref.width) {
				switch (lc.fill) {
				case LatticeConstraints.NONE:
				case LatticeConstraints.VERTICAL:
					switch (lc.halign) {
					case LatticeConstraints.CENTER:
						x += (w - pref.width) / 2;
						break;
					case LatticeConstraints.RIGHT:
						x += w - pref.width;
						break;
					}
					w = pref.width;
					break;
				}
			} else {
				switch(lc.shrink) {
				case LatticeConstraints.NONE:
				case LatticeConstraints.VERTICAL:
					w = pref.width;
					break;
				}
			}

			if (h > pref.height) {
				switch(lc.fill) {
				case LatticeConstraints.NONE:
				case LatticeConstraints.HORIZONTAL:
					switch (lc.valign) {
					case LatticeConstraints.CENTER:
						y += (h - pref.height) / 2;
						break;
					case LatticeConstraints.BOTTOM:
						y += h - pref.height;
						break;
					}
					h = pref.height;
					break;
				}
			} else {
				switch(lc.shrink) {
				case LatticeConstraints.NONE:
				case LatticeConstraints.HORIZONTAL:
					h = pref.height;
					break;
				}
			}
			if (x + w < 0 || parentSize.width <= x + w ||
				y + h < 0 || parentSize.height <= y + h) {
				c.setBounds(0, 0, 0, 0);
			} else {
				c.setBounds(x, y, w, h);
			}
		}
	}

	public void setVerticalLattices(int v) {
		verticalLattices = v;
	}

	public void setHorizontalLattices(int h) {
		horizontalLattices = h;
	}
}
