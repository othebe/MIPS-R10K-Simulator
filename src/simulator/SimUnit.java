package simulator;

import instruction.Instruction;

import java.util.ArrayList;

public abstract class SimUnit implements Cloneable {
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
	
	public void clear() {
		this.instructions_r.clear();
		this.instructions_n.clear();
	}
	
	public SimUnit clone(AppContext appContext) {
		SimUnit cloned = null;
		
		try {
			cloned = (SimUnit) super.clone();
			
			// Rewrite AppContext for sim units.
			cloned.appContext = appContext;
		} catch (CloneNotSupportedException e) {}
		
		return cloned;
	}
	
	public abstract String getIdentifier();
}
