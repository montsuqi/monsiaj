package org.montsuqi.widgets;

import org.montsuqi.widgets.Window;
import org.montsuqi.widgets.PandaTimer;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.finder.FrameFinder;

import junit.textui.TestRunner;

public class TestPandaTimer extends JFCTestCase {
	private transient Window m_window;

	public TestPandaTimer(final String name) {
		super(name);
	}

	public void createPandaTimer() {
		final Window window = new Window();
		window.setTitle("PandaTimer Test"); //$NON-NLS-1$
		window.setBounds(100 , 100 , 200 , 200);
		PandaTimer pandaTimer = new PandaTimer();
		window.getContentPane().add(pandaTimer);
		window.setVisible(true);
	}

	public void setUp() throws Exception{
		createPandaTimer();
		m_window = (Window) TestHelper.getWindow(new FrameFinder("PandaTimer Test")); //$NON-NLS-1$
	}

	public void tearDown() throws Exception{
		m_window.setVisible(false);
	}


	public void testWindowframe() throws Exception {
		assertNotNull("Could not find window:", m_window); //$NON-NLS-1$
	}

	public static void main (String[] args){
		TestRunner.run(TestPandaTimer.class);
	}


}
