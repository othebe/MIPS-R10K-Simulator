package register;

import java.util.HashMap;

import simulator.AppContext;

public class RegisterList {
	public static final int NUM_LOGICAL_REGISTERS = 32;
	public static final int NUM_PHYSICAL_REGISTERS = 64;
	
	// This register represents an immediate value.
	private Register immRegister;
	
	private HashMap<Integer, Register> logicalRegisters;
	private HashMap<Integer, Register> physicalRegisters;
	
	public RegisterList(AppContext appContext) {
		this.immRegister = new Register(appContext, 0);
				
		this.logicalRegisters = new HashMap<Integer, Register>();
		this.physicalRegisters = new HashMap<Integer, Register>();
		
		for (int i = 0; i < NUM_LOGICAL_REGISTERS; i++) {
			this.logicalRegisters.put(i, new Register(appContext, i));
		}
		
		for (int i = 1; i <= NUM_PHYSICAL_REGISTERS; i++) {
			this.physicalRegisters.put(i, new Register(appContext, i));
		}
	}
	
	public Register getImmRegister() {
		return this.immRegister;
	}
	
	public Register getLogicalRegister(int id) {
		if (id == 0) {
			return this.immRegister;
		} else {
			return logicalRegisters.get(id);
		}
	}
	
	public Register getPhysicalRegister(int id) {
		if (id == 0) {
			return this.immRegister;
		} else {
			return physicalRegisters.get(id);
		}
	}
}
