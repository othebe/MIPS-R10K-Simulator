package simulator;

import instruction.Instruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import executionUnit.ExecutionUnit;

public class TimeLogger {
	private AppContext appContext;
	
	private int cycle;
	private LinkedList<Instruction> instructions;
	private HashMap<Instruction, ArrayList<SimUnit>> timeLine;
	
	public TimeLogger(AppContext appContext) {
		this.appContext = appContext;
		
		this.cycle = 0;
		this.instructions = new LinkedList<Instruction>();
		this.timeLine = new HashMap<Instruction, ArrayList<SimUnit>>();
	}
	
	public void nextCycle() {
		this.cycle++;
	}
	
	public void log(Instruction instruction, SimUnit simUnit) {
		ArrayList<SimUnit> history;
		
		// Register instruction.
		if (!timeLine.containsKey(instruction)) {
			instructions.add(instruction);
			history = new ArrayList<SimUnit>();
			for (int i = 1; i < cycle; i++) {
				history.add(null);
			}
			timeLine.put(instruction, history);
		}
		
		history = timeLine.get(instruction);
		
		// Fill in stall cycles.
		while (history.size() < cycle) {
			history.add(null);
		}
		
		// Log instruction.
		if (!history.isEmpty() && !(simUnit instanceof ExecutionUnit)) {
			boolean alreadyPrinted = false;
			for (int i = 0; i < history.size(); i++) {
				alreadyPrinted = alreadyPrinted || (history.get(i) == simUnit);
			}
			if (!alreadyPrinted) {
				history.add(simUnit);
			}
		} else {
			history.add(simUnit);
		}
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
			ArrayList<SimUnit> history = timeLine.get(instruction);
			for (int i = 0; i < history.size(); i++) {
				SimUnit simUnit = history.get(i);
				String identifier = "";
				
				if (simUnit != null) {
					identifier = simUnit.getIdentifier();
				}
				
				System.out.printf("%5s", identifier);
			}
			System.out.println();
		}
	}
}
