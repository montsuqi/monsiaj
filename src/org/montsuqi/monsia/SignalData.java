package org.montsuqi.monsia;

class SignalData {

	SignalData(Object signalObject, SignalInfo sInfo, AccelInfo accel) {
		this(signalObject, sInfo);
		this.accel = accel;
	}
	SignalData(Object signalObject, SignalInfo sInfo) {
		this(signalObject, sInfo.getName(), sInfo.getObject(), sInfo.isAfter());
	}
	
	SignalData(Object signalObject, String name, String connectObject, boolean after) {
		this.signalObject = signalObject;
		this.name = name;
		this.connectObject = connectObject;
		this.after = after;
		this.accel = null;
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

	boolean hasAccel() {
		return accel != null;
	}

	AccelInfo getAccel() {
		if ( ! hasAccel()) {
			throw new IllegalStateException();
		}
		return accel;
	}

	private final Object signalObject;
	private final String name;
	private final String connectObject;
	private final boolean after;
	private AccelInfo accel;
}
