package org.montsuqi.widgets;

import org.montsuqi.widgets.Window;
import org.montsuqi.widgets.PandaCombo;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.finder.FrameFinder;

import junit.textui.TestRunner;

public class TestPandaCombo extends JFCTestCase {
	private transient TestHelper m_helper;
	private transient Window m_window;

	public TestPandaCombo(final String name) {
		super(name);
	}

	public void createPandaCombo() {
		final Window window = new Window();
		window.setTitle("PandaCombo Test");
		window.setBounds(100 , 100 , 200 , 50);
		PandaCombo pandaCombo = new PandaCombo();
		pandaCombo.setEditable(true);
		window.getContentPane().add(pandaCombo);
		window.setVisible(true);
	}

	public void setUp() throws Exception{
		m_helper = new JFCTestHelper();
		createPandaCombo();
		m_window = (Window) TestHelper.getWindow(new FrameFinder("PandaCombo Test"));
	}

	public void tearDown() throws Exception{
		m_window.setVisible(false);
	}


	public void testWindowframe() throws Exception {
		assertNotNull("Could not find window:", m_window);
	}

	public static void main (String[] args){
		TestRunner.run(TestPandaCombo.class);
	}


}
