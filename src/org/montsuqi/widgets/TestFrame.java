package org.montsuqi.widgets;

import org.montsuqi.widgets.Window;
import org.montsuqi.widgets.Frame;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.finder.FrameFinder;

import junit.textui.TestRunner;

public class TestFrame extends JFCTestCase {
	private transient TestHelper m_helper;
	private transient Window m_window;

	public TestFrame(final String name) {
		super(name);
	}

	public void createFrame() {
		final Window window = new Window();
		window.setTitle("Frame Test");
		window.setBounds(100, 100, 200, 200);
		Frame frame = new Frame();
		window.getContentPane().add(frame);
		window.setVisible(true);
	}

	public void setUp() throws Exception{
		m_helper = new JFCTestHelper();
		createFrame();
		m_window = (Window) TestHelper.getWindow(new FrameFinder("Frame Test"));
	}

	public void tearDown() throws Exception{
		m_window.setVisible(false);
	}


	public void testWindowframe() throws Exception {
		assertNotNull("Could not find window:", m_window);
	}

	public static void main (String[] args){
		TestRunner.run(TestFrame.class);
	}


}
