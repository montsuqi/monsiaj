package org.montsuqi.widets;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;

public class TableLayoutOld implements LayoutManager2 {

	Map comps;
	TableInfo[] rowInfos;
	TableInfo[] columnInfos;
	int rows;
	int columns;
	int rowSpacing;
	int columnSpacing;
	boolean homogeneous;
	boolean invalid;

	public TableLayout(int rows, int columns, int rowSpacing, int columnSpacing,
					   boolean homogeneous) {
		comps = new HashMap();
		rowInfos = null;
		columnInfos = null;
		this.rows = 0;
		this.columns = 0;
		this.rowSpacing = rowSpacing;
		this.columnSpacing = columnSpacing;
		this.homogeneous = homogeneous;
		invalid = true;
		resize(rows, columns);
	}
	
	public TableLayout(int rows, int columns, int rowSpacing, int columnSpacing) {
		this(rows, columns, rowSpacing, columnSpacing, false);
	}

	public TableLayout(int rows, int columns) {
		this(rows, columns, 0, 0, false);
	}

	public TableLayout() {
		this(1, 1, 0, 0, false);
	}

	public void addLayoutComponent(String name, Component comp) {
		throw new UnsupportedOperationException(
			"addLayoutComponent(String, Component) is not supported."
			);
	}

	public void addLayoutComponent(Component comp, Object object) {
		if (object instanceof TableConstraints) {
			setConstraints(comp, (TableConstraints)object);
		} else if (object != null) {
			throw new IllegalArgumentException("Unacceptable constraints type.");
		}
	}
	
	public void setConstraints(Component comp, TableConstraints constraints) {
		if (constraints.leftAttach >= constraints.rightAttach) {
			throw new IllegalArgumentException("Wrong constraints.");
		}
		if (constraints.topAttach >= constraints.bottomAttach) {
			throw new IllegalArgumentException("Wrong constraints.");
		}
		if (constraints.rightAttach >= columns) {
			resize(rows, constraints.rightAttach);
		}
		if (constraints.bottomAttach >= rows) {
			resize(constraints.bottomAttach, columns);
		}
		comps.put(comp, (TableConstraints)(constraints.clone()));
	}

	public void resize(int newRows, int newColumns) {
		if (newRows <= 0 || 65536 <= newRows) {
			throw new IllegalArgumentException("Resize out of range.");
		}
		if (newColumns <= 0 || 65536 <= newColumns) {
			throw new IllegalArgumentException("Resize out of range.");
		}
		newRows = Math.max(1, newRows);
		newColumns = Math.max(1, newColumns);

		if (newRows != rows || newColumns != columns) {
			Iterator it = comps.values().iterator();
			while (it.hasNext()) {
				TableConstraints constraints = (TableConstraints)it.next();
				newRows = Math.max(newRows, constraints.bottomAttach);
				newColumns = Math.max(newColumns, constraints.rightAttach);
			}
			if (newRows != rows) {
				int i;
				i = rows;
				rows = newRows;
				rowInfos = new TableInfo[rows];
				for (; i < rows; i++) {
					rowInfos[i] = new TableInfo(rowSpacing);
				}
			}
			if (newColumns != columns) {
				int i;
				i = columns;
				columns = newColumns;
				columnInfos = new TableInfo[columns];
				for (; i < columns; i++) {
					columnInfos[i] = new TableInfo(columnSpacing);
				}
			}
		}
	}

	public void layoutContainer(Container parent) {
		System.out.println("layoutContainer");
		preferredLayoutSize(parent);
		layoutInit();
		layoutPass1(parent);
		layoutPass2(parent);
	}

