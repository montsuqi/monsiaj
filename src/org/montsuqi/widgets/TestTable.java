package org.montsuqi.widgets;

import org.montsuqi.widgets.Window;
import org.montsuqi.widgets.Table;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.finder.FrameFinder;

import junit.textui.TestRunner;

public class TestTable extends JFCTestCase {
	private transient TestHelper m_helper;
	private transient Window m_window;

	public TestTable(final String name) {
		super(name);
	}

	public void createTable() {
		final Window window = new Window();
		window.setTitle("Table Test");
		window.setBounds(100 , 100 , 200 , 200);
		Table table = new Table();
		window.getContentPane().add(table);
		window.setVisible(true);
	}

	public void setUp() throws Exception{
		m_helper = new JFCTestHelper();
		createTable();
		m_window = (Window) TestHelper.getWindow(new FrameFinder("Table Test"));
	}

	public void tearDown() throws Exception{
		m_window.setVisible(false);
	}


	public void testWindowframe() throws Exception {
		assertNotNull("Could not find window:", m_window);
	}

	public static void main (String[] args){
		TestRunner.run(TestTable.class);
	}


}
