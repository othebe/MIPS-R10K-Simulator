/**
 * Free list.
 * 
 */

package register;

import java.util.LinkedList;

import simulator.AppContext;

public class FreeList {
	// Number of registers available to the free list.
	private static final int NUM_REGISTERS = 64;
	
	private AppContext appContext;
	private LinkedList<Register> registers;
	
	public FreeList(AppContext appContext) {
		this.appContext = appContext;
		
		this.registers = new LinkedList<Register>();

		for (int i = 1; i <= appContext.registerList.NUM_PHYSICAL_REGISTERS; i++) {
			this.addRegister(appContext.registerList.getPhysicalRegister(i));
		}
	}
	
	public void addRegister(Register r) {
		this.registers.add(r);
	}
	
	public Register removeRegister() {
		Register register = this.registers.pop();
		register.setBusy(true);
		
		return register;
	}
	
	public boolean hasFreeRegister() {
		return this.registers.peek() != null;
	}
}