	protected void layoutInit() {
		// initialize the rows and cols.
		// by default rows and cols do not expand and do shrink.
		// Those values are modified by the children that occupy the
		// rows and cols.
		for (int col = 0; col < columns; col++) {
			columnInfos[col].alloc = columnInfos[col].req;
			columnInfos[col].needExpand = false;
			columnInfos[col].needShrink = true;
			columnInfos[col].expand = false;
			columnInfos[col].shrink = true;
			columnInfos[col].empty = true;
		}
		for (int row = 0; row < rows; row++) {
			rowInfos[row].alloc = rowInfos[row].req;
			rowInfos[row].needExpand = false;
			rowInfos[row].needShrink = true;
			rowInfos[row].expand = false;
			rowInfos[row].shrink = true;
			rowInfos[row].empty = true;
		}

		Iterator i;
		// loop over all the children and adjust the row and col
		// values based on whether the children want to be allowd to
		// expand or shrink. This loop handles children that occupy a
		// single row or column 
		i = comps.keySet().iterator();
		while (i.hasNext()) {
			Component comp = (Component)i.next();
			TableConstraints constraints = (TableConstraints)comps.get(comp);
			if (comp.isVisible()) {
				System.out.println("comp is visible");
				if (constraints.leftAttach == constraints.rightAttach - 1) {
					if (constraints.xExpand) {
						columnInfos[constraints.leftAttach].expand = true;
					}
					if ( ! constraints.xShrink) {
						columnInfos[constraints.leftAttach].shrink = false;
					}
					columnInfos[constraints.leftAttach].empty = false;
				}
				if (constraints.topAttach == constraints.bottomAttach - 1) {
					if (constraints.yExpand) {
						rowInfos[constraints.topAttach].expand = true;
					}
					if ( ! constraints.yShrink) {
						rowInfos[constraints.topAttach].shrink = false;
					}
					rowInfos[constraints.topAttach].empty = false;
				}
			}
		}
		// loop over all the children again and this time handle
		// children wich span multiple rows or columns.
		i = comps.keySet().iterator();
		while (i.hasNext()) {
			Component comp = (Component)i.next();
			TableConstraints constraints = (TableConstraints)comps.get(comp);
			if (comp.isVisible()) {
				if (constraints.leftAttach != constraints.rightAttach - 1) {
					for (int col = constraints.leftAttach; col < constraints.rightAttach; col++) {
						columnInfos[col].empty = false;
					}
					if (constraints.xExpand) {
						boolean hasExpand = false;
						for (int col = constraints.leftAttach; col < constraints.rightAttach; col++) {
							if (columnInfos[col].expand) {
								hasExpand = true;
								break;
							}
						}
						if ( ! hasExpand) {
							for (int col = constraints.leftAttach; col < constraints.rightAttach; col++) {
								columnInfos[col].needExpand = true;
							}
						}
					}
					if ( ! constraints.xShrink) {
						boolean hasShrink = true;
						for (int col = constraints.leftAttach; col < constraints.rightAttach; col++) {
							if ( ! columnInfos[col].shrink) {
								hasShrink = false;
								break;
							}
						}
						if (hasShrink) {
							for (int col = constraints.leftAttach; col < constraints.rightAttach; col++) {
								columnInfos[col].needShrink = false;
							}
						}
					}
				}
				if (constraints.topAttach != constraints.bottomAttach - 1) {
					for (int row = constraints.topAttach; row < constraints.bottomAttach; row++) {
						rowInfos[row].empty = false;
					}
					if (constraints.yExpand) {
						boolean hasExpand = false;
						for (int row = constraints.topAttach; row < constraints.bottomAttach; row++) {
							if (rowInfos[row].expand) {
								hasExpand = true;
								break;
							}
						}
						if ( ! hasExpand) {
							for (int row = constraints.topAttach; row < constraints.bottomAttach; row++) {
								rowInfos[row].needExpand = true;
							}
						}
					}
					if ( ! constraints.yShrink) {
						boolean hasShrink = true;
						for (int row = constraints.topAttach; row < constraints.bottomAttach; row++) {
							if ( ! rowInfos[row].shrink) {
								hasShrink = false;
								break;
							}
						}
						if (hasShrink) {
							for (int row = constraints.topAttach; row < constraints.bottomAttach; row++) {
								rowInfos[row].needShrink = false;
							}
						}
					}
				}
			}
		}

		// loop over the columns and set the expand and shrink values
		// if the column can be expanded or shrunk.
		for (int col = 0; col < columns; col++) {
			if (columnInfos[col].empty) {
				columnInfos[col].expand = false;
				columnInfos[col].shrink = false;
			} else {
				if (columnInfos[col].needExpand) {
					columnInfos[col].expand = true;
				}
				if ( ! columnInfos[col].needShrink) {
					columnInfos[col].shrink = false;
				}
			}
		}

		// loop over the rows and set the expand and shrink values if
		// the row can be expanded or shrunk.
		for (int row = 0; row < rows; row++) {
			if (rowInfos[row].empty) {
				rowInfos[row].expand = false;
				rowInfos[row].shrink = false;
			} else {
				if (rowInfos[row].needExpand) {
					rowInfos[row].expand = true;
				}
				if ( ! rowInfos[row].needShrink) {
					rowInfos[row].shrink = false;
				}
			}
		}
	}

