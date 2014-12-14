package executionUnit;

import java.util.Iterator;

import simulator.AppContext;
import instruction.Instruction;
import instruction.InstructionType;

public class LoadStoreUnit extends ExecutionUnit {
	public LoadStoreUnit(AppContext appContext) {
		super(appContext);

		pipeline = new Instruction[2];
	}

	@Override
	public void calc() {
		super.calc();
		
		// If a new instruction was added, update dependencies.
		if (pipeline[0] != null) {
			Instruction instruction = pipeline[0];
			instructions_n.add(instruction);
			
			// Update dependency matrix.
			if (instruction.instructionType == InstructionType.LOAD) {
				appContext.addressQueue.addLoadDependency(instruction.rd);
			} 
			else if(instruction.instructionType == InstructionType.STORE) {
				appContext.addressQueue.addStoreDependency(instruction.extra);
			}
		}
		
		execute();
		
		// If an instruction completes execution, update dependencies.
		if (this.getCompletedInstruction() != null) {
			Instruction instruction = this.getCompletedInstruction();
			
			// Update dependency matrix.
			if (instruction.instructionType == InstructionType.LOAD) {
				appContext.addressQueue.removeLoadDependency(instruction.rd);
			} 
			else if(instruction.instructionType == InstructionType.STORE) {
				appContext.addressQueue.removeStoreDependency(instruction.extra);
			}
		}
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
