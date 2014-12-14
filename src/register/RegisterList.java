package register;

import java.util.HashMap;

import simulator.AppContext;

public class RegisterList {
	public static final int NUM_LOGICAL_REGISTERS = 32;
	public static final int NUM_PHYSICAL_REGISTERS = 64;
	
	private HashMap<Integer, Register> logicalRegisters;
	private HashMap<Integer, Register> physicalRegisters;
	
	public RegisterList(AppContext appContext) {
		this.logicalRegisters = new HashMap<Integer, Register>();
		this.physicalRegisters = new HashMap<Integer, Register>();
		
		for (int i = 0; i < NUM_LOGICAL_REGISTERS; i++) {
			this.logicalRegisters.put(i, new Register(i));
		}
		
		for (int i = 0; i < NUM_PHYSICAL_REGISTERS; i++) {
			this.physicalRegisters.put(i, new Register(i));
		}
	}
	
	public Register getLogicalRegister(int id) {
		return logicalRegisters.get(id);
	}
	
	public Register getPhysicalRegister(int id) {
		return physicalRegisters.get(id);
	}
}
