package org.montsuqi.monsia;

public class SignalData {

	public SignalData(Object signalObject, SignalInfo sInfo) {
		this(signalObject, sInfo.getName(), sInfo.getObject(), sInfo.isAfter());
	}
	
	public SignalData(Object signalObject, String name, String connectObject, boolean after) {
		this.signalObject = signalObject;
		this.name = name;
		this.connectObject = connectObject;
		this.after = after;
	}

	public Object getSignalObject() {
		return signalObject;
	}

	public String getName() {
		return name;
	}

	public String getConnectObject() {
		return connectObject;
	}

	public boolean isAfter() {
		return after;
	}

    final Object signalObject;
    final String name;
    final String connectObject;
    final boolean after;
}
