package org.montsuqi.monsia;

class Property {

	private static final String ARROW = "=>";

	Property(String name, String value) {
		this.name = name;
		this.value = value;
	}

	String getName() {
		return name;
	}
	
	String getValue() {
		return value;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(name.length() + value.length() + ARROW.length());
		buf.append(name);
		buf.append(ARROW);
		buf.append(value);
		return buf.toString();
	}
	private String name;
	private String value;
}