	protected void layoutPass1(Container parent) {
		Dimension alloc = parent.getSize();
		Dimension real = new Dimension();
		Insets insets = parent.getInsets();

		// if we were allocated more space than we requested then we
		// have to expand any expandable rows and columns to fill in
		// the extra space.
		real.width = alloc.width - insets.left - insets.right;
		real.height = alloc.height - insets.top - insets.bottom;
		System.out.println("alloc=" + alloc);
		System.out.println("real=" + real);
		int nExpand;
		int nShrink;
		int width;
		int height;
		if (homogeneous) {
			if (comps.isEmpty()) {
				nExpand = 1;
			} else {
				nExpand = 0;
				for (int col = 0; col < columns; col++) {
					if (columnInfos[col].expand) {
						nExpand += 1;
						break;
					}
				}
			}
			if (nExpand != 0) {
				width = real.width;
				System.out.println("width=" + width);
				for (int col = 0; col + 1 < columns; col++) {
					width -= columnInfos[col].spacing;
				}
				System.out.println("width=" + width);
				for (int col = 0; col < columns; col++) {
					int extra = width / (columns - col);
					columnInfos[col].alloc = Math.max(1, extra);
					System.out.println("columnInfos[" + col + "]=" + columnInfos[col]);
					width -= extra;
				}
				System.out.println("width=" + width);
			}
		} else {
			width = 0;
			nExpand = 0;
			nShrink = 0;
			for (int col = 0; col < columns; col++) {
				System.out.println(columnInfos[col]);
				width += columnInfos[col].req;
				if (columnInfos[col].expand) {
					nExpand += 1;
				}
				if (columnInfos[col].shrink) {
					nShrink += 1;
				}
			}
			System.out.println("width=" + width + ", nExpand=" + nExpand + ", nShrink=" + nShrink);
			for (int col = 0; col + 1 < columns; col++) {
				width += columnInfos[col].spacing;
			}
			System.out.println("width=" + width);
			// check to see if we were allocated more width than we requested.
			if (width < real.width && nExpand >= 1) {
				width = real.width - width;
				System.out.println(". width=" + width);
				for (int col = 0; col < columns; col++) {
					if (columnInfos[col].expand) {
						int extra = width / nExpand;
						columnInfos[col].alloc += extra;
						width -= extra;
						nExpand -= 1;
						System.out.println("..width=" + width);
					}
				}
			}
			// check to see if we were allocated less width than we
			// requested. then shrink until we fit the size given.
			System.out.println("width=" + width + ", real.width=" + real.width);
			if (width > real.width) {
				int totalNShrink = nShrink;
				int extra = width - real.width;
				while (totalNShrink > 0 && extra > 0) {
					nShrink = totalNShrink;
					for (int col = 0; col < columns; col++) {
						if (columnInfos[col].shrink) {
							int al = columnInfos[col].alloc;
							columnInfos[col].alloc =
								Math.max(1, (int)columnInfos[col].alloc - extra / nShrink);
							extra -= al - columnInfos[col].alloc;
							nShrink -= 1;
							if (columnInfos[col].alloc < 2){
								totalNShrink -= 1;
								columnInfos[col].shrink = false;
							}
						}
					}
				}
			}
		}
		if (homogeneous) {
			if (comps.size() < 1) {
				nExpand = 1;
			} else {
				nExpand = 0;
				for (int row = 0; row < rows; row++) {
					if (rowInfos[row].expand) {
						nExpand += 1;
						break;
					}
				}
			}
			if (nExpand != 0) {
				height = real.height;
				for (int row = 0; row + 1 < rows; row++) {
					height -= rowInfos[row].spacing;
				}
				for (int row = 0; row < rows; row++) {
					int extra = height / (rows - row);
					rowInfos[row].alloc = Math.max(1, extra);
					height -= extra;
				}
			}
		} else {
			height = 0;
			nExpand = 0;
			nShrink = 0;
			for (int row = 0; row < rows; row++) {
				height += rowInfos[row].req;
				if (rowInfos[row].expand) {
					nExpand += 1;
				}
				if (rowInfos[row].shrink) {
					nShrink += 1;
				}
			}
			for (int row = 0; row + 1 < rows; row++) {
				height += rowInfos[row].spacing;
			}
			// check to see if we were allocaed more height than we
			// requested.
			if (height < real.height && nExpand >= 1) {
				height = real.height - height;
				for (int row = 0; row < rows; row++) {
					if (rowInfos[row].expand) {
						int extra = height / nExpand;
						rowInfos[row].alloc += extra;
						height -= extra;
						nExpand -= 1;
					}
				}
			}
			// check to see if we were allocated less height than wer
			// requested.
			if (height > real.height) {
				int totalNShrink = nShrink;
				int extra = height - real.height;
				while (totalNShrink > 0 && extra > 0) {
					nShrink = totalNShrink;
					for (int row = 0; row < rows; row++) {
						if (rowInfos[row].shrink) {
							int al = rowInfos[row].alloc;
							rowInfos[row].alloc =
								Math.max(1, (int)rowInfos[row].alloc - extra / nShrink);
							extra -= al - rowInfos[row].alloc;
							nShrink-= 1;
							if (rowInfos[row].alloc < 2) {
								totalNShrink -= 1;
								rowInfos[row].shrink = false;
							}
						}
					}
				}
			}
		}
	}

