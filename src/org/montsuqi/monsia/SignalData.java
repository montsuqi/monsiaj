/*      PANDA -- a simple transaction monitor
                                                                                
Copyright (C) 1998-1999 Ogochan.
			  2000-2003 Ogochan & JMA (Japan Medical Association).
                                                                                
This module is part of PANDA.
                                                                                
		PANDA is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY.  No author or distributor accepts responsibility
to anyone for the consequences of using it or for whether it serves
any particular purpose or works at all, unless he says so in writing.
Refer to the GNU General Public License for full details.
                                                                                
		Everyone is granted permission to copy, modify and redistribute
PANDA, but only under the conditions described in the GNU General
Public License.  A copy of this license is supposed to have been given
to you along with PANDA so you can know your rights and
responsibilities.  It should be in a file named COPYING.  Among other
things, the copyright notice and this notice must be preserved on all
copies.
*/

package org.montsuqi.monsia;

public class SignalData {

	public SignalData(Object signalObject, SignalInfo sInfo, AccelInfo accel) {
		this(signalObject, sInfo);
		this.accel = accel;
	}

	public SignalData(Object signalObject, SignalInfo sInfo) {
		this(signalObject, sInfo.getName(), sInfo.getObject(), sInfo.isAfter());
	}
	
	public SignalData(Object signalObject, String name, String connectObject, boolean after) {
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
