package simulator;
import instruction.Instruction;
import instruction.InstructionType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

import register.Register;

/**
 * Instruction fetcher.
 * @author Ozzy
 *
 */
public class Fetcher extends SimUnit {
	private int instructionNdx;
	private ArrayList<Instruction> parsedInstructions;
	
	public ArrayList<Instruction> instructionRegister;

	public Fetcher(AppContext appContext, String filename) {
		super(appContext);
		
		this.instructionRegister = new ArrayList<Instruction>();
		
		instructionNdx = 0;
		try {
			parsedInstructions = parse(filename);
		} catch (FileNotFoundException e) {
			System.out.printf("File %s not found\n", filename);
			System.exit(0);
		}
	}
	
	public boolean hasNext() {
		return instructionNdx < this.parsedInstructions.size();
	}
	
	public int getNumParsedInstructions() {
		return this.parsedInstructions.size();
	}
	
	@Override
	public String getIdentifier() {
		return "F";
	}
	
	@Override
	public void calc() {
		super.calc();
		
		int maxInstructionNdx = instructionNdx + 4;
		while (instructionNdx < Math.min(parsedInstructions.size(),  maxInstructionNdx)) {
			Instruction instruction = parsedInstructions.get(instructionNdx);
			instructions_n.add(instruction);
			instructionNdx++;
		}
	}
	
	/**
	 * Retrieve 4 instructions and send to decoder.
	 */
	@Override
	public void edge() {
		super.edge();
		
		Iterator<Instruction> iterator = instructions_n.iterator();
		while (iterator.hasNext()) {
			Instruction instruction = iterator.next();
			instructionRegister.add(instruction);
		}
		
		instructions_n.clear();
	}
	
	private ArrayList<Instruction> parse(String filename) throws FileNotFoundException {
		ArrayList<Instruction> parsed = new ArrayList<Instruction>();
		
		int seqNum = 0;
		Scanner traceIn = new Scanner(new File(filename));
		while (traceIn.hasNextLine()) {
			InstructionType instructionType = null;
			Register rs = null;
			Register rt = null;
			Register rd = null;
			String extra = null;
			
			String line = traceIn.nextLine();
			String[] tokens = line.split(" ");

			// Extract instruction type.
			switch (tokens[0]) {
			case "L":
				instructionType = InstructionType.LOAD;
				break;
			case "S":
				instructionType = InstructionType.STORE;
				break;
			case "I":
				instructionType = InstructionType.INTEGER;
				break;
			case "B":
				instructionType = InstructionType.BRANCH;
				break;
			case "A":
				instructionType = InstructionType.FADD;
				break;
			case "M":
				instructionType = InstructionType.FMUL;
				break;
			}

			// Extract RS.
			rs = appContext.registerList.getLogicalRegister(Integer.parseInt(tokens[1], 16));
			
			// Extract RT.
			rt = appContext.registerList.getLogicalRegister(Integer.parseInt(tokens[2], 16));
			
			// Extract RD.
			if (tokens.length > 3) rd = appContext.registerList.getLogicalRegister(Integer.parseInt(tokens[3], 16));
			
			// Extract extra.
			if (tokens.length > 4) extra = tokens[4];
			
			Instruction instruction;
			if (instructionType == InstructionType.LOAD) {
				instruction = new Instruction(seqNum, instructionType, rs, rd, rt, extra);
			}
			else {
				instruction = new Instruction(seqNum, instructionType, rs, rt, rd, extra);
			}
			parsed.add(instruction);
			seqNum++;
		}
		
		return parsed;
	}
}
