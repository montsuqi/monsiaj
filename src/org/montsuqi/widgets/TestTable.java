package org.montsuqi.widgets;

import org.montsuqi.widgets.Window;
import org.montsuqi.widgets.Table;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.finder.FrameFinder;

import junit.textui.TestRunner;

public class TestTable extends JFCTestCase {
	private transient Window m_window;

	public TestTable(final String name) {
		super(name);
	}

	public void createTable() {
		final Window window = new Window();
		window.setTitle("Table Test"); //$NON-NLS-1$
		window.setBounds(100 , 100 , 200 , 200);
		Table table = new Table();
		window.getContentPane().add(table);
		window.setVisible(true);
	}

	public void setUp() throws Exception{
		createTable();
		m_window = (Window) TestHelper.getWindow(new FrameFinder("Table Test")); //$NON-NLS-1$
	}

	public void tearDown() throws Exception{
		m_window.setVisible(false);
	}


	public void testWindowframe() throws Exception {
		assertNotNull("Could not find window:", m_window); //$NON-NLS-1$
	}

	public static void main (String[] args){
		TestRunner.run(TestTable.class);
	}


}
