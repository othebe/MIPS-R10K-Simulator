package simulator;
import instruction.Instruction;
import instruction.InstructionType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import register.FreeList;
import register.Register;
import register.ActiveList;

/**
 * Main simulator class.
 * @author Ozzy
 */
public class Simulator {
	private AppContext appContext;
	
	public static void main(String args[]) {
		String filename = "trace.in";

		Simulator simulator = new Simulator(filename);
		simulator.run();
	}
	
	public Simulator(String filename) {
		this.appContext = new AppContext(filename);	
	}
	
	public void run() {
		int cycles = 1;
		
		// Run simulator.
		while (appContext.graduator.getLastGraduated() < appContext.fetcher.getNumParsedInstructions()) {
			System.out.printf("Cycle %d\n", cycles);
			appContext.fetcher.calc();
			appContext.decoder.calc();
			appContext.floatingQueue.calc();
			appContext.integerQueue.calc();
			appContext.addressQueue.calc();
			appContext.graduator.calc();
			
			appContext.fetcher.edge();
			appContext.decoder.edge();
			appContext.floatingQueue.edge();
			appContext.integerQueue.edge();
			appContext.addressQueue.edge();
			appContext.graduator.edge();
			
			appContext.timeLogger.nextCycle();
			
			cycles++;
		}
		
		appContext.timeLogger.print();
	}
}
