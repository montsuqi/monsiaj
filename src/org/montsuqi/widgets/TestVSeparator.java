package org.montsuqi.widgets;

import org.montsuqi.widgets.Window;
import org.montsuqi.widgets.VSeparator;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.finder.FrameFinder;

import junit.textui.TestRunner;

public class TestVSeparator extends JFCTestCase {
	private transient TestHelper m_helper;
	private transient Window m_window;

	public TestVSeparator(final String name) {
		super(name);
	}

	public void createVSeparator() {
		final Window window = new Window();
		window.setTitle("VSeparator Test");
		window.setBounds(100 , 100 , 200 , 200);
		VSeparator vSeparator = new VSeparator();
		window.getContentPane().add(vSeparator);
		window.setVisible(true);
	}

	public void setUp() throws Exception{
		m_helper = new JFCTestHelper();
		createVSeparator();
		m_window = (Window) TestHelper.getWindow(new FrameFinder("VSeparator Test"));
	}

	public void tearDown() throws Exception{
		m_window.setVisible(false);
	}


	public void testWindowframe() throws Exception {
		assertNotNull("Could not find window:", m_window);
	}

	public static void main (String[] args){
		TestRunner.run(TestVSeparator.class);
	}


}
