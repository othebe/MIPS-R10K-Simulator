package instructionQueue;

import instruction.Instruction;
import instruction.InstructionType;

import java.util.Iterator;

import executionUnit.AluUnit;
import executionUnit.ExecutionUnit;
import simulator.AppContext;
import simulator.SimUnit;

public class FloatingQueue extends InstructionQueue {
	private AluUnit alu1;
	private AluUnit alu2;
	
	public FloatingQueue(AppContext appContext) {
		super(appContext);
		
		this.alu1 = new AluUnit(appContext, /** pipelines */ 3, /** allowBypass */ true);
		this.alu2 = new AluUnit(appContext, /** pipelines */ 3, /** allowBypass */ true);
		
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
	}
	
	@Override
	public void edge() {
		super.edge();
		
		// Issue to ALU-1.
		if (alu1.canIssue() && !instructions_n.isEmpty()) {
			Iterator<Instruction> iterator = instructions_n.iterator();
			while (iterator.hasNext()) {
				Instruction instruction = iterator.next();
				
				if (instruction.operandsAvailable()) {
					alu1.issue(instruction);
					iterator.remove();
					dequeue(instruction);
					break;
				}
			}
		}
		
		// Issue to ALU-2.
		if (alu2.canIssue() && !instructions_n.isEmpty()) {
			Iterator<Instruction> iterator = instructions_n.iterator();
			while (iterator.hasNext()) {
				Instruction instruction = iterator.next();
				
				boolean rsAvailable = !instruction.rs.isBusy() || instruction.rs.allowBypass();
				boolean rtAvailable = !instruction.rt.isBusy() || instruction.rt.allowBypass();
				
				if (instruction.operandsAvailable()) {
					alu2.issue(instruction);
					iterator.remove();
					dequeue(instruction);
					break;
				}
			}
		}
		
		this.alu1.edge();
		this.alu2.edge();
	}

	@Override
	public String getIdentifier() {
		return "I";
	}
}
