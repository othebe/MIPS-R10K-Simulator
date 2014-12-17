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
	public boolean flipped;
	
	public Instruction(int seqNum, InstructionType instructionType, Register rs, Register rt, Register rd, String extra) {
		this.seqNum = seqNum;
		this.instructionType = instructionType;
		this.rs = rs;
		this.rt = rt;
		this.rd = rd;
		this.extra = extra;
		this.renamed = false;
		this.flipped = false;
	}
	
	public boolean operandsAvailable() {
		boolean rsAvailable = !this.rs.isBusy() || this.rs.allowBypass();
		boolean rtAvailable = !this.rt.isBusy() || this.rt.allowBypass();
		
		return rsAvailable && rtAvailable;
	}
	
	public boolean isMispredicted() {
		return this.instructionType.compareTo(InstructionType.BRANCH) == 0 && this.extra.compareTo("1") == 0;
	}
	
	public void flip() {
		if (this.instructionType.compareTo(InstructionType.BRANCH) == 0) {
			this.extra = ((Integer.parseInt(this.extra) + 1) % 2) + "";
			this.flipped = !this.flipped;
		}
	}
}
