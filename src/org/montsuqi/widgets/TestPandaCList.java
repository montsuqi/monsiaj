package org.montsuqi.widgets;

import org.montsuqi.widgets.Window;
import org.montsuqi.widgets.PandaCList;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.finder.FrameFinder;

import junit.textui.TestRunner;

public class TestPandaCList extends JFCTestCase {
	private transient Window m_window;

	public TestPandaCList(final String name) {
		super(name);
	}

	public void createPandaCList() {
		final Window window = new Window();
		window.setTitle("PandaCList Test"); //$NON-NLS-1$
		window.setBounds(100 , 100 , 200 , 200);
		PandaCList pandaCList = new PandaCList();
		window.getContentPane().add(pandaCList);
		window.setVisible(true);
	}

	public void setUp() throws Exception{
		createPandaCList();
		m_window = (Window) TestHelper.getWindow(new FrameFinder("PandaCList Test")); //$NON-NLS-1$
	}

	public void tearDown() throws Exception{
		m_window.setVisible(false);
	}


	public void testWindowframe() throws Exception {
		assertNotNull("Could not find window:", m_window); //$NON-NLS-1$
	}

	public static void main (String[] args){
		TestRunner.run(TestPandaCList.class);
	}


}
