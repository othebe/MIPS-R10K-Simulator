package executionUnit;

import instruction.Instruction;
import simulator.AppContext;

public class AddressCalculationUnit extends ExecutionUnit {
	public AddressCalculationUnit(AppContext appContext) {
		super(appContext);
		
		pipeline = new Instruction[1];
	}
	
	@Override
	public void calc() {
		super.calc();
		
		execute();
		
		Instruction completedInstruction = this.getCompletedInstruction();
		if (completedInstruction != null) {
			this.instructions_n.add(completedInstruction);
		}
		
		instructions_r.clear();
	}
	
	@Override
	public void edge() {
		super.edge();
		
		instructions_n.clear();
	}
	
	@Override
	public String getIdentifier() {
		return "A";
	}
}
