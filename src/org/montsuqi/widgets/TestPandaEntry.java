package org.montsuqi.widgets;

import org.montsuqi.widgets.Window;
import org.montsuqi.widgets.PandaEntry;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.StringEventData;
import junit.extensions.jfcunit.finder.NamedComponentFinder;
import junit.extensions.jfcunit.finder.FrameFinder;

import junit.textui.TestRunner;

public class TestPandaEntry extends JFCTestCase {
	private transient TestHelper m_helper;
	private transient PandaEntry m_pandaEntry;
	private transient Window m_window;

	public TestPandaEntry(final String name) {
		super(name);
	}

	public void createPandaEntry() {
		final Window window = new Window();
		window.setTitle("PandaEntry Test");
		window.setBounds(100 , 100 , 200 , 50);
		PandaEntry pandaEntry = new PandaEntry();
		pandaEntry.setName("PandaEntry1");
		window.getContentPane().add(pandaEntry);
		window.setVisible(true);
	}

	public void setUp() throws Exception{
		m_helper = new JFCTestHelper();
		createPandaEntry();
		m_window = (Window) TestHelper.getWindow(new FrameFinder("PandaEntry Test"));

		NamedComponentFinder f = new NamedComponentFinder(
			PandaEntry.class, "PandaEntry1");

		m_pandaEntry = (PandaEntry) f.find(m_window, 0);
	}

	public void tearDown() throws Exception{
		m_window.setVisible(false);
	}

	public void testKANAentry() throws Exception {
		String text;
		m_pandaEntry.setInputMode(PandaDocument.KANA);
		m_helper.sendString(new StringEventData(this,
						m_pandaEntry, "aiueo"));
		text = m_pandaEntry.getText();
		assertEquals("KANA input ", "\u30a2\u30a4\u30a6\u30a8\u30aa", text);
	}

	public void testASCIIentry() throws Exception {
		String text;
		m_pandaEntry.setInputMode(PandaDocument.ASCII);
		m_helper.sendString(new StringEventData(this,
							m_pandaEntry, "aa"));
		text = m_pandaEntry.getText();
		assertEquals("KANA input ", "aa", text);
	}

	public void testXIMentry() throws Exception {
		String text;
		m_pandaEntry.setInputMode(PandaDocument.XIM);
		m_helper.sendString(new StringEventData(this,
							m_pandaEntry, "aa"));
		text = m_pandaEntry.getText();
		assertEquals("KANA input ", "aa", text);
	}

	public static void main (String[] args){
		TestRunner.run(TestPandaEntry.class);
	}


}
