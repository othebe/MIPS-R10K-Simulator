package instructionQueue;

import instruction.Instruction;
import instruction.InstructionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import executionUnit.AddressCalculationUnit;
import executionUnit.AluUnit;
import executionUnit.ExecutionUnit;
import executionUnit.LoadStoreUnit;
import register.Register;
import simulator.AppContext;
import simulator.SimUnit;

public class AddressQueue extends InstructionQueue {
	private HashMap<Instruction, Boolean> completedAddressCompute;
	private HashMap<Instruction, Boolean> completedMemoryAccess;
	
	private ArrayList<Instruction> inderminationList;
	private ArrayList<Instruction> dependencyList;
	
	private AddressCalculationUnit addressCalcUnit;
	private LoadStoreUnit loadStoreUnit;
	
	public AddressQueue(AppContext appContext) {
		super(appContext);
		
		this.completedAddressCompute = new HashMap<Instruction, Boolean>();
		this.completedMemoryAccess = new HashMap<Instruction, Boolean>();
		
		this.inderminationList = new ArrayList<Instruction>();
		this.dependencyList = new ArrayList<Instruction>();
		
		this.addressCalcUnit = new AddressCalculationUnit(appContext);
		this.loadStoreUnit = new LoadStoreUnit(appContext);
		
		this.executionUnits = new ExecutionUnit[2];
		this.executionUnits[0] = this.addressCalcUnit;
		this.executionUnits[1] = this.loadStoreUnit;
	}
	
	@Override
	public boolean canAdd() {
		return this.queuedInstructions.size() < SIZE;
	}

	@Override
	public void calc() {
		super.calc();
		
		// Issue instruction.
		Iterator<Instruction> iterator = queuedInstructions.iterator();
		while (iterator.hasNext()) {
			Instruction instruction = iterator.next();
			instructions_n.add(instruction);
			
			// Initialize address computed table.
			if (!completedAddressCompute.containsKey(instruction)) {
				completedAddressCompute.put(instruction, false);
			}
			
			// Initialize memory lookup table and dependency matrix.
			if (!completedMemoryAccess.containsKey(instruction)) {
				completedMemoryAccess.put(instruction, false);

				addIndeterminationDependency(instruction);

				//if(instruction.instructionType.compareTo(InstructionType.STORE) == 0) {
					addDependency(instruction);
				//}
			}
		}
		
		// Run address calculation unit.
		this.addressCalcUnit.calc();
		if (this.addressCalcUnit.getCompletedInstruction() != null) {
			Instruction completedInstruction = this.addressCalcUnit.getCompletedInstruction();
			completedInstruction.rd.setBusy(true);
			this.completedAddressCompute.put(completedInstruction, true);
		}
		
		// Run memory access unit.
		this.loadStoreUnit.calc();
		if (this.loadStoreUnit.getCompletedInstruction() != null) {
			Instruction completedInstruction = this.loadStoreUnit.getCompletedInstruction();
			this.completedMemoryAccess.put(completedInstruction, true);
			
			// Update dependency matrix.
			appContext.addressQueue.removeInderminationDependency(completedInstruction);
			
			if (completedInstruction.instructionType.compareTo(InstructionType.LOAD) == 0) {
				this.removeDependency(completedInstruction);
			}
		}
		
		this.instructions_r.clear();
	}
			
	@Override
	public void dequeue(Instruction instruction) {
		super.dequeue(instruction);
		
		this.completedAddressCompute.remove(instruction);
	}
	
	@Override
	public void edge() {
		super.edge();
		
		// Attempt to assign instructions to execution units.
		assignInstructionToExecutionUnit();
		
		instructions_n.clear();
		
		this.addressCalcUnit.edge();
		this.loadStoreUnit.edge();
	}
	
	@Override
	public void clearFromInstruction(Instruction instruction) {
		super.clearFromInstruction(instruction);
		
		Iterator<Instruction> iterator;
		
		// Remove all prior entries from the indermination matrix.
		iterator = inderminationList.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().seqNum >= instruction.seqNum) iterator.remove();
		}
		
		// Remove all prior entries from the dependency matrix.
		iterator = dependencyList.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().seqNum >= instruction.seqNum) iterator.remove();
		}
		
		// Remove all prior entries from the address lookup matrix.
		iterator = completedAddressCompute.keySet().iterator();
		while (iterator.hasNext()) {
			if (iterator.next().seqNum >= instruction.seqNum) iterator.remove();
		}
		
		// Remove all prior entries from the memory lookup matrix.
		iterator = completedMemoryAccess.keySet().iterator();
		while (iterator.hasNext()) {
			if (iterator.next().seqNum >= instruction.seqNum) iterator.remove();
		}
	}
	
	public void addIndeterminationDependency(Instruction instruction) {
		this.inderminationList.add(instruction);
	}
	
	public void removeInderminationDependency(Instruction instruction) {
		this.inderminationList.remove(instruction);
	}
	
	public boolean checkIndeterminationDependency(Instruction instruction) {
		// Allow address to be calculated out of order.
		return true;
	}
	
	public void addDependency(Instruction instruction) {
		this.dependencyList.add(instruction);
	}
	
	public void removeDependency(Instruction instruction) {
		this.dependencyList.remove(instruction);
		
		// If a dependency is removed, attempt to move instructions in the pipeline so they are ready at the calc stage.
		assignInstructionToExecutionUnit();
	}
	
	/**
	 * @param instruction
	 * @return
	 */
	public boolean checkDependency(Instruction instruction) {
		if (instruction.instructionType.compareTo(InstructionType.STORE) == 0) {
			return dependencyList.indexOf(instruction) == 0;
		}
		
		Iterator<Instruction> iterator = dependencyList.iterator();
		while (iterator.hasNext()) {
			Instruction queued = iterator.next();
			
			// No need to check dependencies for previous instructions.
			if (queued.seqNum >= instruction.seqNum) continue;
			
			if (instruction.extra.compareTo(queued.extra) == 0) {
				return false;
			}
		}
		return true;
	}
	
	private void assignInstructionToExecutionUnit() {
		Iterator<Instruction> iterator = queuedInstructions.iterator();
		while (iterator.hasNext()) {
			Instruction instruction = iterator.next();
			
			// Attempt to compute memory address.
			if (completedAddressCompute.containsKey(instruction) && !completedAddressCompute.get(instruction)) {
				if (instruction.operandsAvailable()) {
					if (this.addressCalcUnit.canIssue()) {
						this.addressCalcUnit.issue(instruction);
					}
				}
			}
			
			// Attempt to access memory.
			else if (completedMemoryAccess.containsKey(instruction) && !completedMemoryAccess.get(instruction)) {
				// Check indetermination list.
				if (!checkIndeterminationDependency(instruction)) {
					continue;
				}
				
				// Check dependency list.
				if (!checkDependency(instruction)) {
					continue;
				}
				
				if (instruction.operandsAvailable() && this.loadStoreUnit.canIssue()) {
					this.loadStoreUnit.issue(instruction);
				}
			}
		}
	}	
}