	protected void layoutPass2(Container parent) {
		Iterator i = comps.keySet().iterator();
		Rectangle alloc = new Rectangle();
		while (i.hasNext()) {
			Component comp = (Component)i.next();
			TableConstraints constraints = (TableConstraints)comps.get(comp);
			if (comp.isVisible()) {
				System.out.println("pass2: visible");
				Dimension req = comp.getPreferredSize();
				Insets insets = parent.getInsets();
				System.out.println(parent.getBounds());
				int x = insets.left;
				int y = insets.top;
				int maxWidth = 0;
				int maxHeight = 0;
				for (int col = 0; col < constraints.leftAttach; col++) {
					x += columnInfos[col].alloc;
					x += columnInfos[col].spacing;
				}
				for (int col = constraints.leftAttach; col < constraints.rightAttach; col++) {
					maxWidth += columnInfos[col].alloc;
					if (col + 1 < constraints.rightAttach) {
						maxWidth += columnInfos[col].spacing;
					}
				}
				for (int row = 0; row < constraints.topAttach; row++) {
					y += rowInfos[row].alloc;
					y += rowInfos[row].spacing;
				}
				for (int row = constraints.topAttach; row < constraints.bottomAttach; row++) {
					maxHeight += rowInfos[row].alloc;
					if (row + 1 < constraints.bottomAttach) {
						maxHeight += rowInfos[row].spacing;
					}
				}
				if (constraints.xFill) {
					alloc.width = Math.max(1, maxWidth - (int)constraints.xPadding * 2);
					alloc.x = x;// + (maxWidth - alloc.width) / 2;
				} else {
					alloc.width = req.width;
					alloc.x = x;// + (maxWidth - alloc.width) / 2;
				}
				if (constraints.yFill) {
					alloc.height = Math.max(1, maxHeight - (int)constraints.yPadding * 2);
					alloc.y = y; //+ (maxHeight - alloc.height) / 2;
				} else {
					alloc.height = req.height;
					alloc.y = y; //+ (maxHeight - alloc.height) / 2;
				}
				comp.setBounds(alloc);
				System.out.println(alloc);
			}
		}
	}

	public Dimension minimumLayoutSize(Container parent) {
		System.out.println("minimumLayoutSize");
		return preferredLayoutSize(parent);
	}

