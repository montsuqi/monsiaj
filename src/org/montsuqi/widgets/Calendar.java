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

package org.montsuqi.widgets;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import java.util.Date;

public class Calendar extends JComponent {

	private Date date;
	private SimpleDateFormat df;
	private JComponent caption;
	private JButton[][] dateCells;
	private Date[][] cellDates;
	private java.util.Calendar cal;

	private JButton prev;
	private JButton next;
	private JLabel monthLabel;

	public Calendar() {
		super();
		df = new SimpleDateFormat("yyyy/MM"); //$NON-NLS-1$
		setLayout(new BorderLayout());
		caption = new JPanel();
		add(caption, BorderLayout.NORTH);
		prev = new JButton("<"); //$NON-NLS-1$
		next = new JButton(">"); //$NON-NLS-1$

		prev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				previousMonth();
				fireCalendarEvent(new CalendarEvent(Calendar.this, CalendarEvent.PREVIOUS_MONTH));
			}
		});

		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nextMonth();
				fireCalendarEvent(new CalendarEvent(Calendar.this, CalendarEvent.NEXT_MONTH));
			}
		});

		monthLabel = new JLabel();
		GridBagLayout gbl = new GridBagLayout();
		caption.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbl.setConstraints(prev, gbc);
		caption.add(prev);

		gbc.gridx = 2;
		gbl.setConstraints(next, gbc);
		caption.add(next);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbl.setConstraints(monthLabel, gbc);
		caption.add(monthLabel);

		dateCells = new JButton[7][7];
		cellDates = new Date[7][7];
		JComponent dateCellPanel = new JPanel();
		dateCellPanel.setLayout(new GridLayout(7, 7));
		add(dateCellPanel, BorderLayout.CENTER);

		cal = java.util.Calendar.getInstance();
		setDate(cal.getTime());

		DateFormatSymbols symbols = df.getDateFormatSymbols();
		String[] dayOfWeekNames = symbols.getShortWeekdays();
		for (int col = 0; col < 7; col++) {
			JLabel dayOfWeek = new JLabel(dayOfWeekNames[col + 1]);
			dayOfWeek.setHorizontalAlignment(SwingConstants.CENTER);
			dateCellPanel.add(dayOfWeek);
		}

		Insets margin = new Insets(0, 0, 0, 0);
		for (int row = 1; row < 7; row++) {
			for (int col = 0; col < 7; col++) {
				final JButton cell = new JButton();
				cell.setHorizontalAlignment(SwingConstants.RIGHT);
				
				cell.setMargin(margin);
				final int finalRow = row;
				final int finalCol = col;
				cell.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setDate(cellDates[finalRow][finalCol]);
						fireCalendarEvent(new CalendarEvent(Calendar.this, CalendarEvent.DAY_SELECTED));
					}
				});
				dateCells[row][col] = cell;
				dateCellPanel.add(cell);
			}
		}
		setCells();
	}

	protected void fireCalendarEvent(CalendarEvent e) {
		CalendarListener[] listeners = (CalendarListener[])listenerList.getListeners(CalendarListener.class);
		for (int i = 0, n = listeners.length; i < n; i++) {
			CalendarListener l = listeners[i];
			switch (e.getID()) {
			case CalendarEvent.PREVIOUS_MONTH:
				l.previousMonth(e);
				break;
			case CalendarEvent.NEXT_MONTH:
				l.nextMonth(e);
				break;
			case CalendarEvent.DAY_SELECTED:
				l.daySelected(e);
				break;
			default:
				throw new IllegalStateException(Messages.getString("Calendar.unknown_event_id")); //$NON-NLS-1$
			}
		}
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	private void setCells() {
		cal.setTime(date);
		int month = cal.get(java.util.Calendar.MONTH);
		for (int row = 1; row < 7; row++) {
			for (int col = 0; col < 7; col++) {
				Date d = computeCellDate(row, col);
				cellDates[row][col] = d;
				cal.setTime(d);
				JButton cell = dateCells[row][col];
				cell.setText(String.valueOf(cal.get(java.util.Calendar.DATE)));
				cell.setEnabled(month == cal.get(java.util.Calendar.MONTH));
			}
		}
		monthLabel.setText(df.format(date));
	}
	
	private Date computeCellDate(int row, int col) {
		// compute the first day of the month 
		cal.setTime(date);
		cal.set(java.util.Calendar.DATE, 1);

		// compute the date of top left cell.
		cal.add(java.util.Calendar.DATE, -cal.get(java.util.Calendar.DAY_OF_WEEK));
		cal.add(java.util.Calendar.DATE, cal.getFirstDayOfWeek());

		// advance to the desired row/col
		cal.add(java.util.Calendar.DATE, (row - 1) * 7 + col);

		return cal.getTime();
	}

	public void nextMonth() {
		cal.setTime(date);
		cal.roll(java.util.Calendar.MONTH, true);
		if (cal.getTime().before(date)) {
			cal.roll(java.util.Calendar.YEAR, true);
		}
		
		date = cal.getTime();
		setCells();
	}

	public void previousMonth() {
		cal.setTime(date);
		cal.roll(java.util.Calendar.MONTH, false);
		if (cal.getTime().after(date)) {
			cal.roll(java.util.Calendar.YEAR, false);
		}
		date = cal.getTime();
		setCells();
	}

	public void addCalendarListener(CalendarListener l) {
		listenerList.add(CalendarListener.class, l);
	}

	public void removeCalendarListener(CalendarListener l) {
		listenerList.remove(CalendarListener.class, l);
	}

	public static void main(String[] args) {
		final JFrame f = new JFrame("CalendarTest"); //$NON-NLS-1$
		final Calendar cal = new Calendar();
		f.getContentPane().add(cal);
		f.setSize(400, 300);
		f.setVisible(true);
		final DateFormat monthFormat = new SimpleDateFormat("yyyy/MM"); //$NON-NLS-1$
		final DateFormat dayFormat = new SimpleDateFormat("yyyy/MM/dd"); //$NON-NLS-1$
		cal.addCalendarListener(new CalendarListener() {

			public void previousMonth(CalendarEvent e) {
				Date date = cal.getDate();
				JOptionPane.showMessageDialog(f, monthFormat.format(date));
			}

			public void nextMonth(CalendarEvent e) {
				Date date = cal.getDate();
				JOptionPane.showMessageDialog(f, monthFormat.format(date));
			}

			public void daySelected(CalendarEvent e) {
				Date date = cal.getDate();
				JOptionPane.showMessageDialog(f, dayFormat.format(date));
			}
		});
	}
}
