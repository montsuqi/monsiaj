package org.montsuqi.widgets;

import org.montsuqi.widgets.Window;
import org.montsuqi.widgets.PandaTimer;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.finder.FrameFinder;

import junit.textui.TestRunner;

public class TestPandaTimer extends JFCTestCase {
	private transient TestHelper m_helper;
	private transient Window m_window;

	public TestPandaTimer(final String name) {
		super(name);
	}

	public void CreatePandaTimer() {
		final Window window = new Window();
		window.setTitle("PandaTimer Test");
		window.setBounds(100 , 100 , 200 , 200);
		PandaTimer pandaTimer = new PandaTimer();
		window.getContentPane().add(pandaTimer);
		window.setVisible(true);
	}

	public void setUp() throws Exception{
		m_helper = new JFCTestHelper();
		CreatePandaTimer();
		m_window = (Window) TestHelper.getWindow(new FrameFinder("PandaTimer Test"));
	}

	public void tearDown() throws Exception{
		m_window.setVisible(false);
	}


	public void testWindowframe() throws Exception {
		assertNotNull("Could not find window:", m_window);
	}

	public static void main (String[] args){
		TestRunner.run(TestPandaTimer.class);
	}


}
