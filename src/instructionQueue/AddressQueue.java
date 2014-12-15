package instructionQueue;

import instruction.Instruction;
import instruction.InstructionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import executionUnit.AddressCalculationUnit;
import executionUnit.AluUnit;
import executionUnit.LoadStoreUnit;
import register.Register;
import simulator.AppContext;

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

				if(instruction.instructionType == InstructionType.STORE) {
					addDependency(instruction);
				}
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
		
		Iterator<Instruction> iterator = instructions_n.iterator();
		while (iterator.hasNext()) {
			Instruction instruction = iterator.next();
			
			// Attempt to compute memory address.
			if (!completedAddressCompute.get(instruction)) {
				if (instruction.operandsAvailable()) {
					if (this.addressCalcUnit.canIssue()) {
						this.addressCalcUnit.issue(instruction);
					}
				}
			}
			
			// Attempt to access memory.
			else if (!completedMemoryAccess.get(instruction)) {
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
		
		instructions_n.clear();
		
		this.addressCalcUnit.edge();
		this.loadStoreUnit.edge();
	}
	
	public void addIndeterminationDependency(Instruction instruction) {
		this.inderminationList.add(instruction);
	}
	
	public void removeInderminationDependency(Instruction instruction) {
		this.inderminationList.remove(instruction);
	}
	
	public boolean checkIndeterminationDependency(Instruction instruction) {
		// Loads are only allowed if there are no prior loads waiting.
		return inderminationList.indexOf(instruction) == 0;
	}
	
	public void addDependency(Instruction instruction) {
		this.dependencyList.add(instruction);
	}
	
	public void removeDependency(Instruction instruction) {
		this.dependencyList.remove(instruction);
	}
	
	public boolean checkDependency(Instruction instruction) {
		if (instruction.instructionType != InstructionType.LOAD) {
			return true;
		}
		
		Iterator<Instruction> iterator = dependencyList.iterator();
		while (iterator.hasNext()) {
			Instruction queued = iterator.next();
			if (instruction.rs == queued.rs || instruction.rt == queued.rt) {
				return false;
			}
		}
		return true;
	}
}
