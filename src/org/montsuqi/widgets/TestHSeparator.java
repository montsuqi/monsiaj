package org.montsuqi.widgets;

import org.montsuqi.widgets.Window;
import org.montsuqi.widgets.HSeparator;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.finder.FrameFinder;

import junit.textui.TestRunner;

public class TestHSeparator extends JFCTestCase {
	private transient Window m_window;

	public TestHSeparator(final String name) {
		super(name);
	}

	public void createHSeparator() {
		final Window window = new Window();
		window.setTitle("HSeparator Test"); //$NON-NLS-1$
		window.setBounds(100 , 100 , 200 , 200);
		HSeparator hSeparator = new HSeparator();
		window.getContentPane().add(hSeparator);
		window.setVisible(true);
	}

	public void setUp() throws Exception{
		createHSeparator();
		m_window = (Window) TestHelper.getWindow(new FrameFinder("HSeparator Test")); //$NON-NLS-1$
	}

	public void tearDown() throws Exception{
		m_window.setVisible(false);
	}


	public void testWindowframe() throws Exception {
		assertNotNull("Could not find window:", m_window); //$NON-NLS-1$
	}

	public static void main (String[] args){
		TestRunner.run(TestHSeparator.class);
	}


}
