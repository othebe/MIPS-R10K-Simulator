package executionUnit;

import instruction.Instruction;
import simulator.AppContext;
import simulator.SimUnit;

public abstract class ExecutionUnit extends SimUnit {
	private Instruction completedInstruction;
	
	protected Instruction[] pipeline;
	
	public ExecutionUnit(AppContext appContext) {
		super(appContext);
	}
	
	public boolean canIssue() {
		return pipeline[0] == null;
	}
	
	public void issue(Instruction instruction) {
		instructions_r.add(instruction);
		pipeline[0] = instruction;
	}
	
	protected void execute() {
		this.completedInstruction = pipeline[pipeline.length - 1];
		
		// Move instructions through the pipeline.
		for (int i = pipeline.length - 1; i >= 0; i--) {
			if (i == 0) {
				pipeline[i] = null;
			} else {
				pipeline[i] = pipeline[i - 1];
			}
		}
		
		if (this.completedInstruction != null) {
			this.completedInstruction.rd.setBusy(false);
		}
	}
	
	public Instruction getCompletedInstruction() {
		return this.completedInstruction;
	}
	
	@Override
	public String getIdentifier() {
		return "E";
	}
}
