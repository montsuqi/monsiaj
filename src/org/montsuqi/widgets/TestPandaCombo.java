package org.montsuqi.widgets;

import org.montsuqi.widgets.Window;
import org.montsuqi.widgets.PandaCombo;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.finder.FrameFinder;
import junit.extensions.jfcunit.finder.NamedComponentFinder;
import junit.extensions.jfcunit.eventdata.JComboBoxMouseEventData;

import junit.textui.TestRunner;

public class TestPandaCombo extends JFCTestCase {
	private transient TestHelper m_helper;
	private transient Window m_window;
	private transient PandaCombo m_pandaCombo;

	public TestPandaCombo(final String name) {
		super(name);
	}

	public void createPandaCombo() {
		final Window window = new Window();
		window.setTitle("PandaCombo Test"); //$NON-NLS-1$
		window.setBounds(100 , 100 , 200 , 50);
		PandaCombo pandaCombo = new PandaCombo();
		pandaCombo.setName("PandaCombo1");
		pandaCombo.addItem("Items-0");
		pandaCombo.addItem("Items-1");
		pandaCombo.addItem("Items-2");
		pandaCombo.addItem("Items-3");
		pandaCombo.setEditable(true);

		window.getContentPane().add(pandaCombo);
		window.setVisible(true);
	}

	public void setUp() throws Exception{
		m_helper = new JFCTestHelper();
		createPandaCombo();
		m_window = (Window) TestHelper.getWindow(new FrameFinder("PandaCombo Test"));
		NamedComponentFinder f = new NamedComponentFinder(
			PandaCombo.class, "PandaCombo1");

		m_pandaCombo = (PandaCombo) f.find(m_window, 0);
	}

	public void tearDown() throws Exception{
		m_window.setVisible(false);
	}

	public void testComboClick() throws Exception {
		try {
			setHelper(new JFCTestHelper());
		} catch (Exception e) {
			// do nothing
		}

		String text = null;
		text = (String) m_pandaCombo.getSelectedItem();
		assertEquals("Cursor down test", "Items-0", text);

		JComboBoxMouseEventData m_event1 = 
			new JComboBoxMouseEventData(this,m_pandaCombo, 2, 1);

		m_helper.enterClickAndLeave(m_event1);
		text = (String) m_pandaCombo.getSelectedItem();

		assertEquals("Cursor down test", "Items-2", text);
	}

	public static void main (String[] args){
		TestRunner.run(TestPandaCombo.class);
	}


}
