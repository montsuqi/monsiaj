package org.montsuqi.monsia;

class SignalData {

	SignalData(Object signalObject, SignalInfo sInfo) {
		this(signalObject, sInfo.getName(), sInfo.getObject(), sInfo.isAfter());
	}
	
	SignalData(Object signalObject, String name, String connectObject, boolean after) {
		this.signalObject = signalObject;
		this.name = name;
		this.connectObject = connectObject;
		this.after = after;
	}

	Object getSignalObject() {
		return signalObject;
	}

	String getName() {
		return name;
	}

	String getConnectObject() {
		return connectObject;
	}

	boolean isAfter() {
		return after;
	}

	private final Object signalObject;
	private final String name;
	private final String connectObject;
	private final boolean after;
}
