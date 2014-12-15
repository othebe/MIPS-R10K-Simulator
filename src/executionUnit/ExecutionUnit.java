package executionUnit;

import java.util.Iterator;

import instruction.Instruction;
import simulator.AppContext;
import simulator.SimUnit;

public abstract class ExecutionUnit extends SimUnit {
	private Instruction completedInstruction;
	private boolean graduateOnCompletion;
	
	protected Instruction[] pipeline;
	
	public ExecutionUnit(AppContext appContext, boolean graduateOnCompletion) {
		super(appContext);
		
		this.graduateOnCompletion = graduateOnCompletion;
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
		
		Instruction instruction = getCompletedInstruction();
		if (instruction != null && graduateOnCompletion) {
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
	
	@Override
	public void clearFromInstruction(Instruction instruction) {
		Iterator<Instruction> iterator;
		
		// Clear any instructions scheduled for to be issued.
		iterator = instructions_n.iterator();
		while (iterator.hasNext()) {
			Instruction queuedInstruction = iterator.next();
			if (queuedInstruction.seqNum >= instruction.seqNum) {
				iterator.remove();
			}
		}
		
		// Clear any instructions scheduled for to be issued.
		iterator = instructions_r.iterator();
		while (iterator.hasNext()) {
			Instruction queuedInstruction = iterator.next();
			if (queuedInstruction.seqNum >= instruction.seqNum) {
				iterator.remove();
			}
		}
		
		// Clear any newer instructions from the pipeline.
		for (int i = 0; i < pipeline.length; i++) {
			Instruction pipelined = pipeline[i];
			if (pipelined != null && pipelined.seqNum >= instruction.seqNum) {
				pipeline[i] = null;
			}
		}
		
		// Ignore any completed instructions.
		if (completedInstruction != null && completedInstruction.seqNum >= instruction.seqNum) {
			completedInstruction = null;
		}
	}
}
