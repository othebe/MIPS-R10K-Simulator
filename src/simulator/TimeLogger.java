package simulator;

import instruction.Instruction;
import instructionQueue.InstructionQueue;

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
				// Fill in previous cycles.
				history.add(null);
			}
			timeLine.put(instruction, history);
		}
		
		history = timeLine.get(instruction);
		
		// Fill in stall cycles.
		while (history.size() < cycle) {
			history.add(null);
		}
		
		// Log a sim unit.
		if (history.size() <= cycle) {
			history.add(simUnit);
		}
		// Replace a sim unit.
		else {
			history.set(cycle, simUnit);
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
			HashMap<SimUnit, Boolean> waiting = new HashMap<SimUnit, Boolean>();
			for (int i = 0; i < history.size(); i++) {
				SimUnit simUnit = history.get(i);
				String identifier = "";
				
				if (simUnit != null) {
					if (simUnit instanceof BranchHandler) {
						waiting.clear();
						identifier = simUnit.getIdentifier();
					} else if (simUnit instanceof ExecutionUnit){
						identifier = simUnit.getIdentifier();
					} else if (!waiting.containsKey(simUnit)) {
						identifier = simUnit.getIdentifier();
						waiting.put(simUnit, true);
					}
				}
				System.out.printf("%5s", identifier);
			}
			System.out.println();
		}
	}
	
	public void printStatistics() {
		int[] fetches = new int[cycle];
		int[] decodes = new int[cycle];
		int[] issues = new int[cycle];
		int[] rollbacks = new int[cycle];
		int[] executions = new int[cycle];
		int[] graduations = new int[cycle];
		
		Iterator<Instruction> iterator = instructions.iterator();
		while (iterator.hasNext()) {
			Instruction instruction = iterator.next();
			ArrayList<SimUnit> history = timeLine.get(instruction);
			HashMap<SimUnit, Boolean> waiting = new HashMap<SimUnit, Boolean>();
			for (int i = 0; i < history.size(); i++) {
				SimUnit simUnit = history.get(i);
				
				if (simUnit != null) {
					if (simUnit instanceof BranchHandler) {
						waiting.clear();
						rollbacks[i]++;
					} else if (simUnit instanceof ExecutionUnit){
						executions[i]++;
					} else if (!waiting.containsKey(simUnit)) {
						if (simUnit instanceof Fetcher) {
							fetches[i]++;
						} else if (simUnit instanceof Decoder) {
							decodes[i]++;
						} else if (simUnit instanceof InstructionQueue) {
							issues[i]++;
						} else if (simUnit instanceof Graduator) {
							graduations[i]++;
						}
						waiting.put(simUnit, true);
					}
				}
			}	
		}
		
		System.out.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\n", "Cycle", "Fetches", "Issues", "Decodes", "Executions", "Rollbacks", "Graduations");
		for (int i = 0; i < cycle; i++) {
			System.out.printf("%d\t%d\t%d\t%d\t%d\t%d\t%d\n", i + 1, fetches[i], decodes[i], issues[i], executions[i], rollbacks[i], graduations[i]);
		}
	}
}
