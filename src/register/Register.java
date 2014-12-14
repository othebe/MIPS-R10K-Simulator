package register;

import simulator.AppContext;

public class Register {
	// Identifier for the register. May need special handling for Hi/Lo.
	private int id;
	
	// Determines if register is busy. Implementation of the busy bit table.
	private boolean isBusy;
	
	// Determines if bypass allowed.
	private boolean allowBypass;
	
	private AppContext appContext;
	
	public Register(AppContext appContext, int id) {
		this.appContext = appContext;
		this.id = id;
		this.isBusy = false;
		this.allowBypass = false;
	}
	
	// Set busy status.
	public void setBusy(boolean isBusy) {
		if (this != appContext.registerList.getImmRegister()) {
			this.isBusy = isBusy;
		}
	}
	
	// Check if register is busy.
	public boolean isBusy() {
		return this.isBusy;
	}
	
	// Set bypass status.
	public void setBypass(boolean allowBypass) {
		this.allowBypass = allowBypass;
	}
	
	// Check if bypass is allowed.
	public boolean allowBypass() {
		return this.allowBypass;
	}
}
