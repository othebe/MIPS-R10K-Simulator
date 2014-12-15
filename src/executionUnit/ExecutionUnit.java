package executionUnit;

import java.util.Iterator;

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
	public void edge() {
		super.edge();
		
		// If a rollback is going to be performed, no instructions can move to the graduator.
		if (appContext.branchHandler.performRollback) {
			instructions_n.clear();
			return;
		}
		
		Instruction instruction = getCompletedInstruction();
		if (instruction != null) {
			// Destination register is no longer busy.
			instruction.rd.setBusy(false);
			
			// Mark instruction for graduation.
			appContext.graduator.add(instruction);
			
			instructions_n.remove(instruction);
		}
	}
	
	@Override
	public String getIdentifier() {
		return "E";
	}
	
	public void clearMispredictedBranch(Instruction mispredicted) {
		Iterator<Instruction> iterator;
		
		iterator = instructions_r.iterator();
		while (iterator.hasNext()) {
			Instruction instruction = iterator.next();
			if (instruction.seqNum >= mispredicted.seqNum) {
				iterator.remove();
			}
		}
		
		iterator = instructions_n.iterator();
		while (iterator.hasNext()) {
			Instruction instruction = iterator.next();
			if (instruction.seqNum >= mispredicted.seqNum) {
				iterator.remove();
			}
		}
	}
}
