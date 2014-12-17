package simulator;

import java.util.Iterator;
import java.util.LinkedList;

import instruction.Instruction;

public class BranchHandler extends SimUnit {
	private static final int SIZE = 4;
	
	private LinkedList<Instruction> branchStack;
	private Instruction rollbackInstruction;
	
	public BranchHandler(AppContext appContext) {
		super(appContext);
		
		this.rollbackInstruction = null;
		this.branchStack = new LinkedList<Instruction>();
	}
	
	@Override
	public String getIdentifier() {
		return "R";
	}
	
	@Override
	public void calc() {
		super.calc();
		
		if (!instructions_r.isEmpty()) {
			rollbackInstruction = instructions_r.remove(0);
		}
	}
	
	@Override
	public void edge() {
		if (rollbackInstruction != null) {
			// Mark instructions that rollback.
			LinkedList<Instruction> running = appContext.activeList.getRunning();
			Iterator<Instruction> iterator = running.iterator();
			while (iterator.hasNext()) {
				Instruction runningInstruction = iterator.next();
				if (runningInstruction.seqNum >= rollbackInstruction.seqNum) {
					instructions_n.add(runningInstruction);
				}
			}
			
			super.edge();
			
			rollback(rollbackInstruction);
			rollbackInstruction = null;
		}
		
		instructions_n.clear();
	}
	
	// Determine if the branch stack has free space.
	public boolean canAdd() {
		return this.branchStack.size() < SIZE;
	}
	
	// Rollback all instructions after a given instruction.
	public void markForRollback(Instruction instruction) {
		instructions_r.add(instruction);
	}
	
	// Rollback a branch.
	private void rollback(Instruction branchInstruction) {					
		// Refetch instructions.
		appContext.fetcher.clearFromInstruction(branchInstruction);
		
		// Clear decoded instructions.
		appContext.decoder.clearFromInstruction(branchInstruction);
		
		// Clear active list.
		appContext.activeList.clearFromInstruction(branchInstruction);
		
		// Clear free list.
		appContext.freeList.clearFromInstruction(branchInstruction);
		
		// Clear instruction queues.
		appContext.integerQueue.clearFromInstruction(branchInstruction);
		appContext.floatingQueue.clearFromInstruction(branchInstruction);
		appContext.addressQueue.clearFromInstruction(branchInstruction);
		
		// Abort graduating instructions.
		appContext.graduator.clearFromInstruction(rollbackInstruction);
		
		// Update branch stack.
		clearFromInstruction(branchInstruction);
	}
	
	// Allocate space on the branch stack.
	public void addBranchFrame(Instruction branchInstruction) {
		branchStack.push(branchInstruction);
	}
	
	// Branch has been resolved and committed.
	public void resolveBranch(Instruction instruction) {
		branchStack.remove(instruction);
	}

	@Override
	public void clearFromInstruction(Instruction instruction) {
		Iterator<Instruction> iterator;
		
		// Clear any newer branches.
		iterator = branchStack.iterator();
		while (iterator.hasNext()) {
			Instruction branchInstruction = iterator.next();
			if (branchInstruction.seqNum > instruction.seqNum) {
				if (branchInstruction.flipped) {
					branchInstruction.flip();
				}
				iterator.remove();
			}
		}
		branchStack.remove(instruction);
		
		iterator = instructions_r.iterator();
		while (iterator.hasNext()) {
			Instruction branchInstruction = iterator.next();
			if (branchInstruction.seqNum > instruction.seqNum) {
				if (branchInstruction.flipped) {
					branchInstruction.flip();
				}
				iterator.remove();
			}
		}
	}
}
