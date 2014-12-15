/**
 * An abstract class defining an instruction queue.
 */

package instructionQueue;

import instruction.Instruction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import executionUnit.ExecutionUnit;
import simulator.AppContext;
import simulator.SimUnit;

public abstract class InstructionQueue extends SimUnit{
	protected final static int SIZE = 16;
	
	protected ArrayList<Instruction> queuedInstructions;
	protected ExecutionUnit[] executionUnits;
	
	public InstructionQueue(AppContext appContext) {
		super(appContext);
		
		this.appContext = appContext;
		this.queuedInstructions = new ArrayList<Instruction>();
	}
	
	@Override
	public String getIdentifier() {
		return "I";
	}
	
	// Determine if instructions can be added to queue.
	public boolean canAdd() {
		return (this.queuedInstructions.size() < SIZE);
	}
	
	// Add instruction to the queue.
	public void enqueue(Instruction instruction) {
		this.instructions_r.add(instruction);
		
		if (!this.queuedInstructions.contains(instruction)) {
			this.queuedInstructions.add(instruction);
		}
	}
	
	// Remove instruction from queue.
	public void dequeue(Instruction instruction) {
		this.queuedInstructions.remove(instruction);
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
		
		// Clear any newer instructions from queue.
		iterator = queuedInstructions.iterator();
		while (iterator.hasNext()) {
			Instruction queuedInstruction = iterator.next();
			if (queuedInstruction.seqNum >= instruction.seqNum) {
				iterator.remove();
			}
		}
		// Clear execution units.
		for (int i = 0; i < executionUnits.length; i++) {
			executionUnits[i].clearFromInstruction(instruction);
		}
	}
}
