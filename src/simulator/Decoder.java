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
	public boolean canAdd() {
		// Active list is not full.
		boolean checkActiveList = appContext.activeList.canAdd();
		
		// Queues are not full.
		
		return checkActiveList;
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
		
		Iterator<Instruction> iterator = instructions_r.iterator();
		while (iterator.hasNext()) {
			Instruction instruction = iterator.next();
			
			// Rename operand registers.
			instruction.rs = renameOperandRegister(instruction.rs);
			instruction.rt = renameOperandRegister(instruction.rt);
			
			// Rename destination register.
			if (instruction.rd != null && appContext.freeList.hasFreeRegister()) {
				Register reserved = appContext.freeList.removeRegister();
				if (reserved != null) {
					reserved.setBypass(false);
					appContext.activeList.addMapping(instruction.rd, reserved);
					instruction.rd = reserved;
					instruction.renamed = true;
					
					InstructionQueue instructionQueue = getInstructionQueue(instruction.instructionType);
					if (instructionQueue != null) {
						instructions_n.add(instruction);
						iterator.remove();
					}
				}
			}
		}
	}
	
	
	@Override
	public void edge() {
		super.edge();
		
		Iterator<Instruction> iterator = instructions_n.iterator();
		while (iterator.hasNext()) {
			Instruction instruction = iterator.next();
			InstructionQueue instructionQueue = getInstructionQueue(instruction.instructionType);
			if (instructionQueue.canAdd()) {
				instructionQueue.enqueue(instruction);
				iterator.remove();
			}
		}
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
