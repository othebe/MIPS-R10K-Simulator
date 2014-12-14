package executionUnit;

import java.util.Iterator;

import simulator.AppContext;
import instruction.Instruction;
import instruction.InstructionType;

public class LoadStoreUnit extends ExecutionUnit {
	public LoadStoreUnit(AppContext appContext) {
		super(appContext);

		pipeline = new Instruction[1];
	}

	@Override
	public void calc() {
		super.calc();
		
		if (pipeline[0] != null) {
			Instruction instruction = pipeline[0];
			instructions_n.add(instruction);
		}
		
		execute();
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
