package org.montsuqi.client;

class ValueAttribute {
	private String key;
	private String nameSuffix;
	private String valueName;
	private int type;
	private Object opt;

	String getKey() {
		return key;
	}

	ValueAttribute(String key, String nameSuffix, String valueName, int type, Object opt) {
		this.key = key;
		this.nameSuffix = nameSuffix;
		this.valueName = valueName;
		this.type = type;
		this.opt = opt;
	}

	void setOpt(int type, Object opt) {
		this.type = type;
		this.opt = opt;
	}

	void setNameSuffix(String nameSuffix) {
		this.nameSuffix = nameSuffix;
	}

	String getNameSuffix() {
		return nameSuffix;
	}

	String getValueName() {
		return valueName;
	}

	int getType() {
		return type;
	}

	void setOpt(Object opt) {
		this.opt = opt;
	}

	Object getOpt() {
		return opt;
	}
}
