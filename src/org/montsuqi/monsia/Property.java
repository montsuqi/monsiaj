package org.montsuqi.monsia;

class Property {

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

	private String name;
	private String value;
}
