package org.montsuqi.util;

public class ParameterConverter {
	// inhibit instantiation
	private ParameterConverter() {
		throw new InternalError();
	}

	public static int toInteger(String s) {
		return Integer.parseInt(s);
	}

	public static boolean toBoolean(String s) {
		return "ty".indexOf(s.charAt(0)) >= 0 || toInteger(s) != 0;
	}

	public double toDouble(String s) {
		return Double.parseDouble(s);
	}
}
