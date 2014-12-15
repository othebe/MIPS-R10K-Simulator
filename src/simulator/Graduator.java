package simulator;

import instruction.Instruction;
import instruction.InstructionType;

import java.util.ArrayList;
import java.util.Iterator;

import register.Register;

public class Graduator extends SimUnit {
	private static final int LIMIT = 4;
	
	private int gradSeqNum;
	
	public Graduator(AppContext appContext) {
		super(appContext);
		
		this.gradSeqNum = 0;
	}
	
	// Mark an instruction as ready for graduation.
	public void add(Instruction instruction) {
		if (instructions_r.size() == 0) {
			instructions_r.add(instruction);
			return;
		}
		
		boolean added = false;
		for (int i = 0; i < instructions_r.size(); i++) {
			if (instruction.seqNum < instructions_r.get(i).seqNum) {
				instructions_r.add(i, instruction);
				added = true;
				break;
			}
		}
		
		if (!added) instructions_r.add(instruction);
	}
	
	public int getLastGraduated() {
		return this.gradSeqNum;
	}
	
	@Override
	public void calc() {
		super.calc();
		
		int count = 0;
		Iterator<Instruction> iterator = instructions_r.iterator();
		while (iterator.hasNext()) {
			Instruction instruction = iterator.next();
			if (instruction.seqNum == gradSeqNum) {
				instructions_n.add(instruction);
				iterator.remove();
				gradSeqNum++;
				count++;
				
				if (count == LIMIT) break;
			}
		}
	}
	
	@Override
	public void edge() {
		super.edge();
		
		for (int i = 0; i < instructions_n.size(); i++) {
			Instruction instruction = instructions_n.get(i);
			
			if (instruction.instructionType == InstructionType.LOAD || instruction.instructionType == InstructionType.STORE) {
				// If dealing with load or stores, remove from address queue.
				appContext.addressQueue.dequeue(instruction);
				
				// If graduating a store, remove from dependency matrix.
				if (instruction.instructionType == InstructionType.STORE) {
					appContext.addressQueue.removeDependency(instruction);
				}
			}
			
			// Remove from active list.
			appContext.activeList.dequeue(instruction);
			
			// Re-add to the free list.
			if (instruction.renamed) {
				appContext.freeList.addRegister(instruction.rd);
			}
			
			System.out.println(instruction.seqNum);
		}
		
		instructions_n.clear();
	}

	@Override
	public String getIdentifier() {
		return "C";
	}
}
