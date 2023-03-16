package io.qudot.qudotc.qudir;

import io.qudot.qudotc.utils.BytecodeUtils;
import io.qudot.qudotc.utils.Bytecodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Disassembles QuDot Bytecode into instruction. Instruction are print to the console
 *
 * @since 0.1.0
 */
public class QuDotDisassembler {
    private byte[] code;
    private int codeSize;
    private Object[] constPool;

    public QuDotDisassembler(byte[] code, int codeSize, Object[] constPool) {
        this.code = code;
        this.codeSize = codeSize;
        this.constPool = constPool;
    }

    public void disassemble() {
        System.out.println("Disassembly\n");
        int i=0;
        while (i < codeSize) {
            i = disassembleInstruction(i);
            System.out.println();
        }
        System.out.println();
    }

    public int disassembleInstruction(int ip) {
        int opcode = code[ip];
        Bytecodes.Instruction instr = Bytecodes.instructions[opcode];
        String instrName = instr.getName();
        System.out.printf("%04d:\t%-11s", ip, instrName);
        ip++;
        if (instr.getN() == 0) {
            System.out.print("  ");
            return ip;
        }

        List<String> operands = new ArrayList<>();
        for (int i=0; i < instr.getN(); i++) {
            int opnd = BytecodeUtils.getInt(code, ip);
            ip += 4;
            switch ( instr.getType()[i] ) {
                case Bytecodes.REG:
                    operands.add("r" + opnd);
                    break;
                case Bytecodes.QUREG:
                    operands.add("q" + opnd);
                    break;
                case Bytecodes.GATE :
                    operands.add(showConstPoolOperand(opnd));
                    break;
                case Bytecodes.INT :
                    operands.add(String.valueOf(opnd));
                    break;
                case Bytecodes.ARRAY:
                    // the previous int in an array gives us the size
                    ip -= 4;
                    int size = BytecodeUtils.getInt(code, ip);
                    ip +=4;

                    for (int j=0; j < size; j++) {
                        opnd = BytecodeUtils.getInt(code, ip);
                        operands.add(String.valueOf(opnd));
                        ip += 4;
                    }

            }
        }

        for (int i = 0; i < operands.size(); i++) {
            String s = operands.get(i);
            if ( i > 0 ) System.out.print(", ");
            System.out.print(s);
        }
        return ip;
    }

    private String showConstPoolOperand(int poolIndex) {
        StringBuilder buf = new StringBuilder();
        buf.append("#");
        buf.append(poolIndex);
        String s = constPool[poolIndex].toString();
        if ( constPool[poolIndex] instanceof GateAsmSymbol ) {
            GateAsmSymbol gs = (GateAsmSymbol) constPool[poolIndex];
            s= gs.getName() + "() @ " +gs.getAddress();
        }
        buf.append(":");
        buf.append(s);
        return buf.toString();
    }
}