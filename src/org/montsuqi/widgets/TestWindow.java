package org.montsuqi.widgets;

import org.montsuqi.widgets.Window;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.finder.FrameFinder;

import junit.textui.TestRunner;

public class TestWindow extends JFCTestCase {
	private transient TestHelper m_helper;
	private transient Window m_window;

	public TestWindow(final String name) {
		super(name);
	}

	public void CreateWindow() {
		final Window window = new Window();
		window.setTitle("Window Test");
		window.setBounds(100, 100, 200, 100);
		window.setVisible(true);
	}

	public void setUp() throws Exception{
		m_helper = new JFCTestHelper();
		CreateWindow();
		m_window = (Window) TestHelper.getWindow(new FrameFinder("Window Test"));
	}

	public void tearDown() throws Exception{
		m_window.setVisible(false);
	}


	public void testWindowframe() throws Exception {
		assertNotNull("Could not find window:", m_window);
	}

	public static void main (String[] args){
		TestRunner.run(TestWindow.class);
	}


}
