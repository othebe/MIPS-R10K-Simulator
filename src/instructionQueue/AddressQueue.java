package instructionQueue;

import instruction.Instruction;
import instruction.InstructionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import executionUnit.LoadStoreUnit;
import register.Register;
import simulator.AppContext;

public class AddressQueue extends InstructionQueue {
	private ArrayList<Register> loadDependencies;
	private ArrayList<String> storeDependencies;
	
	private LoadStoreUnit loadStoreUnit;
	
	public AddressQueue(AppContext appContext) {
		super(appContext);
		
		this.loadDependencies = new ArrayList<Register>();
		this.storeDependencies = new ArrayList<String>();
		this.loadStoreUnit = new LoadStoreUnit(appContext);
	}
	
	
	@Override
	public void calc() {
		super.calc();
		
		Iterator<Instruction> iterator = instructions_r.iterator();
		while (iterator.hasNext()) {
			Instruction instruction = iterator.next();
			
			// Check load dependency.
			if (loadDependencies.contains(instruction.rs) || storeDependencies.contains(instruction.rt)) {
				continue;
			}
			
			// Check store dependency.
			if (storeDependencies.contains(instruction.extra)) {
				continue;
			}
			
			// Instruction is allowed to be executed.
			this.instructions_n.add(instruction);
			iterator.remove();
		}
		
		this.loadStoreUnit.calc();
	}
	
	
	@Override
	public void edge() {
		super.edge();
		
		Iterator<Instruction> iterator = instructions_n.iterator();
		while (iterator.hasNext()) {
			Instruction instruction = iterator.next();
			if (instruction.rs.isBusy() || instruction.rt.isBusy()) {
				continue;
			}
			
			if (this.loadStoreUnit.canIssue()) {
				this.loadStoreUnit.issue(instruction);
			}
		}
		
		this.loadStoreUnit.edge();
	}
	
	public void dequeue(Instruction instruction) {
		this.instructions_n.remove(instruction);
	}
	
	public void addLoadDependency(Register register) {
		this.loadDependencies.add(register);
	}
	
	public void removeLoadDependency(Register register) {
		this.loadDependencies.remove(register);
	}
	
	public void addStoreDependency(String address) {
		this.storeDependencies.add(address);
	}
	
	public void removeStoreDependency(String address) {
		this.storeDependencies.remove(address);
	}
}
