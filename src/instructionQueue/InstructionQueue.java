/**
 * An abstract class defining an instruction queue.
 */

package instructionQueue;

import instruction.Instruction;

import java.util.ArrayList;
import java.util.LinkedList;

import simulator.AppContext;
import simulator.SimUnit;

public abstract class InstructionQueue extends SimUnit{
	protected final static int SIZE = 16;
	
	protected ArrayList<Instruction> queuedInstructions;
	
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
}