	public Dimension preferredLayoutSize(Container parent) {
		System.out.println("preferredLayoutSize");
		Dimension req = new Dimension(0, 0);
		sizeInit();
		sizePass1();
		sizePass2();
		sizePass3();
		for (int col = 0; col < columns; col++) {
			req.width += columnInfos[col].req;
		}
		for (int col = 0; col + 1 < columns; col++) {
			req.width += columnInfos[col].spacing;
		}
		for (int row = 0; row < rows; row++) {
			req.height += rowInfos[row].req;
		}
		for (int row = 0; row + 1 < rows; row++) {
			req.height += rowInfos[row].spacing;
		}
		Insets insets = parent.getInsets();
		req.width += insets.left + insets.right;
		req.height += insets.top + insets.bottom;

		return req;
	}

	protected void sizeInit() {
		for (int r = 0; r < rows; r++) {
			rowInfos[r].req = 0;
			rowInfos[r].expand = false;
		}
		for (int c = 0; c < columns; c++) {
			columnInfos[c].req = 0;
			columnInfos[c].expand = false;
		}
		Iterator i = comps.keySet().iterator();
		while (i.hasNext()) {
			Component comp = (Component)i.next();
			TableConstraints constraints = (TableConstraints)comps.get(comp);
			if (comp.isVisible()) {
				System.out.println("sizeInit: visible ");
			}
			
			if (constraints.leftAttach == (constraints.rightAttach - 1) && constraints.xExpand) {
				columnInfos[constraints.leftAttach].expand = true;
			}
			if (constraints.topAttach == (constraints.bottomAttach - 1) && constraints.yExpand) {
				rowInfos[constraints.topAttach].expand = true;
			}
		}
	}

	protected void sizePass1() {
		Iterator i = comps.keySet().iterator();
		while (i.hasNext()) {
			Component comp = (Component)i.next();
			TableConstraints constraints = (TableConstraints)comps.get(comp);
			if (comp.isVisible()) {
				Dimension req = comp.getPreferredSize();
				// child spans a single column
				if (constraints.leftAttach == constraints.rightAttach - 1) {
					int width = req.width + constraints.xPadding * 2;
					columnInfos[constraints.leftAttach].req =
						Math.max(columnInfos[constraints.leftAttach].req, width);
				}
				// child spans a single row
				if (constraints.topAttach == constraints.bottomAttach - 1) {
					int height = req.height + constraints.yPadding * 2;
					rowInfos[constraints.topAttach].req =
						Math.max(rowInfos[constraints.topAttach].req, height);
				}
			}
		}
	}

	protected void sizePass2() {
		if (homogeneous) {
			int maxWidth = 0;
			int maxHeight = 0;
			for (int c = 0; c < columns; c++) {
				maxWidth = Math.max(maxWidth, columnInfos[c].req);
			}
			for (int r = 0; r < rows; r++) {
				maxHeight = Math.max(maxHeight, rowInfos[r].req);
			}
			for (int c = 0; c < columns; c++) {
				columnInfos[c].req = maxWidth;
			}
			for (int r = 0; r < rows; r++) {
				rowInfos[r].req = maxHeight;
			}
		}
	}

