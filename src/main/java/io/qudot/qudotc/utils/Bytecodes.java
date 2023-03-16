package io.qudot.qudotc.utils;

import io.qudot.qudotc.qudir.QuDotAsmParser;

/**
 * Class to define all our Bytecodes supported by QuDot VM and the Instruction class. The Instruction class
 * tells us the number of arguments an instruction supports and the type of the argument. This helps the
 * VM read the correct number of bytes from a bytecode array.
 *
 * @since 0.1.0
 */
public class Bytecodes {
    public static final int REG = QuDotAsmParser.REG;
    public static final int QUREG = QuDotAsmParser.QUREG;
    public static final int GATE = QuDotAsmParser.GATE;
    public static final int INT = QuDotAsmParser.INT;
    public static final int ID = QuDotAsmParser.ID;
    public static final int ARRAY = 69;

    public static class Instruction {
        String name; // E.g., "iadd", "call"
        int[] type = new int[5];
        int n = 0;
        public Instruction(String name) {
            this(name,0,0,0);
            n=0;
        }

        public Instruction(String name, int r1) {
            this(name, r1,0,0);
            n=1;
        }

        public Instruction(String name, int r1, int r2) {
            this(name, r1, r2,0);
            n=2;

        }

        public Instruction(String name, int r1, int r2, int r3) {
            this.name = name;
            type[0] = r1;
            type[1] = r2;
            type[2] = r3;
            n = 3;
        }

        public Instruction(String name, int r1, int r2, int r3, int r4) {
            this.name = name;
            type[0] = r1;
            type[1] = r2;
            type[2] = r3;
            type[3] = r4;
            n = 4;
        }

        public Instruction(String name, int r1, int r2, int r3, int r4, int r5) {
            this.name = name;
            type[0] = r1;
            type[1] = r2;
            type[2] = r3;
            type[3] = r4;
            type[4] = r5;
            n = 5;
        }

        public String getName() {
            return name;
        }

        public int getN() {
            return n;
        }

        public int[] getType() {
            return type;
        }

    }

    public static final short HALT = 0;
    public static final short PATHS = 1;
    public static final short X = 2;
    public static final short Y = 3;
    public static final short Z = 4;    // R(1)
    public static final short S = 5;    // R(2)
    public static final short T = 6;    // R(3)
    public static final short PHI = 7;
    public static final short H = 8;
    public static final short SWAP = 9;
    public static final short SWAP_AB = 10;
    public static final short MEASURE = 11;
    public static final short CNOT = 12;
    public static final short CROT = 13;
    public static final short SEMI_CNOT = 14;
    public static final short SEMI_CROT = 15;
    public static final short XON = 16;
    public static final short YON = 17;
    public static final short ZON = 18;
    public static final short SON = 19;
    public static final short TON = 20;
    public static final short PHION = 21;
    public static final short HON = 22;
    public static final short MON = 23;
    public static final short SWAPON = 24;
    public static final short QLOAD = 25;
    public static final short QLOAD_ARRAY = 26;
    public static final short IADD = 27;
    public static final short ISUB = 28;
    public static final short IMUL = 29;
    public static final short ILT = 30;
    public static final short IEQ = 31;
    public static final short INCR = 32;
    public static final short BR = 33;
    public static final short BRT = 34;
    public static final short BRF = 35;
    public static final short ILOAD = 36;
    public static final short RET = 37;
    public static final short MOVE = 38;
    public static final short NULL = 39;
    public static final short CALL = 40;
    public static final short PRINTR = 41;
    public static final short QLOAD_SEQUENCE = 42;
    public static final short BREQ = 43;
    public static final short BRGEZ = 44;
    public static final short BRGTZ = 45;
    public static final short BRLEZ = 46;
    public static final short BRLTZ = 47;
    public static final short BRNEQ = 48;
    public static final short QLOADR = 49;
    public static final short IDIV = 50;
    public static final short DECR = 51;
    public static final short TOFF = 52;
    public static final short IQUADD = 53;
    public static final short QFT = 54;
    public static final short QFT_INV = 55;
    public static final short IQUADD_MOD = 56;
    public static final short IQUMUL_MOD = 57;
    public static final short CIQUADD_MOD = 58;
    public static final short CIQUMUL_MOD = 59;
    public static final short MODPOW = 60;
    public static final short PHIDAG = 61;
    public static final short PHIDAGON = 62;
    public static final short SDAG = 63;
    public static final short SDAGON = 64;
    public static final short TDAG = 65;
    public static final short TDAGON = 66;

    public static Instruction[] instructions = new Instruction[] {
            new Instruction("halt"),
            new Instruction("paths"),
            new Instruction("x"),
            new Instruction("y"),
            new Instruction("z"),
            new Instruction("s"),
            new Instruction("t"),
            new Instruction("phi", REG),
            new Instruction("h"),
            new Instruction("swap"),
            new Instruction("swap_ab", QUREG, QUREG),
            new Instruction("measure"),
            new Instruction("cnot", QUREG, QUREG),
            new Instruction("crot", QUREG, QUREG),
            new Instruction("semi_cnot", QUREG, QUREG),
            new Instruction("semi_crot", REG, QUREG, QUREG),
            new Instruction("xon", QUREG),
            new Instruction("yon", QUREG),
            new Instruction("zon", QUREG),
            new Instruction("son", QUREG),
            new Instruction("ton", QUREG),
            new Instruction("phion", REG, QUREG),
            new Instruction("hon", QUREG),
            new Instruction("mon", QUREG),
            new Instruction("swapon", QUREG),
            new Instruction("qload", QUREG, INT),
            new Instruction("qload_array", QUREG, INT, ARRAY),
            new Instruction("iadd", REG, REG, REG),
            new Instruction("isub", REG, REG, REG),
            new Instruction("imul", REG, REG, REG),
            new Instruction("ilt", REG, REG, REG),
            new Instruction("ieq", REG, REG, REG),
            new Instruction("incr", REG),
            new Instruction("br", INT),
            new Instruction("brt", REG, INT),
            new Instruction("brf", REG, INT),
            new Instruction("iload", REG, INT),
            new Instruction("ret"),
            new Instruction("move", REG, REG),
            new Instruction("null", REG),
            new Instruction("call", GATE),
            new Instruction("printr", REG),
            new Instruction("qload_seq", QUREG, INT, INT),
            new Instruction("breq", REG, REG, INT),
            new Instruction("brgez", REG, INT),
            new Instruction("brgtz", REG, INT),
            new Instruction("brlez", REG, INT),
            new Instruction("brltz", REG, INT),
            new Instruction("brneq", REG, REG, INT),
            new Instruction("qloadr", QUREG, REG),
            new Instruction("idiv", REG, REG, REG),
            new Instruction("decr", REG),
            new Instruction("toff", QUREG, QUREG),
            new Instruction("iquadd", REG),
            new Instruction("qft", QUREG, QUREG),
            new Instruction("qft_inv", QUREG, QUREG),
            new Instruction("iquadd_mod", REG, REG),
            new Instruction("iqumul_mod", REG, REG),
            new Instruction("ciquadd_mod", REG, REG, QUREG, QUREG, QUREG),
            new Instruction("ciqumul_mod", REG, REG, QUREG, QUREG, QUREG),
            new Instruction("modpow", REG, REG, REG, REG),
            new Instruction("phidag", REG),
            new Instruction("phidagon", REG, QUREG),
            new Instruction("sdag"),
            new Instruction("sdagon", QUREG),
            new Instruction("tdag"),
            new Instruction("tdagon", QUREG)
    };
}