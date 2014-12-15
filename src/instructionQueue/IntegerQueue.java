package instructionQueue;

import instruction.Instruction;
import instruction.InstructionType;

import java.util.Iterator;

import executionUnit.AddressCalculationUnit;
import executionUnit.AluUnit;
import executionUnit.ExecutionUnit;
import executionUnit.LoadStoreUnit;
import simulator.AppContext;
import simulator.SimUnit;

public class IntegerQueue extends InstructionQueue {
	private AluUnit alu1;
	private AluUnit alu2;
	
	public IntegerQueue(AppContext appContext) {
		super(appContext);
		
		this.alu1 = new AluUnit(appContext, /** pipelines */ 1, /** allowBypass */ false);
		this.alu2 = new AluUnit(appContext, /** pipelines */ 1, /** allowBypass */ false);
		
		this.executionUnits = new ExecutionUnit[2];
		this.executionUnits[0] = this.alu1;
		this.executionUnits[1] = this.alu2;
	}
	
	@Override
	public void calc() {
		super.calc();
		
		Iterator<Instruction> iterator = instructions_r.iterator();
		while (iterator.hasNext()) {
			Instruction instruction = iterator.next();
			
			instructions_n.add(instruction);
			iterator.remove();
		}
		
		this.alu1.calc();		
		this.alu2.calc();
		
		// Check for branch resolution.
		if (this.alu1.getCompletedInstruction() != null) {
			Instruction completedInstruction = this.alu1.getCompletedInstruction();
			if (completedInstruction.isMispredicted()) {
				appContext.branchHandler.performRollback = true;
				instructions_n.remove(completedInstruction);
			}
		}
	}
	
	@Override
	public void edge() {
		super.edge();
		
		// Issue to ALU-1.
		if (alu1.canIssue() && !instructions_n.isEmpty()) {
			// Prioritize branches.
			Instruction issuable = null;
			Iterator<Instruction> iterator = instructions_n.iterator();
			while (iterator.hasNext()) {
				Instruction instruction = iterator.next();
				if (!instruction.operandsAvailable()) continue;
				
				if (issuable == null) {
					issuable = instruction;
				}
				
				if (instruction.instructionType == InstructionType.BRANCH) {
					issuable = instruction;
					break;
				}
			}
			
			if (issuable != null) {
				if (issuable.instructionType == InstructionType.BRANCH) {
					//appContext.branchHandler.addFrame(issuable);
				}
				alu1.issue(issuable);
				instructions_n.remove(issuable);
				dequeue(issuable);
			}
		}
		
		// Issue to ALU-2.
		if (alu2.canIssue() && !instructions_n.isEmpty()) {
			// Branches not allowed on this ALU.
			Instruction issuable = null;
			Iterator<Instruction> iterator = instructions_n.iterator();
			while (iterator.hasNext()) {
				Instruction instruction = iterator.next();
				if (!instruction.operandsAvailable()) continue;
				
				// Branches not allowed on this ALU.
				if (instruction.instructionType == InstructionType.BRANCH) continue;
				
				if (issuable == null) {
					issuable = instruction;
				}
				
				if (instruction.instructionType != InstructionType.BRANCH) {
					issuable = instruction;
					break;
				}
			}
			
			if (issuable != null) {
				alu2.issue(issuable);
				instructions_n.remove(issuable);
				dequeue(issuable);
			}
		}
		
		this.alu1.edge();
		this.alu2.edge();
	}
	
	@Override
	public SimUnit clone(AppContext appContext) {
		IntegerQueue cloned = null;
		
		try {
			cloned = (IntegerQueue) super.clone();
			
			// Rewrite AppContext for sim units.
			cloned.appContext = appContext;
			cloned.alu1 = (AluUnit) cloned.alu1.clone(appContext);
			cloned.alu2 = (AluUnit) cloned.alu2.clone(appContext);
		} catch (CloneNotSupportedException e) {}
		
		return cloned;
	}
}
