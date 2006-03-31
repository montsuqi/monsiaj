package org.montsuqi.widgets;

import org.montsuqi.widgets.Window;
import org.montsuqi.widgets.NumberEntry;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.StringEventData;
import junit.extensions.jfcunit.finder.NamedComponentFinder;
import junit.extensions.jfcunit.finder.FrameFinder;

import junit.textui.TestRunner;

public class TestNumberEntry extends JFCTestCase {
	private transient TestHelper m_helper;
	private transient Window m_window;
	private transient NumberEntry m_numberEntry;

	public TestNumberEntry(final String name) {
		super(name);
	}

	public void assertFormattedInputEquals(String expected, String input) {
		m_helper.sendString(new StringEventData(this, m_numberEntry, input));
		final NumberDocument doc = (NumberDocument)m_numberEntry.getDocument();
		final String format = doc.getFormat();
		final String actual = m_numberEntry.getText();
		assertEquals("Using format \"" + format + "\"", expected, actual);
	}

	public void createNumberEntry() {
		final Window window = new Window();
		window.setTitle("NumberEntry Test"); //$NON-NLS-1$
		window.setBounds(100 , 100 , 200 , 50);
		final NumberEntry numberEntry = new NumberEntry();
		numberEntry.setName("NumberEntery1"); //$NON-NLS-1$
		window.getContentPane().add(numberEntry);
		window.setVisible(true);
	}

	public void setUp() {
		m_helper = new JFCTestHelper();
		createNumberEntry();
		m_window = (Window) TestHelper.getWindow(new FrameFinder("NumberEntry Test")); //$NON-NLS-1$
		NamedComponentFinder f = new NamedComponentFinder(
			NumberEntry.class, "NumberEntery1"); //$NON-NLS-1$

		m_numberEntry = (NumberEntry) f.find(m_window, 0);
	}

	public void tearDown() {
		m_window.dispose();
	}

	/* names of test methods are "test" + format string. */
	/* "-"s are substituted with "_"s, "," is "c", "." is "d". */
	public void test__________() throws Exception {
		m_numberEntry.setFormat("----------");

		assertFormattedInputEquals("", "0");
		assertFormattedInputEquals("10000", "10000");
	}

	public void test___c___c___() throws Exception {
		m_numberEntry.setFormat("---,---,---");

		assertFormattedInputEquals("", "0");
		assertFormattedInputEquals("10,000", "10000");
	}

	public void test___c___c__9() throws Exception {
		m_numberEntry.setFormat("---,---,--9");

		assertFormattedInputEquals("0", "0");
		assertFormattedInputEquals("10,000", "10000");
	}

	public void testZ() {
		m_numberEntry.setFormat("Z");

		assertFormattedInputEquals("", "0");
		assertFormattedInputEquals("1", "10000");
	}

	public void testZZ() {
		m_numberEntry.setFormat("ZZ");

		assertFormattedInputEquals("", "0");
		assertFormattedInputEquals("10", "10000");
	}

	public void testZZZZ() {
		m_numberEntry.setFormat("ZZZZ");

		assertFormattedInputEquals("", "0");
		assertFormattedInputEquals("1000", "1000");
		assertFormattedInputEquals("10000", "10000");
		assertFormattedInputEquals("1000", "100000");
	}

	public void testZZZZZZZZZZ() {
		m_numberEntry.setFormat("ZZZZZZZZZZ");

		assertFormattedInputEquals("", "0");
		assertFormattedInputEquals("10,000", "10000");
	}

	public void test9() {
		m_numberEntry.setFormat("9");

		assertFormattedInputEquals("0", "0");
		assertFormattedInputEquals("10,000", "10000");
	}

	public void test99() {
		m_numberEntry.setFormat("99");

		assertFormattedInputEquals("0", "0");
		assertFormattedInputEquals("10,000", "10000");
	}

	public void testZ9() {
		m_numberEntry.setFormat("Z9");

		assertFormattedInputEquals("0", "0");
		assertFormattedInputEquals("10,000", "10000");
	}

	public void test9999() {
		m_numberEntry.setFormat("9999");

		assertFormattedInputEquals("0", "0");
		assertFormattedInputEquals("10,000", "10000");
	}

	public void testZZZ9() {
		m_numberEntry.setFormat("ZZZ9");

		assertFormattedInputEquals("0", "0");
		assertFormattedInputEquals("101", "101");
	}

	public void test9999999999() {
		m_numberEntry.setFormat("9999999999");

		assertFormattedInputEquals("0", "0");
		assertFormattedInputEquals("10,000", "10000");
	}

	public void testZZZZZZZZZ9() {
		m_numberEntry.setFormat("ZZZZZZZZZ9");

		assertFormattedInputEquals("0", "0");
		assertFormattedInputEquals("10,000", "10000");
	}

	public void testZZZZZZZZ99() {
		m_numberEntry.setFormat("ZZZZZZZZ99");

		assertFormattedInputEquals("0", "0");
		assertFormattedInputEquals("10,000", "10000");
	}

	public void testZZZcZZZcZZZ() {
		m_numberEntry.setFormat("ZZ,ZZZ,ZZZ");

		assertFormattedInputEquals("0", "0");
		assertFormattedInputEquals("10,000", "10000");
	}

	public void testZZ99d99() {
		m_numberEntry.setFormat("ZZ99.99");

		assertFormattedInputEquals("0", "0");
		assertFormattedInputEquals("10,000", "10000");
	}

	public void testZZZ99d99999() {
		m_numberEntry.setFormat("ZZZ99.99999");

		assertFormattedInputEquals("0", "0");
		assertFormattedInputEquals("10,000", "10000");
	}

	public void test99d9() throws Exception {
		m_numberEntry.setFormat("99.9");

		assertFormattedInputEquals("00.0", "0");
		assertFormattedInputEquals("01.0", "1.0");
	}

	public void testZZdZ() throws Exception {
		m_numberEntry.setFormat("ZZ.Z");

		assertFormattedInputEquals("", "AA");
		assertFormattedInputEquals("", "0");
		assertFormattedInputEquals("1", "1.0");
	}

	public static void main (String[] args){
		TestRunner.run(TestNumberEntry.class);
	}
}
