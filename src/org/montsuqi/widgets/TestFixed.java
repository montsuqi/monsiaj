package org.montsuqi.widgets;

import org.montsuqi.widgets.Window;
import org.montsuqi.widgets.Fixed;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.finder.FrameFinder;

import junit.textui.TestRunner;

public class TestFixed extends JFCTestCase {
	private transient Window m_window;

	public TestFixed(final String name) {
		super(name);
	}

	public void createFixed() {
		final Window window = new Window();
		window.setTitle("Fixed Test"); //$NON-NLS-1$
		window.setBounds(100 , 100 , 200 , 200);
		Fixed fixed = new Fixed();
		fixed.setSize(10,10);
		window.getContentPane().add(fixed);
		window.setVisible(true);
	}

	public void setUp() throws Exception{
		createFixed();
		m_window = (Window) TestHelper.getWindow(new FrameFinder("Fixed Test")); //$NON-NLS-1$
	}

	public void tearDown() throws Exception{
		m_window.setVisible(false);
	}


	public void testWindowframe() throws Exception {
		assertNotNull("Could not find window:", m_window); //$NON-NLS-1$
	}

	public static void main (String[] args){
		TestRunner.run(TestFixed.class);
	}


}
