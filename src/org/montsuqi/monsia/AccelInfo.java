package org.montsuqi.monsia;

class AccelInfo {
	AccelInfo(int key, int modifiers, String signal) {
		this.key = key;
		this.modifiers = modifiers;
		this.signal = signal;
	}

	int getKey() {
		return key;
	}

	int getModifiers() {
		return modifiers;
	}

	String getSignal() {
		return signal;
	}

    private final int key;
    private final int modifiers;
    private final String signal;
}
