package register;

import instruction.Instruction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class ActiveList {
	private static final int SIZE = 32;
	
	// Map physical register to logical. This will be unique since a physical register can only map to one logical register.
	private HashMap<Register, Register> registerMap;
	
	// Ordered list of instructions.
	private LinkedList<Instruction> running;
	
	public ActiveList() {
		this.registerMap = new HashMap<Register, Register>();
		this.running = new LinkedList<Instruction>();
	}
	
	public boolean canAdd() {
		return this.running.size() < SIZE;
	}
	
	// Add a renamed instruction to the list of running instructions.
	public void enqueue(Instruction instruction) {
		running.add(instruction);
	}
	
	public void dequeue(Instruction instruction) {
		running.remove(instruction);
		removeMapping(instruction.rd);
	}
	
	public void addMapping(Register logical, Register physical) {
		registerMap.put(physical, logical);
	}
	
	public void removeMapping(Register physical) {
		registerMap.remove(physical);
	}
	
	public Register getRenamed(Register logical) {
		Iterator<Instruction> iterator = running.descendingIterator();
		while (iterator.hasNext()) {
			Instruction instruction = iterator.next();
			Register physical = instruction.rd;
			if (registerMap.get(physical) == logical) {
				return physical;
			}
		}
		
		return null;
	}
	
	public Register getOriginal(Register physical) {
		return registerMap.get(physical);
	}
	
	public LinkedList<Instruction> getRunning() {
		return this.running;
	}
	
	public void clearFromInstruction(Instruction instruction) {
		Iterator<Instruction> iterator = running.iterator();
		while (iterator.hasNext()) {
			Instruction runningInstruction = iterator.next();
			
			if (runningInstruction.seqNum >= instruction.seqNum) {
				// Clear newer instructions from running list.
				iterator.remove();
				
				// Clear mapping.
				registerMap.remove(runningInstruction.rd);
			}
		}
	}
}
