package simulator;

import instruction.Instruction;

import java.util.ArrayList;

public abstract class SimUnit {
	protected AppContext appContext;
	
	protected ArrayList<Instruction> instructions_r;
	protected ArrayList<Instruction> instructions_n;
	
	public SimUnit(AppContext appContext) {
		this.appContext = appContext;
		
		this.instructions_r = new ArrayList<Instruction>();
		this.instructions_n = new ArrayList<Instruction>();
	}
	
	public void calc() {
		
	}
	
	public void edge() {
		for (int i = 0; i < instructions_n.size(); i++) {
			Instruction instruction = instructions_n.get(i);
			appContext.timeLogger.log(instruction, this);
		}
	}
	
	// Clears pipelines from this instruction (inclusive) onwards.
	public abstract void clearFromInstruction(Instruction instruction);
	
	public abstract String getIdentifier();
}