	protected void sizePass3() {
		Iterator i = comps.keySet().iterator();
		while (i.hasNext()) {
			Component comp = (Component)i.next();
			TableConstraints constraints = (TableConstraints)comps.get(comp);
			if (comp.isVisible()) {
				// child spans multiple columns
				if (constraints.leftAttach != constraints.rightAttach - 1) {
					Dimension req = comp.getPreferredSize();
					int width = 0;
					for (int col = constraints.leftAttach; col < constraints.rightAttach; col++) {
						width += columnInfos[col].req;
						if (col + 1 < constraints.rightAttach) {
							width += columnInfos[col].spacing;
						}
					}
					if (width < req.width + constraints.xPadding * 2) {
						int nExpand = 0;
						boolean forceExpand = false;
						width = req.width + constraints.xPadding * 2 - width;
						for (int col = constraints.leftAttach; col < constraints.rightAttach; col++) {
							if (columnInfos[col].expand) {
								nExpand++;
							}
						}
						if (nExpand == 0) {
							nExpand = constraints.rightAttach - constraints.leftAttach;
							forceExpand = true;
						}
						for (int col = constraints.leftAttach; col < constraints.rightAttach; col++) {
							if (forceExpand || columnInfos[col].expand) {
								int extra = width / nExpand;
								columnInfos[col].req += extra;
								width -= extra;
								nExpand--;
							}
						}
					}
				}
				// child spans multiple rows
				if (constraints.topAttach != constraints.bottomAttach - 1) {
					Dimension req = comp.getPreferredSize();
					int height = 0;
					for (int row = constraints.topAttach; row < constraints.bottomAttach; row++) {
						height += rowInfos[row].req;
						if (row + 1 < constraints.bottomAttach) {
							height += rowInfos[row].spacing;
						}
					}
					if (height < req.height + constraints.yPadding * 2) {
						int nExpand = 0;
						boolean forceExpand = false;
						height = req.height + constraints.yPadding * 2 - height;
						for (int row = constraints.topAttach; row < constraints.bottomAttach; row++) {
							if (rowInfos[row].expand) {
								nExpand++;
							}
						}
						if (nExpand == 0) {
							nExpand = constraints.bottomAttach - constraints.topAttach;
							forceExpand = true;
						}
						for (int row = constraints.topAttach; row < constraints.bottomAttach; row++) {
							if (forceExpand || rowInfos[row].expand) {
								int extra = height / nExpand;
								rowInfos[row].req += extra;
								height -= extra;
								nExpand--;
							}
						}
					}
				}
			}
		}
	}

	public void removeLayoutComponent(Component comp) {
		comps.remove(comp);
	}
	

	public float getLayoutAlignmentX(Container target) {
		return (float)0.5;
	}

	public float getLayoutAlignmentY(Container target) {
		return (float)0.5;
	}

	public void invalidateLayout(Container target) {
		invalid = true;
	}

	public Dimension maximumLayoutSize(Container target) {
		return preferredLayoutSize(target);
	}

	public static void main(String[] args) {
		JFrame f = new JFrame("TableLayoutTest");
		Container container = f.getContentPane();

		TableLayout tl = new TableLayout(3, 3);
		container.setLayout(tl);
		TableConstraints tc = new TableConstraints();
		JLabel label;

		label = new JLabel("AAA");
		tc.leftAttach = 0;
		tc.rightAttach = 2;
		tc.topAttach = 0;
		tc.bottomAttach = 1;
		tl.setConstraints(label, tc);
		container.add(label);

		label = new JLabel("BBB");
		tc.leftAttach = 1;
		tc.rightAttach = 3;
		tc.topAttach = 1;
		tc.bottomAttach = 2;
		tl.setConstraints(label, tc);
		container.add(label);
		f.setSize(200, 200);
		f.setVisible(true);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}
}

class TableConstraints implements Cloneable {
	public int leftAttach;
	public int rightAttach;
	public int topAttach;
	public int bottomAttach;

	public int xPadding;
	public int yPadding;
	public boolean xExpand;
	public boolean yExpand;
	public boolean xShrink;
	public boolean yShrink;
	public boolean xFill;
	public boolean yFill;

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("TableChild[");
		//		buf.append(comp.toString());
		buf.append("leftAttach=");   buf.append(leftAttach);
		buf.append(", rightAttach=");  buf.append(rightAttach);
		buf.append(", topAttach=");    buf.append(topAttach);
		buf.append(", bottomAttach="); buf.append(bottomAttach);
		buf.append("]");
		return buf.toString();
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}

class TableInfo {
	int req;
	int alloc;
	int spacing;
	boolean needExpand;
	boolean needShrink;
	boolean expand;
	boolean shrink;
	boolean empty;

	public TableInfo(int spacing) {
		req = 0;
		alloc = 0;
		this.spacing = spacing;
		needExpand = false;
		needShrink = true;
		expand = false;
		shrink = true;
		empty = true;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("TableInfo[");
		buf.append("req=");          buf.append(req);
		buf.append(", alloc=");      buf.append(alloc);
		buf.append(", spacing=");    buf.append(spacing);
		buf.append(", needExpand="); buf.append(needExpand);
		buf.append(", needShrink="); buf.append(needShrink);
		buf.append(", expand=");     buf.append(expand);
		buf.append(", shrink=");     buf.append(shrink);
		buf.append(", empty=");      buf.append(empty);
		buf.append("]");
		return buf.toString();
	}
}
