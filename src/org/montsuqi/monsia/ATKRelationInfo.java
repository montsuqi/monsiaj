package org.montsuqi.monsia;

/**
 * Placeholder class for GladeATKRElationInfo.
 * Java version does not use it.
 */
class ATKRelationInfo {
	ATKRelationInfo(String target, String type) {
		this.target = target;
		this.type = type;
	}

	String getTarget() {
		return target;
	}
	
	String getType() {
		return type;
	}

	private final String target;
    private final String type;
}
