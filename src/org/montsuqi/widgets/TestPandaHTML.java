package org.montsuqi.widgets;

import org.montsuqi.widgets.Window;
import org.montsuqi.widgets.PandaHTML;

import java.net.MalformedURLException;
import java.net.URL;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.finder.FrameFinder;

import junit.textui.TestRunner;

public class TestPandaHTML extends JFCTestCase {
	private transient Window m_window;

	public TestPandaHTML(final String name) {
		super(name);
	}

	public void createPandaHTML() throws MalformedURLException {
		final Window window = new Window();
		window.setTitle("PandaHTML Test"); //$NON-NLS-1$
		window.setBounds(100 , 100 , 600 , 400);
		PandaHTML pandaHTML = new PandaHTML();
		URL uri = new URL("http://www.orca.med.or.jp/info/"); //$NON-NLS-1$
		pandaHTML.setURI(uri);
		window.getContentPane().add(pandaHTML);
		window.setVisible(true);
	}

	public void setUp() throws Exception{
		createPandaHTML();
		m_window = (Window) TestHelper.getWindow(new FrameFinder("PandaHTML Test")); //$NON-NLS-1$
	}

	public void tearDown() throws Exception{
		m_window.setVisible(false);
	}


	public void testWindowframe() throws Exception {
		assertNotNull("Could not find window:", m_window); //$NON-NLS-1$
	}

	public static void main (String[] args){
		TestRunner.run(TestPandaHTML.class);
	}


}
