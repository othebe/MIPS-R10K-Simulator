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
	
	private ArrayList<Instruction> loadDependencies;
	private ArrayList<String> storeDependencies;
	
	private AddressCalculationUnit addressCalcUnit;
	private LoadStoreUnit loadStoreUnit;
	
	public AddressQueue(AppContext appContext) {
		super(appContext);
		
		this.completedAddressCompute = new HashMap<Instruction, Boolean>();
		this.completedMemoryAccess = new HashMap<Instruction, Boolean>();
		
		this.loadDependencies = new ArrayList<Instruction>();
		this.storeDependencies = new ArrayList<String>();
		
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

				if (instruction.instructionType == InstructionType.LOAD) {
					addLoadDependency(instruction);
				} 
				else if(instruction.instructionType == InstructionType.STORE) {
					addStoreDependency(instruction.extra);
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
			if (completedInstruction.instructionType == InstructionType.LOAD) {
				appContext.addressQueue.removeLoadDependency(completedInstruction);
			} 
			else if(completedInstruction.instructionType == InstructionType.STORE) {
				appContext.addressQueue.removeStoreDependency(completedInstruction.extra);
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
				// Check load dependency.
				if (!checkLoadDependency(instruction)) {
					continue;
				}
				
				// Check store dependency.
				if (storeDependencies.contains(instruction.extra)) {
					continue;
				}
				
				if (instruction.operandsAvailable() && this.loadStoreUnit.canIssue()) {
					this.loadStoreUnit.issue(instruction);
				}
			}
			
			iterator.remove();
		}
		
		this.addressCalcUnit.edge();
		this.loadStoreUnit.edge();
	}
	
	public void addLoadDependency(Instruction instruction) {
		this.loadDependencies.add(instruction);
	}
	
	public void removeLoadDependency(Instruction instruction) {
		this.loadDependencies.remove(instruction);
	}
	
	public boolean checkLoadDependency(Instruction instruction) {
		// Loads are only allowed if there are no prior loads waiting.
		return loadDependencies.indexOf(instruction) == 0;
	}
	
	public void addStoreDependency(String address) {
		this.storeDependencies.add(address);
	}
	
	public void removeStoreDependency(String address) {
		this.storeDependencies.remove(address);
	}
}
