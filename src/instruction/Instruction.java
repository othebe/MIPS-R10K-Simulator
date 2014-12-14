package instruction;

import register.FreeList;
import register.Register;
import register.ActiveList;

public class Instruction {
	public int seqNum;
	public InstructionType instructionType;
	public Register rs;
	public Register rt;
	public Register rd;
	public String extra;
	public boolean renamed;
	
	public Instruction(int seqNum, InstructionType instructionType, Register rs, Register rt, Register rd, String extra) {
		this.seqNum = seqNum;
		this.instructionType = instructionType;
		this.rs = rs;
		this.rt = rt;
		this.rd = rd;
		this.extra = extra;
		this.renamed = false;
	}
}
