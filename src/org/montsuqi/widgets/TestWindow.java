package org.montsuqi.widgets;

import org.montsuqi.widgets.Window;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.finder.FrameFinder;

import junit.textui.TestRunner;

public class TestWindow extends JFCTestCase {
	private transient Window m_window;

	public TestWindow(final String name) {
		super(name);
	}

	public void createWindow() {
		final Window window = new Window();
		window.setTitle("Window Test"); //$NON-NLS-1$
		window.setBounds(100, 100, 200, 100);
		window.setVisible(true);
	}

	public void setUp() throws Exception{
		createWindow();
		m_window = (Window) TestHelper.getWindow(new FrameFinder("Window Test")); //$NON-NLS-1$
	}

	public void tearDown() throws Exception{
		m_window.setVisible(false);
	}


	public void testWindowframe() throws Exception {
		assertNotNull("Could not find window:", m_window); //$NON-NLS-1$
	}

	public static void main (String[] args){
		TestRunner.run(TestWindow.class);
	}


}
