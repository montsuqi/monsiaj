package org.montsuqi.widgets;

import org.montsuqi.widgets.Window;
import org.montsuqi.widgets.Calendar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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

	public static void createCalendar() {
		final Window window = new Window();
		window.setTitle("CalendarTest"); //$NON-NLS-1$
		final Calendar cal = new Calendar();
		window.getContentPane().add(cal);
		window.setBounds(100 , 100 , 200 , 200);
		window.setVisible(true);
		final DateFormat monthFormat = new SimpleDateFormat("yyyy/MM"); //$NON-NLS-1$
		final DateFormat dayFormat = new SimpleDateFormat("yyyy/MM/dd"); //$NON-NLS-1$
		cal.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				Date date = cal.getDate();
				JOptionPane.showMessageDialog(window, dayFormat.format(date));
			}
		});
	}

	public void setUp() throws Exception{
		m_helper = new JFCTestHelper();
		createCalendar();
		m_window = (Window) TestHelper.getWindow(new FrameFinder("CalendarTest")); //$NON-NLS-1$
	}

	public void tearDown() throws Exception{
		m_window.setVisible(false);
	}

	public void testDateSelect() throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat ("dd"); //$NON-NLS-1$
		Date currentDate = new Date();
		Finder f = new AbstractButtonFinder(formatter.format(currentDate));
		JButton today_button = (JButton) f.find(m_window, 0);
		assertNotNull("Could not find today button", today_button); //$NON-NLS-1$
		m_helper.enterClickAndLeave( new MouseEventData( this, today_button ) );
	}

	public void testNextMonth() throws Exception {
		Finder f = new AbstractButtonFinder(">"); //$NON-NLS-1$
		JButton nextMonthButton = (JButton) f.find(m_window, 0);
		assertNotNull("Could not find Next Month button", nextMonthButton); //$NON-NLS-1$
		m_helper.enterClickAndLeave( new MouseEventData( this, nextMonthButton ) );
	}

	public void testPreviousMonth() throws Exception {
		Finder f = new AbstractButtonFinder("<"); //$NON-NLS-1$
		JButton previousMonthButton = (JButton) f.find(m_window, 0);
		assertNotNull("Could not find field", previousMonthButton); //$NON-NLS-1$
		m_helper.enterClickAndLeave( new MouseEventData( this, previousMonthButton ) );
	}

	public static void main (String[] args){
		TestRunner.run(TestCalendar.class);
	}


}
