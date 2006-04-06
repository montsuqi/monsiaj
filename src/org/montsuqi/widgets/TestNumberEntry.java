package org.montsuqi.widgets;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

import org.montsuqi.widgets.Window;
import org.montsuqi.widgets.NumberEntry;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.KeyEventData;
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
		m_numberEntry.setValue(new BigDecimal(0));
		m_helper.sendString(new StringEventData(this, m_numberEntry, input));
		final NumberDocument doc = (NumberDocument)m_numberEntry.getDocument();
		final String format = doc.getFormat();
		final String actual = m_numberEntry.getText();
		assertEquals("Using format \"" + format + "\", input:<" + input + ">, value=<" + m_numberEntry.getValue() + ">", expected, actual);
	}

	private void assertBackSpaceAfterInputEquals(String expected, String input) {
		m_helper.sendString(new StringEventData(this, m_numberEntry, input));
		m_helper.sendKeyAction(new KeyEventData(this, m_numberEntry, KeyEvent.VK_BACK_SPACE));
		final NumberDocument doc = (NumberDocument)m_numberEntry.getDocument();
		final String format = doc.getFormat();
		final String actual = m_numberEntry.getText();
		assertEquals("Using format \"" + format + "\", input:<" + input + ">, value=<" + m_numberEntry.getValue() + ">", expected, actual);
	}

	public void createNumberEntry() {
		final Window window = new Window();
		window.setTitle("NumberEntry Test"); //$NON-NLS-1$
		final NumberEntry numberEntry = new NumberEntry();
		numberEntry.setName("NumberEntery1"); //$NON-NLS-1$
		window.getContentPane().add(numberEntry);
		window.pack();
		window.setVisible(true);
	}

	public void setUp() {
		m_helper = new JFCTestHelper();
		createNumberEntry();
		m_window = (Window) TestHelper.getWindow(new FrameFinder("NumberEntry Test")); //$NON-NLS-1$
		NamedComponentFinder f = new NamedComponentFinder(NumberEntry.class, "NumberEntery1"); //$NON-NLS-1$

		m_numberEntry = (NumberEntry) f.find(m_window, 0);
	}

	public void tearDown() {
		m_window.dispose();
	}

	/* names of test methods are "test" + format string. */
	/* "-"s are substituted with "_"s, "," is "c", "." is "d". */
	public void test__________() throws Exception {
		m_numberEntry.setFormat("----------");

		assertFormattedInputEquals("", "AA");
		assertFormattedInputEquals("", "0");
		assertFormattedInputEquals("", "00");
		assertFormattedInputEquals("1", "01");
		assertFormattedInputEquals("1", "0.01");
		assertFormattedInputEquals("1", "0.1");
		assertFormattedInputEquals("10", "10");
		assertFormattedInputEquals("100", "100");
		assertFormattedInputEquals("1000", "1000");
		assertFormattedInputEquals("10000", "10000");
		assertFormattedInputEquals("1000000", "10000.00");
		assertFormattedInputEquals("", "-0");
		assertFormattedInputEquals("", "-00");
		assertFormattedInputEquals("-1", "-01");
		assertFormattedInputEquals("-1", "-0.01");
		assertFormattedInputEquals("-1", "-0.1");
		assertFormattedInputEquals("-10", "-10");
		assertFormattedInputEquals("-100", "-100");
		assertFormattedInputEquals("-1000", "-1000");
		assertFormattedInputEquals("-10000", "-10000");
		assertFormattedInputEquals("-1000000", "-10000.00");
		assertBackSpaceAfterInputEquals("", "10000");
	}

	public void test___c___c___() throws Exception {
		m_numberEntry.setFormat("---,---,---");

		assertFormattedInputEquals("", "AA");
		assertFormattedInputEquals("", "0");
		assertFormattedInputEquals("", "00");
		assertFormattedInputEquals("1", "01");
		assertFormattedInputEquals("1", "0.01");
		assertFormattedInputEquals("1", "0.1");
		assertFormattedInputEquals("10", "10");
		assertFormattedInputEquals("100", "100");
		assertFormattedInputEquals("1,000", "1000");
		assertFormattedInputEquals("10,000", "10000");
		assertFormattedInputEquals("1,000,000", "10000.00");
		assertFormattedInputEquals("", "-0");
		assertFormattedInputEquals("", "-00");
		assertFormattedInputEquals("-1", "-01");
		assertFormattedInputEquals("-1", "-0.01");
		assertFormattedInputEquals("-1", "-0.1");
		assertFormattedInputEquals("-10", "-10");
		assertFormattedInputEquals("-100", "-100");
		assertFormattedInputEquals("-1,000", "-1000");
		assertFormattedInputEquals("-10,000", "-10000");
		assertFormattedInputEquals("-1,000,000", "-10000.00");
		assertBackSpaceAfterInputEquals("", "10000");
	}

	public void test___c___c__9() throws Exception {
		m_numberEntry.setFormat("---,---,--9");

		assertFormattedInputEquals("0", "AA");
		assertFormattedInputEquals("0", "0");
		assertFormattedInputEquals("0", "00");
		assertFormattedInputEquals("1", "01");
		assertFormattedInputEquals("1", "0.01");
		assertFormattedInputEquals("1", "0.1");
		assertFormattedInputEquals("10", "10");
		assertFormattedInputEquals("100", "100");
		assertFormattedInputEquals("1,000", "1000");
		assertFormattedInputEquals("10,000", "10000");
		assertFormattedInputEquals("1,000,000", "10000.00");
		assertFormattedInputEquals("0", "-0");
		assertFormattedInputEquals("0", "-00");
		assertFormattedInputEquals("-1", "-01");
		assertFormattedInputEquals("-1", "-0.01");
		assertFormattedInputEquals("-1", "-0.1");
		assertFormattedInputEquals("-10", "-10");
		assertFormattedInputEquals("-100", "-100");
		assertFormattedInputEquals("-1,000", "-1000");
		assertFormattedInputEquals("-10,000", "-10000");
		assertFormattedInputEquals("-1,000,000", "-10000.00");
		assertBackSpaceAfterInputEquals("0", "10000");
	}

	public void testZ() {
		m_numberEntry.setFormat("Z");

		assertFormattedInputEquals("", "AA");
		assertFormattedInputEquals("", "0");
		assertFormattedInputEquals("", "00");
		assertFormattedInputEquals("1", "01");
		assertFormattedInputEquals("1", "0.01");
		assertFormattedInputEquals("1", "0.1");
		assertFormattedInputEquals("1", "10");
		assertFormattedInputEquals("1", "100");
		assertFormattedInputEquals("1", "1000");
		assertFormattedInputEquals("1", "10000");
		assertFormattedInputEquals("1", "10000.00");
		assertFormattedInputEquals("", "-0");
		assertFormattedInputEquals("", "-00");
		assertFormattedInputEquals("1", "-01");
		assertFormattedInputEquals("1", "-0.01");
		assertFormattedInputEquals("1", "-0.1");
		assertFormattedInputEquals("1", "-10");
		assertFormattedInputEquals("1", "-100");
		assertFormattedInputEquals("1", "-1000");
		assertFormattedInputEquals("1", "-10000");
		assertFormattedInputEquals("1", "-10000.00");
		assertBackSpaceAfterInputEquals("", "10000");
	}

	public void testZZ() {
		m_numberEntry.setFormat("ZZ");

		assertFormattedInputEquals("", "AA");
		assertFormattedInputEquals("", "0");
		assertFormattedInputEquals("", "00");
		assertFormattedInputEquals("1", "01");
		assertFormattedInputEquals("1", "0.01");
		assertFormattedInputEquals("1", "0.1");
		assertFormattedInputEquals("10", "10");
		assertFormattedInputEquals("10", "100");
		assertFormattedInputEquals("10", "1000");
		assertFormattedInputEquals("10", "10000");
		assertFormattedInputEquals("10", "10000.00");
		assertFormattedInputEquals("1", "-01");
		assertFormattedInputEquals("1", "-0.01");
		assertFormattedInputEquals("1", "-0.1");
		assertFormattedInputEquals("10", "-10");
		assertFormattedInputEquals("10", "-100");
		assertFormattedInputEquals("10", "-1000");
		assertFormattedInputEquals("10", "-10000");
		assertFormattedInputEquals("10", "-10000.00");
		assertBackSpaceAfterInputEquals("", "10000");
	}

	public void testZZZZ() {
		m_numberEntry.setFormat("ZZZZ");

		assertFormattedInputEquals("", "AA");
		assertFormattedInputEquals("", "0");
		assertFormattedInputEquals("", "00");
		assertFormattedInputEquals("1", "01");
		assertFormattedInputEquals("1", "0.01");
		assertFormattedInputEquals("1", "0.1");
		assertFormattedInputEquals("10", "10");
		assertFormattedInputEquals("100", "100");
		assertFormattedInputEquals("1000", "1000");
		assertFormattedInputEquals("1000", "10000");
		assertFormattedInputEquals("1000", "10000.00");
		assertFormattedInputEquals("1", "-01");
		assertFormattedInputEquals("1", "-0.01");
		assertFormattedInputEquals("1", "-0.1");
		assertFormattedInputEquals("10", "-10");
		assertFormattedInputEquals("100", "-100");
		assertFormattedInputEquals("1000", "-1000");
		assertFormattedInputEquals("1000", "-10000");
		assertFormattedInputEquals("1000", "-10000.00");
		assertBackSpaceAfterInputEquals("", "10000");
	}

	public void testZZZZZZZZZZ() {
		m_numberEntry.setFormat("ZZZZZZZZZZ");

		assertFormattedInputEquals("", "AA");
		assertFormattedInputEquals("", "0");
		assertFormattedInputEquals("", "00");
		assertFormattedInputEquals("1", "01");
		assertFormattedInputEquals("1", "0.01");
		assertFormattedInputEquals("1", "0.1");
		assertFormattedInputEquals("10", "10");
		assertFormattedInputEquals("100", "100");
		assertFormattedInputEquals("1000", "1000");
		assertFormattedInputEquals("10000", "10000");
		assertFormattedInputEquals("1000000", "10000.00");
		assertFormattedInputEquals("1", "-01");
		assertFormattedInputEquals("1", "-0.01");
		assertFormattedInputEquals("1", "-0.1");
		assertFormattedInputEquals("10", "-10");
		assertFormattedInputEquals("100", "-100");
		assertFormattedInputEquals("1000", "-1000");
		assertFormattedInputEquals("10000", "-10000");
		assertFormattedInputEquals("1000000", "-10000.00");
		assertBackSpaceAfterInputEquals("", "10000");
	}

	public void test9() {
		m_numberEntry.setFormat("9");

		assertFormattedInputEquals("0", "AA");
		assertFormattedInputEquals("0", "0");
		assertFormattedInputEquals("0", "00");
		assertFormattedInputEquals("1", "01");
		assertFormattedInputEquals("1", "0.01");
		assertFormattedInputEquals("1", "0.1");
		assertFormattedInputEquals("1", "10");
		assertFormattedInputEquals("1", "100");
		assertFormattedInputEquals("1", "1000");
		assertFormattedInputEquals("1", "10000");
		assertFormattedInputEquals("1", "10000.00");
		assertFormattedInputEquals("0", "-0");
		assertFormattedInputEquals("0", "-00");
		assertFormattedInputEquals("1", "-01");
		assertFormattedInputEquals("1", "-0.01");
		assertFormattedInputEquals("1", "-0.1");
		assertFormattedInputEquals("1", "-10");
		assertFormattedInputEquals("1", "-100");
		assertFormattedInputEquals("1", "-1000");
		assertFormattedInputEquals("1", "-10000");
		assertFormattedInputEquals("1", "-10000.00");
		assertBackSpaceAfterInputEquals("0", "10000");
	}

	public void test99() {
		m_numberEntry.setFormat("99");

		assertFormattedInputEquals("00", "AA");
		assertFormattedInputEquals("00", "0");
		assertFormattedInputEquals("00", "00");
		assertFormattedInputEquals("01", "01");
		assertFormattedInputEquals("01", "0.01");
		assertFormattedInputEquals("01", "0.1");
		assertFormattedInputEquals("10", "10");
		assertFormattedInputEquals("10", "100");
		assertFormattedInputEquals("10", "1000");
		assertFormattedInputEquals("10", "10000");
		assertFormattedInputEquals("10", "10000.00");
		assertFormattedInputEquals("00", "-0");
		assertFormattedInputEquals("00", "-00");
		assertFormattedInputEquals("01", "-01");
		assertFormattedInputEquals("01", "-0.01");
		assertFormattedInputEquals("01", "-0.1");
		assertFormattedInputEquals("10", "-10");
		assertFormattedInputEquals("10", "-100");
		assertFormattedInputEquals("10", "-1000");
		assertFormattedInputEquals("10", "-10000");
		assertFormattedInputEquals("10", "-10000.00");
		assertBackSpaceAfterInputEquals("00", "10000");
	}

	public void testZ9() {
		m_numberEntry.setFormat("Z9");

		assertFormattedInputEquals("0", "AA");
		assertFormattedInputEquals("0", "0");
		assertFormattedInputEquals("0", "00");
		assertFormattedInputEquals("1", "01");
		assertFormattedInputEquals("1", "0.01");
		assertFormattedInputEquals("1", "0.1");
		assertFormattedInputEquals("10", "10");
		assertFormattedInputEquals("10", "100");
		assertFormattedInputEquals("10", "1000");
		assertFormattedInputEquals("10", "10000");
		assertFormattedInputEquals("10", "10000.00");
		assertFormattedInputEquals("0", "-0");
		assertFormattedInputEquals("0", "-00");
		assertFormattedInputEquals("1", "-01");
		assertFormattedInputEquals("1", "-0.01");
		assertFormattedInputEquals("1", "-0.1");
		assertFormattedInputEquals("10", "-10");
		assertFormattedInputEquals("10", "-100");
		assertFormattedInputEquals("10", "-1000");
		assertFormattedInputEquals("10", "-10000");
		assertFormattedInputEquals("10", "-10000.00");
		assertBackSpaceAfterInputEquals("0", "10000");
	}

	public void test9999() {
		m_numberEntry.setFormat("9999");

		assertFormattedInputEquals("0000", "AA");
		assertFormattedInputEquals("0000", "0");
		assertFormattedInputEquals("0000", "00");
		assertFormattedInputEquals("0001", "01");
		assertFormattedInputEquals("0001", "0.01");
		assertFormattedInputEquals("0001", "0.1");
		assertFormattedInputEquals("0010", "10");
		assertFormattedInputEquals("0100", "100");
		assertFormattedInputEquals("1000", "1000");
		assertFormattedInputEquals("1000", "10000");
		assertFormattedInputEquals("1000", "10000.00");
		assertFormattedInputEquals("0000", "-0");
		assertFormattedInputEquals("0000", "-00");
		assertFormattedInputEquals("0001", "-01");
		assertFormattedInputEquals("0001", "-0.01");
		assertFormattedInputEquals("0001", "-0.1");
		assertFormattedInputEquals("0010", "-10");
		assertFormattedInputEquals("0100", "-100");
		assertFormattedInputEquals("1000", "-1000");
		assertFormattedInputEquals("1000", "-10000");
		assertFormattedInputEquals("1000", "-10000.00");
		assertBackSpaceAfterInputEquals("0000", "10000");
	}

	public void testZZZ9() {
		m_numberEntry.setFormat("ZZZ9");

		assertFormattedInputEquals("0", "AA");
		assertFormattedInputEquals("0", "0");
		assertFormattedInputEquals("0", "00");
		assertFormattedInputEquals("1", "01");
		assertFormattedInputEquals("1", "0.01");
		assertFormattedInputEquals("1", "0.1");
		assertFormattedInputEquals("10", "10");
		assertFormattedInputEquals("100", "100");
		assertFormattedInputEquals("1000", "1000");
		assertFormattedInputEquals("1000", "10000");
		assertFormattedInputEquals("1000", "10000.00");
		assertFormattedInputEquals("0", "-0");
		assertFormattedInputEquals("0", "-00");
		assertFormattedInputEquals("1", "-01");
		assertFormattedInputEquals("1", "-0.01");
		assertFormattedInputEquals("1", "-0.1");
		assertFormattedInputEquals("10", "-10");
		assertFormattedInputEquals("100", "-100");
		assertFormattedInputEquals("1000", "-1000");
		assertFormattedInputEquals("1000", "-10000");
		assertFormattedInputEquals("1000", "-10000.00");
		assertBackSpaceAfterInputEquals("0", "10000");
	}

	public void test9999999999() {
		m_numberEntry.setFormat("9999999999");

		assertFormattedInputEquals("0000000000", "AA");
		assertFormattedInputEquals("0000000000", "0");
		assertFormattedInputEquals("0000000000", "00");
		assertFormattedInputEquals("0000000001", "01");
		assertFormattedInputEquals("0000000001", "0.01");
		assertFormattedInputEquals("0000000001", "0.1");
		assertFormattedInputEquals("0000000010", "10");
		assertFormattedInputEquals("0000000100", "100");
		assertFormattedInputEquals("0000001000", "1000");
		assertFormattedInputEquals("0000010000", "10000");
		assertFormattedInputEquals("0001000000", "10000.00");
		assertFormattedInputEquals("0000000000", "-0");
		assertFormattedInputEquals("0000000000", "-00");
		assertFormattedInputEquals("0000000001", "-01");
		assertFormattedInputEquals("0000000001", "-0.01");
		assertFormattedInputEquals("0000000001", "-0.1");
		assertFormattedInputEquals("0000000010", "-10");
		assertFormattedInputEquals("0000000100", "-100");
		assertFormattedInputEquals("0000001000", "-1000");
		assertFormattedInputEquals("0000010000", "-10000");
		assertFormattedInputEquals("0001000000", "-10000.00");
		assertBackSpaceAfterInputEquals("0000000000", "10000");
	}

	public void testZZZZZZZZZ9() {
		m_numberEntry.setFormat("ZZZZZZZZZ9");

		assertFormattedInputEquals("0", "AA");
		assertFormattedInputEquals("0", "0");
		assertFormattedInputEquals("0", "00");
		assertFormattedInputEquals("1", "01");
		assertFormattedInputEquals("1", "0.01");
		assertFormattedInputEquals("1", "0.1");
		assertFormattedInputEquals("10", "10");
		assertFormattedInputEquals("100", "100");
		assertFormattedInputEquals("1000", "1000");
		assertFormattedInputEquals("10000", "10000");
		assertFormattedInputEquals("1000000", "10000.00");
		assertFormattedInputEquals("0", "-0");
		assertFormattedInputEquals("0", "-00");
		assertFormattedInputEquals("1", "-01");
		assertFormattedInputEquals("1", "-0.01");
		assertFormattedInputEquals("1", "-0.1");
		assertFormattedInputEquals("10", "-10");
		assertFormattedInputEquals("100", "-100");
		assertFormattedInputEquals("1000", "-1000");
		assertFormattedInputEquals("10000", "-10000");
		assertFormattedInputEquals("1000000", "-10000.00");
		assertBackSpaceAfterInputEquals("0", "10000");
	}

	public void testZZZZZZZZ99() {
		m_numberEntry.setFormat("ZZZZZZZZ99");

		assertFormattedInputEquals("00", "AA");
		assertFormattedInputEquals("00", "0");
		assertFormattedInputEquals("00", "00");
		assertFormattedInputEquals("01", "01");
		assertFormattedInputEquals("01", "0.01");
		assertFormattedInputEquals("01", "0.1");
		assertFormattedInputEquals("10", "10");
		assertFormattedInputEquals("100", "100");
		assertFormattedInputEquals("1000", "1000");
		assertFormattedInputEquals("10000", "10000");
		assertFormattedInputEquals("1000000", "10000.00");
		assertFormattedInputEquals("00", "-0");
		assertFormattedInputEquals("00", "-00");
		assertFormattedInputEquals("01", "-01");
		assertFormattedInputEquals("01", "-0.01");
		assertFormattedInputEquals("01", "-0.1");
		assertFormattedInputEquals("10", "-10");
		assertFormattedInputEquals("100", "-100");
		assertFormattedInputEquals("1000", "-1000");
		assertFormattedInputEquals("10000", "-10000");
		assertFormattedInputEquals("1000000", "-10000.00");
		assertBackSpaceAfterInputEquals("00", "10000");
	}

	public void testZZZcZZZcZZZ() {
		m_numberEntry.setFormat("ZZ,ZZZ,ZZZ");

		assertFormattedInputEquals("", "AA");
		assertFormattedInputEquals("", "0");
		assertFormattedInputEquals("", "00");
		assertFormattedInputEquals("1", "01");
		assertFormattedInputEquals("1", "0.01");
		assertFormattedInputEquals("1", "0.1");
		assertFormattedInputEquals("10", "10");
		assertFormattedInputEquals("100", "100");
		assertFormattedInputEquals("1,000", "1000");
		assertFormattedInputEquals("10,000", "10000");
		assertFormattedInputEquals("1,000,000", "10000.00");
		assertFormattedInputEquals("", "-0");
		assertFormattedInputEquals("", "-00");
		assertFormattedInputEquals("1", "-01");
		assertFormattedInputEquals("1", "-0.01");
		assertFormattedInputEquals("1", "-0.1");
		assertFormattedInputEquals("10", "-10");
		assertFormattedInputEquals("100", "-100");
		assertFormattedInputEquals("1,000", "-1000");
		assertFormattedInputEquals("10,000", "-10000");
		assertFormattedInputEquals("1,000,000", "-10000.00");
		assertBackSpaceAfterInputEquals("", "10000");
	}

	public void testZZ99d99() {
		m_numberEntry.setFormat("ZZ99.99");

		assertFormattedInputEquals("00.00", "AA");
		assertFormattedInputEquals("00.00", "0");
		assertFormattedInputEquals("00.00", "00");
		assertFormattedInputEquals("01.00", "01");
		assertFormattedInputEquals("00.01", "0.01");
		assertFormattedInputEquals("00.10", "0.1");
		assertFormattedInputEquals("10.00", "10");
		assertFormattedInputEquals("100.00", "100");
		assertFormattedInputEquals("1000.00", "1000");
		assertFormattedInputEquals("1000.00", "10000");
		assertFormattedInputEquals("1000.00", "10000.00");
		assertFormattedInputEquals("00.00", "-0");
		assertFormattedInputEquals("00.00", "-00");
		assertFormattedInputEquals("01.00", "-01");
		assertFormattedInputEquals("00.01", "-0.01");
		assertFormattedInputEquals("00.10", "-0.1");
		assertFormattedInputEquals("10.00", "-10");
		assertFormattedInputEquals("100.00", "-100");
		assertFormattedInputEquals("1000.00", "-1000");
		assertFormattedInputEquals("1000.00", "-10000");
		assertFormattedInputEquals("1000.00", "-10000.00");
		assertBackSpaceAfterInputEquals("00.00", "10000");
	}

	public void testZZZ99d99999() {
		m_numberEntry.setFormat("ZZZ99.99999");

		assertFormattedInputEquals("00.00000", "AA");
		assertFormattedInputEquals("00.00000", "0");
		assertFormattedInputEquals("00.00000", "00");
		assertFormattedInputEquals("01.00000", "01");
		assertFormattedInputEquals("00.01000", "0.01");
		assertFormattedInputEquals("00.10000", "0.1");
		assertFormattedInputEquals("10.00000", "10");
		assertFormattedInputEquals("100.00000", "100");
		assertFormattedInputEquals("1000.00000", "1000");
		assertFormattedInputEquals("10000.00000", "10000");
		assertFormattedInputEquals("1000.00000", "1000.00");
		assertFormattedInputEquals("00.00000", "-0");
		assertFormattedInputEquals("00.00000", "-00");
		assertFormattedInputEquals("01.00000", "-01");
		assertFormattedInputEquals("00.01000", "-0.01");
		assertFormattedInputEquals("00.10000", "-0.1");
		assertFormattedInputEquals("10.00000", "-10");
		assertFormattedInputEquals("100.00000", "-100");
		assertFormattedInputEquals("1000.00000", "-1000");
		assertFormattedInputEquals("10000.00000", "-10000");
		assertFormattedInputEquals("10000.00000", "-10000.00");
		assertBackSpaceAfterInputEquals("00.00000", "10000");
	}

	public void test99d9() {
		m_numberEntry.setFormat("99.9");

		assertFormattedInputEquals("00.0", "AA");
		assertFormattedInputEquals("00.0", "0");
		assertFormattedInputEquals("00.0", "00");
		assertFormattedInputEquals("01.0", "01");
		assertFormattedInputEquals("00.0", "0.01");
		assertFormattedInputEquals("00.1", "0.1");
		assertFormattedInputEquals("10.0", "10");
		assertFormattedInputEquals("10.0", "100");
		assertFormattedInputEquals("10.0", "1000");
		assertFormattedInputEquals("10.0", "10000");
		assertFormattedInputEquals("10.0", "1000.00");
		assertFormattedInputEquals("00.0", "-0");
		assertFormattedInputEquals("00.0", "-00");
		assertFormattedInputEquals("01.0", "-01");
		assertFormattedInputEquals("00.0", "-0.01");
		assertFormattedInputEquals("00.1", "-0.1");
		assertFormattedInputEquals("10.0", "-10");
		assertFormattedInputEquals("10.0", "-100");
		assertFormattedInputEquals("10.0", "-1000");
		assertFormattedInputEquals("10.0", "-10000");
		assertFormattedInputEquals("10.0", "-10000.00");
		assertBackSpaceAfterInputEquals("00.0", "10000");
	}

	public void testZZdZ() {
		m_numberEntry.setFormat("ZZ.Z");

		assertFormattedInputEquals("", "AA");
		assertFormattedInputEquals("", "0");
		assertFormattedInputEquals("", "00");
		assertFormattedInputEquals("1", "01");
		assertFormattedInputEquals("", "0.01");
		assertFormattedInputEquals("0.1", "0.1");
		assertFormattedInputEquals("10", "10");
		assertFormattedInputEquals("10", "100");
		assertFormattedInputEquals("10", "1000");
		assertFormattedInputEquals("10", "10000");
		assertFormattedInputEquals("10", "1000.00");
		assertFormattedInputEquals("", "-0");
		assertFormattedInputEquals("", "-00");
		assertFormattedInputEquals("1", "-01");
		assertFormattedInputEquals("", "-0.01");
		assertFormattedInputEquals("0.1", "-0.1");
		assertFormattedInputEquals("10", "-10");
		assertFormattedInputEquals("10", "-100");
		assertFormattedInputEquals("10", "-1000");
		assertFormattedInputEquals("10", "-10000");
		assertFormattedInputEquals("10", "-10000.00");
		assertBackSpaceAfterInputEquals("", "10000");
	}

	public static void main (String[] args){
		TestRunner.run(TestNumberEntry.class);
	}
}
