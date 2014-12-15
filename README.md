MIPS-R10K-Simulator
===================

A simulator for the MIPS R10K Simulator.

- Import using Eclipse.
- Run main via src/simulator/Simulator.java
- Trace input goes in trace.in

===================
I 07 08 1D    (r29=r7 INTOP r8)
A 02 13 15   (r21=r2 FPADD r19) 
M 03 04 02  (r2  =r3 FPMUL r4)

Load and Store instructions have immediate field, which is not considered, and rd field becomes unimportant (xx):
L 1f 12 xx ABCD1234   (r18 = MEM[r31+imm] which is MEM [ABCD1234])
S 10 12 xx ABCD1234  (MEM[r16+imm] which is MEM [ABCD1234] = r18)

Finally for branches, the rd field becomes unimportant as well: 
B 09 08 xx 1
B 09 05 xx 0
If the extra field is 0, it means after execution the branch were correctly predicted and the trace file is run correctly. If the extra field is 1, when the branch finished execution, it finds out that it was mis-predicted, so it will flush instructions after the branch. Now it goes back to same branch instruction, but the extra field is changed to 0 (simulator must do this), so it re-runs the branch and instructions afterwards. It is just a model that the instructions on the wrong path of the branch are exactly the same as the correct path.
