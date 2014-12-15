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
}
