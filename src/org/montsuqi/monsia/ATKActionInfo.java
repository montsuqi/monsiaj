package org.montsuqi.monsia;

/**
 * Placeholder class for GladeATKActionInfo.
 * Java version does not use it.
 */
class ATKActionInfo {
	ATKActionInfo(String actionName, String description) {
		this.actionName = actionName;
		this.description = description;
	}

	String getActionName() {
		return actionName;
	}
	
	String getDescription() {
		return description;
	}

    private final String actionName;
    private final String description;
}
