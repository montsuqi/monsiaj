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
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.util.Date;

public class Calendar extends JComponent {

	Date date;
	private JComponent caption;
	private JButton[][] dateCells;
	Date[][] cellDates;
	java.util.Calendar cal;

	public Calendar() {
		super();
		setLayout(new BorderLayout());
		caption = new JPanel();
		add(caption, BorderLayout.NORTH);
		caption.setLayout(new GridLayout(1, 2));

		caption.add(createDateSpinner(java.util.Calendar.MONTH, Messages.getString("Calendar.month_format"))); //$NON-NLS-1$
		caption.add(createDateSpinner(java.util.Calendar.YEAR, Messages.getString("Calendar.year_format"))); //$NON-NLS-1$

		dateCells = new JButton[7][7];
		cellDates = new Date[7][7];
		JComponent dateCellPanel = new JPanel();
		dateCellPanel.setLayout(new GridLayout(7, 7));
		add(dateCellPanel, BorderLayout.CENTER);

		cal = java.util.Calendar.getInstance();
		setDate(cal.getTime());

		SimpleDateFormat df = new SimpleDateFormat();
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
						fireChangeEvent(new ChangeEvent(Calendar.this));
					}
				});
				dateCells[row][col] = cell;
				dateCellPanel.add(cell);
			}
		}
		setCells();
	}

	private JSpinner createDateSpinner(final int field, String format) {
		SpinnerDateModel model = new SpinnerDateModel();
		model.setCalendarField(field);
		final JSpinner spinner = new JSpinner(model);
		JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, format);
		spinner.setEditor(editor);

		JTextField textField = editor.getTextField();
		textField.setHorizontalAlignment(SwingConstants.RIGHT);

		spinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Date newDate = (Date)spinner.getValue();
				cal.setTime(newDate);
				int value = cal.get(field);
				cal.setTime(date);
				cal.set(field, value);
				setDate(cal.getTime());
				setCells();
			}
		});

		return spinner;
	}

	protected void fireChangeEvent(ChangeEvent e) {
		ChangeListener[] listeners = (ChangeListener[])listenerList.getListeners(ChangeListener.class);
		for (int i = 0, n = listeners.length; i < n; i++) {
			ChangeListener l = listeners[i];
			l.stateChanged(e);
		}
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	void setCells() {
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
		//monthLabel.setText(df.format(date));
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

	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);
	}

	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
	}

	public static void main(String[] args) {
		final JFrame f = new JFrame("CalendarTest"); //$NON-NLS-1$
		final Calendar cal = new Calendar();
		f.getContentPane().add(cal);
		f.setSize(400, 300);
		f.setVisible(true);
		final DateFormat dayFormat = new SimpleDateFormat("yyyy/MM/dd"); //$NON-NLS-1$
		cal.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				Date date = cal.getDate();
				JOptionPane.showMessageDialog(f, dayFormat.format(date));
			}
		});
	}
}
