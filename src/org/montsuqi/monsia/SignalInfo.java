package org.montsuqi.monsia;

class SignalInfo {

	SignalInfo(String name, String handler, String object, boolean after) {
		this.name = name;
		this.handler = handler;
		this.object = object;
		this.after = after;
	}

	String getName() {
		return name;
	}

	String getHandler() {
		return handler;
	}

	String getObject() {
		return object;
	}

	boolean isAfter() {
		return after;
	}
	
	private final String name;
	private final String handler;
	private final String object;
	private final boolean after;
}
