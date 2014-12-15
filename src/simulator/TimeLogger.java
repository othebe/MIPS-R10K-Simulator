package simulator;

import instruction.Instruction;

import java.net.IDN;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import executionUnit.AluUnit;
import executionUnit.ExecutionUnit;

public class TimeLogger {
	private AppContext appContext;
	
	private int cycle;
	private LinkedList<Instruction> instructions;
	private HashMap<Instruction, ArrayList<String>> timeLine;
	
	public TimeLogger(AppContext appContext) {
		this.appContext = appContext;
		
		this.cycle = 0;
		this.instructions = new LinkedList<Instruction>();
		this.timeLine = new HashMap<Instruction, ArrayList<String>>();
	}
	
	public void nextCycle() {
		this.cycle++;
	}
	
	public void log(Instruction instruction, SimUnit simUnit) {
		ArrayList<String> history;
		
		// Register instruction.
		if (!timeLine.containsKey(instruction)) {
			instructions.add(instruction);
			history = new ArrayList<String>();
			for (int i = 1; i < cycle; i++) {
				// Fill in previous cycles.
				history.add("");
			}
			timeLine.put(instruction, history);
		}
		
		history = timeLine.get(instruction);
		
		// Fill in stall cycles.
		while (history.size() < cycle) {
			history.add("");
		}
		
		// Log instruction. Don't log waiting instructions.
//		if (!history.isEmpty() && !(simUnit instanceof ExecutionUnit)) {
//			boolean waiting = false;
//			for (int i = 0; i < history.size(); i++) {
//				if (simUnit instanceof BranchHandler) waiting = false;
//				String identifier = simUnit.getIdentifier();
//				waiting = waiting || (history.get(i).compareTo(identifier) == 0);
//			}
//			if (!waiting) {
//				history.add(simUnit.getIdentifier());
//			}
//		} else {
			history.add(simUnit.getIdentifier());
//		}
		timeLine.put(instruction, history);
	}
	
	public void print() {
		for (int i = 0; i <= cycle; i++) System.out.printf("%5d", i);
		System.out.println();
		for (int i = 0; i <= cycle; i++) System.out.printf("%5s", "-----");
		System.out.println();

		Iterator<Instruction> iterator = instructions.iterator();
		while (iterator.hasNext()) {
			Instruction instruction = iterator.next();
			
			// Print sequence number.
			System.out.printf("%5d", instruction.seqNum);
			
			// Print sequential timeline.
			String lastIdentifier = "";
			ArrayList<String> history = timeLine.get(instruction);
			for (int i = 0; i < history.size(); i++) {
				String identifier = history.get(i);
				System.out.printf("%5s", history.get(i));
			}
			System.out.println();
		}
	}
}
