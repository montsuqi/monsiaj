package org.montsuqi.monsia;

class SignalInfo {

	public SignalInfo(String name, String handler, String object, boolean after) {
		this.name = name;
		this.handler = handler;
		this.object = object;
		this.after = after;
	}

	public String getName() {
		return name;
	}

	public String getHandler() {
		return handler;
	}

	public String getObject() {
		return object;
	}

	public boolean isAfter() {
		return after;
	}
	
    final String name;
	final String handler;
    final String object;
	final boolean after;
}
