package org.montsuqi.monsia;

class Property {

	public Property(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}

	private String name;
	private String value;
}
