package org.montsuqi.monsia;

class AccelInfo {
	public AccelInfo(int key, int modifiers, String signal) {
		this.key = key;
		this.modifiers = modifiers;
		this.signal = signal;
	}

	public int getKey() {
		return key;
	}
	public int getModifiers() {
		return modifiers;
	}
	public String getSignal() {
		return signal;
	}

    final int key;
    final int modifiers;
    final String signal;
}
