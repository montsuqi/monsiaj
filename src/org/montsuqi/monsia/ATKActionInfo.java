package org.montsuqi.monsia;

/**
 * Placeholder class for GladeATKActionInfo.
 * Java version does not use it.
 */
class ATKActionInfo {
	public ATKActionInfo(String actionName, String description) {
		this.actionName = actionName;
		this.description = description;
	}

	public String getActionName() {
		return actionName;
	}
	
	public String getDescription() {
		return description;
	}

    final String actionName;
    final String description;
}
