package simulator;

import java.util.Iterator;
import java.util.LinkedList;

import instruction.Instruction;

public class BranchHandler extends SimUnit {
	private LinkedList<ExecutionFrame> executionStack;
	public boolean performRollback;
	
	public BranchHandler(AppContext appContext) {
		super(appContext);
		
		this.performRollback = false;
		this.executionStack = new LinkedList<ExecutionFrame>();
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
	
	public AppContext getRollback() {
		AppContext original = this.appContext;
		
		if (performRollback) {
			ExecutionFrame executionFrame = executionStack.pop();
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
		executionStack.push(new ExecutionFrame(appContext, branchInstruction));
	}
	
	/**
	 * Execution frame for branch stack.
	 */
	private class ExecutionFrame {
		private AppContext appContext;
		private Instruction resetInstruction;
		
		public ExecutionFrame(AppContext appContext, Instruction resetInstruction) {
			this.appContext = appContext.clone();
			this.resetInstruction = resetInstruction;
		}
		
		public AppContext getAppContext() {
			return this.appContext;
		}
		
		public Instruction getResetInstruction() {
			return this.resetInstruction;
		}
	}
}
