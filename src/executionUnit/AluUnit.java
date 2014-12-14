package executionUnit;

import instruction.Instruction;
import simulator.AppContext;

public class AluUnit extends ExecutionUnit {
	private boolean allowBypass;
	
	public AluUnit(AppContext appContext, int pipelines, boolean allowBypass) {
		super(appContext);
		
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
		
		if (allowBypass) {
			Instruction instruction = pipeline[pipeline.length - 1];
			if (instruction != null) {
				instruction.rd.setBypass(true);
			}
		}
		
		instructions_r.clear();
	}
	
	@Override
	public void edge() {
		super.edge();
		
		Instruction instruction = getCompletedInstruction();
		if (instruction != null) {
			// Destination register is no longer busy.
			instruction.rd.setBusy(false);
			
			// Mark instruction for graduation.
			appContext.graduator.add(instruction);
			
			instructions_n.remove(instruction);
		}
	}
}
