package org.montsuqi.widgets;

import org.montsuqi.widgets.Window;
import org.montsuqi.widgets.Calendar;
import org.montsuqi.widgets.CalendarEvent;
import org.montsuqi.widgets.CalendarListener;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.awt.GridLayout;
import java.util.Date;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.finder.Finder;
import junit.extensions.jfcunit.finder.FrameFinder;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.eventdata.MouseEventData;

import junit.textui.TestRunner;

public class TestCalendar extends JFCTestCase {
	private transient TestHelper m_helper;
	private transient Window m_window;

	public TestCalendar(final String name) {
		super(name);
	}

	public static void CreateCalendar() {
		final Window window = new Window();
		window.setTitle("CalendarTest"); 
		final Calendar cal = new Calendar();
		window.getContentPane().add(cal);
		window.setBounds(100 , 100 , 200 , 200);
		window.setVisible(true);
		final DateFormat monthFormat = new SimpleDateFormat("yyyy/MM"); //$NON-NLS-1$
		final DateFormat dayFormat = new SimpleDateFormat("yyyy/MM/dd"); //$NON-NLS-1$
		cal.addCalendarListener(new CalendarListener() {

			public void previousMonth(CalendarEvent e) {
				Date date = cal.getDate();
				JOptionPane.showMessageDialog(window, monthFormat.format(date));
			}

			public void nextMonth(CalendarEvent e) {
				Date date = cal.getDate();
				JOptionPane.showMessageDialog(window, monthFormat.format(date));
			}

			public void daySelected(CalendarEvent e) {
				Date date = cal.getDate();
				JOptionPane.showMessageDialog(window, dayFormat.format(date));
			}
		});
	}

	public void setUp() throws Exception{
		m_helper = new JFCTestHelper();
		CreateCalendar();
		m_window = (Window) TestHelper.getWindow(new FrameFinder("CalendarTest"));
	}

	public void tearDown() throws Exception{
		m_window.setVisible(false);
	}

	public void testDateSelect() throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat ("dd");
		Date currentDate = new Date();
		Finder f = new AbstractButtonFinder(formatter.format(currentDate));
		JButton today_button = (JButton) f.find(m_window, 0);
		assertNotNull("Could not find today button", today_button);
		m_helper.enterClickAndLeave( new MouseEventData( this, today_button ) );
	}

	public void testNextMonth() throws Exception {
		Finder f = new AbstractButtonFinder(">");
		JButton nextMonthButton = (JButton) f.find(m_window, 0);
		assertNotNull("Could not find Next Month button", nextMonthButton);
		m_helper.enterClickAndLeave( new MouseEventData( this, nextMonthButton ) );
	}

	public void testPreviousMonth() throws Exception {
		Finder f = new AbstractButtonFinder("<");
		JButton previousMonthButton = (JButton) f.find(m_window, 0);
		assertNotNull("Could not find field", previousMonthButton);
		m_helper.enterClickAndLeave( new MouseEventData( this, previousMonthButton ) );
	}

	public static void main (String[] args){
		TestRunner.run(TestCalendar.class);
	}


}
