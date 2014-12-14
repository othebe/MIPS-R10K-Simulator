/**
 * An abstract class defining an instruction queue.
 */

package instructionQueue;

import instruction.Instruction;

import java.util.LinkedList;

import simulator.AppContext;
import simulator.SimUnit;

public abstract class InstructionQueue extends SimUnit{
	private final static int SIZE = 16;
	
	protected AppContext appContext;
	
	public InstructionQueue(AppContext appContext) {
		super(appContext);
		
		this.appContext = appContext;
	}
	
	@Override
	public String getIdentifier() {
		return "I";
	}
	
	// Determine if instructions can be added to queue.
	public boolean canAdd() {
		return (this.instructions_r.size() + this.instructions_n.size()) < SIZE;
	}
	
	// Add instruction to the queue.
	public void enqueue(Instruction instruction) {
		this.instructions_r.add(instruction);
	}
}
