package instructionQueue;

import instruction.Instruction;
import instruction.InstructionType;

import java.util.Iterator;

import executionUnit.AluUnit;
import simulator.AppContext;

public class IntegerQueue extends InstructionQueue {
	private AluUnit alu1;
	private AluUnit alu2;
	
	public IntegerQueue(AppContext appContext) {
		super(appContext);
		
		this.alu1 = new AluUnit(appContext, /** pipelines */ 2, /** allowBypass */ false);
		this.alu2 = new AluUnit(appContext, /** pipelines */ 2, /** allowBypass */ false);
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
				if (instruction.rs.isBusy() || instruction.rt.isBusy()) continue;
				
				if (issuable == null) {
					issuable = instruction;
				}
				
				if (instruction.instructionType == InstructionType.BRANCH) {
					issuable = instruction;
					break;
				}
			}
			
			if (issuable != null) {
				alu1.issue(issuable);
				instructions_n.remove(issuable);
			}
		}
		
		// Issue to ALU-2.
		if (alu2.canIssue() && !instructions_n.isEmpty()) {
			// Branches not allowed on this ALU.
			Instruction issuable = null;
			Iterator<Instruction> iterator = instructions_n.iterator();
			while (iterator.hasNext()) {
				Instruction instruction = iterator.next();
				if (instruction.rs.isBusy() || instruction.rt.isBusy()) continue;
				
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
			}
		}
		
		this.alu1.edge();
		this.alu2.edge();
	}
}
