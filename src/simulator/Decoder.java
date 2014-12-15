package simulator;
import instruction.Instruction;
import instruction.InstructionType;
import instructionQueue.InstructionQueue;

import java.util.ArrayList;
import java.util.Iterator;

import register.FreeList;
import register.Register;
import register.ActiveList;

/**
 * Instruction decode. Renames instruction registers.
 * @author Ozzy
 *
 */
public class Decoder extends SimUnit {
	public Decoder(AppContext appContext) {
		super(appContext);
	}
	
	
	/**
	 * Determine if instructions can be added to the queue.
	 */
	private boolean canAdd() {
		// Active list is not full.
		boolean checkActiveList = appContext.activeList.canAdd();
		
		// Queues are not full.
		boolean checkAddressQueue = appContext.addressQueue.canAdd();
		boolean checkFloatingQueue = appContext.floatingQueue.canAdd();
		boolean checkIntegerQueue = appContext.integerQueue.canAdd();
		
		return checkActiveList && checkAddressQueue && checkFloatingQueue && checkIntegerQueue;
	}
	
	
	/**
	 * Add instructions to the queue for instructions to be decoded.
	 */
	public void add(Instruction instruction) {
		instructions_r.add(instruction);
	}
	
	
	@Override
	public String getIdentifier() {
		return "D";
	}
	
	
	/**
	 * Translate logical to physical registers.
	 */
	@Override
	public void calc() {
		super.calc();
		
		int maxReadableInstructions = Math.min(4, appContext.fetcher.instructionRegister.size());
		for (int i = 0; i < maxReadableInstructions; i++) {
			Instruction instruction = appContext.fetcher.instructionRegister.get(i);
			instructions_r.add(instruction);
		}
	}
	
	
	@Override
	public void edge() {
		super.edge();
		
		Iterator<Instruction> iterator = instructions_r.iterator();
		while (iterator.hasNext()) {
			Instruction instruction = iterator.next();
			
			if (instruction.instructionType == InstructionType.BRANCH) {
				appContext.branchHandler.addFrame(instruction);
			}
			
			if (canAdd()) {
				// If there are no instructions, break the loop.
				if (instruction == null) break;
				
				// Rename destination register if not immediate.
				if (instruction.rd != appContext.registerList.getImmRegister()) {
					if (appContext.freeList.hasFreeRegister()) {
						Register reserved = appContext.freeList.removeRegister();
						appContext.activeList.addMapping(instruction.rd, reserved);
						instruction.rd = reserved;
						instruction.renamed = true;
						
						// Rename operand registers.
						instruction.rs = renameOperandRegister(instruction.rs);
						instruction.rt = renameOperandRegister(instruction.rt);
						
						enqueueInstruction(instruction);						
						instructions_n.add(instruction);
					}
				} else {
					// Rename operand registers.
					instruction.rs = renameOperandRegister(instruction.rs);
					instruction.rt = renameOperandRegister(instruction.rt);
					
					enqueueInstruction(instruction);
					instructions_n.add(instruction);
				}
			}
		}
		
		super.edge();
		
		instructions_r.clear();
		instructions_n.clear();
	}
	
	@Override
	public void clearFromInstruction(Instruction instruction) {
		
	}
	
	// Rename an operand register if mapping exists.
	private Register renameOperandRegister(Register register) {
		if (register != null) {
			Register renamed = appContext.activeList.getRenamed(register);
			if (renamed != null) {
				return renamed;
			}
		}
		
		return register;
	}
	
	// Send instruction to queues.
	private void enqueueInstruction(Instruction instruction) {
		appContext.activeList.enqueue(instruction);
		getInstructionQueue(instruction.instructionType).enqueue(instruction);
		appContext.fetcher.instructionRegister.remove(instruction);
	}
	
	// Get instruction queue for an instruction type.
	private InstructionQueue getInstructionQueue(InstructionType instructionType) {
		InstructionQueue instructionQueue = null;
		
		switch (instructionType) {
		case LOAD:
			instructionQueue = appContext.addressQueue;
			break;
		case BRANCH:
			instructionQueue = appContext.integerQueue;
			break;
		case FADD:
			instructionQueue = appContext.floatingQueue;
			break;
		case FMUL:
			instructionQueue = appContext.floatingQueue;
			break;
		case INTEGER:
			instructionQueue = appContext.integerQueue;
			break;
		case STORE:
			instructionQueue = appContext.addressQueue;
			break;
		default:
			break;
		}
		
		return instructionQueue;
	}
}
