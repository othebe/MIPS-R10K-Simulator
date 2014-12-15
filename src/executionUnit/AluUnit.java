package executionUnit;

import instruction.Instruction;
import simulator.AppContext;

public class AluUnit extends ExecutionUnit {
	private boolean allowBypass;
	
	public AluUnit(AppContext appContext, int pipelines, boolean allowBypass) {
		super(appContext, true);
		
		pipeline = new Instruction[pipelines];
		this.allowBypass = allowBypass;
	}
	
	@Override
	public void calc() {
		super.calc();
		
		// If a new instruction was added, update dependencies.
		if (pipeline[0] != null) {
			instructions_n.add(pipeline[0]);
		}
		
		execute();
		
		// Check for branch mispredictions.
		if (getCompletedInstruction() != null) {
			Instruction completedInstruction = getCompletedInstruction();
			if (completedInstruction.isMispredicted()) {
				//instructions_n.remove(completedInstruction);
			}
		}
		
		if (allowBypass) {
			Instruction instruction = pipeline[pipeline.length - 1];
			if (instruction != null) {
				instruction.rd.setBypass(true);
			}
		}
		
		instructions_r.clear();
	}
}
