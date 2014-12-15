package simulator;

import java.util.Iterator;
import java.util.LinkedList;

import instruction.Instruction;

public class BranchHandler extends SimUnit {
	private LinkedList<Instruction> branchStack;
	public boolean performRollback;
	
	public BranchHandler(AppContext appContext) {
		super(appContext);
		
		this.performRollback = false;
		this.branchStack = new LinkedList<Instruction>();
	}
	
	@Override
	public void edge() {
		super.edge();

		instructions_n.clear();
	}

	@Override
	public String getIdentifier() {
		return "R";
	}
	
	public static void rollback(AppContext appContext) {
		
		
		AppContext original = this.appContext;
		
		if (performRollback) {
			ExecutionFrame executionFrame = branchStack.pop();
			Instruction resetInstruction = executionFrame.getResetInstruction();
			
			original = executionFrame.getAppContext();
			
			// Reset branch bit and instruction pointer.
			original.fetcher.resetFromInstruction(resetInstruction);
			
			// Clear queues of any newer instructions.
			original.integerQueue.clearMispredictedBranch(resetInstruction);
			original.floatingQueue.clearMispredictedBranch(resetInstruction);
			original.addressQueue.clearMispredictedBranch(resetInstruction);
		}
		
		return original;
	}
	
	public void addFrame(Instruction branchInstruction) {
		branchStack.push(branchInstruction);
	}
}
