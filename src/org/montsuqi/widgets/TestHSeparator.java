package org.montsuqi.widgets;

import org.montsuqi.widgets.Window;
import org.montsuqi.widgets.HSeparator;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.finder.FrameFinder;

import junit.textui.TestRunner;

public class TestHSeparator extends JFCTestCase {
	private transient TestHelper m_helper;
	private transient Window m_window;

	public TestHSeparator(final String name) {
		super(name);
	}

	public void CreateHSeparator() {
		final Window window = new Window();
		window.setTitle("HSeparator Test");
		window.setBounds(100 , 100 , 200 , 200);
		HSeparator hSeparator = new HSeparator();
		window.getContentPane().add(hSeparator);
		window.setVisible(true);
	}

	public void setUp() throws Exception{
		m_helper = new JFCTestHelper();
		CreateHSeparator();
		m_window = (Window) TestHelper.getWindow(new FrameFinder("HSeparator Test"));
	}

	public void tearDown() throws Exception{
		m_window.setVisible(false);
	}


	public void testWindowframe() throws Exception {
		assertNotNull("Could not find window:", m_window);
	}

	public static void main (String[] args){
		TestRunner.run(TestHSeparator.class);
	}


}
