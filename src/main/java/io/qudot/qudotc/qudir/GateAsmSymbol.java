package io.qudot.qudotc.qudir;

import io.qudot.qudotc.utils.BytecodeUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Class for management of gate symbols defined in .gate declaration.
 * We track the name, number of argument, number of registers required,
 * number of qubit registers required and the address of the call.
 *
 * @since 0.1.0
 */
public class GateAsmSymbol {
    public static final String DEFAULT_CHARSET = "US-ASCII";
    private String name;
    // number of arguments
    private int args;
    // number of registers
    private int regs;
    // number of qubit registers
    private int qubitRegs;
    // definition address
    private int address;

    public GateAsmSymbol(String name) {
        this.name = name;
    }

    public GateAsmSymbol(String name, int args, int regs, int qubitRegs, int address) {
        this.name = name;
        this.args = args;
        this.regs = regs;
        this.qubitRegs = qubitRegs;
        this.address = address;
    }

    @Override
    public int hashCode() { return name.hashCode(); }

    @Override
    public boolean equals(Object o) {
        return o instanceof GateAsmSymbol && name.equals(((GateAsmSymbol)o).name);
    }

    @Override
    public String toString() {
        return "GateSymbol{" +
                "name='" + name + '\'' +
                ", args=" + args +
                ", regs=" + regs +
                ", qubitRegs=" + qubitRegs +
                ", address=" + address +
                '}';
    }

    /**
     * Serialize the GateAsmSymbol into a byte[].
     * The first 4 bytes is an Int the size of the US-ASCII byte[] of the name field
     * The next bytes are the bytes of the field
     * The next 4 bytes is an Int the number of argument
     * The next 4 bytes is an Int the number of registers
     * The next 4 bytes is an Int the number of qubit registers
     * The next 4 bytes is an Int the address
     *
     * The total size of the array should be length_of_name + 20
     * @return byte serialization of object
     */
    public byte[] getBytes() {
        int ip = 0;
        byte[] nameBytes = name.getBytes(Charset.forName(DEFAULT_CHARSET));
        byte[] serialization = new byte[nameBytes.length + 20];
        BytecodeUtils.writeInt(serialization, ip, nameBytes.length);
        ip += 4;

        for (int i=0; i < nameBytes.length; i++) {
            serialization[ip++] = nameBytes[i];
        }

        ip = BytecodeUtils.writeInt(serialization, ip, args);
        ip = BytecodeUtils.writeInt(serialization, ip, regs);
        ip = BytecodeUtils.writeInt(serialization, ip, qubitRegs);
        ip = BytecodeUtils.writeInt(serialization, ip, address);

        return serialization;
    }

    /**
     * Deserialize a byte representation of a GateAsmSymbol
     * @param bytes a byte[]
     * @return GateAsmSymbol
     */
    public static GateAsmSymbol fromBytes(byte[] bytes) {
        try {
            int ip = 0;
            int nameLength = BytecodeUtils.getInt(bytes, ip);
            ip += 4;
            String name = new String(Arrays.copyOfRange(bytes, 4, 4 + nameLength), DEFAULT_CHARSET);
            ip += nameLength;
            int args = BytecodeUtils.getInt(bytes, ip);
            ip += 4;
            int regs = BytecodeUtils.getInt(bytes, ip);
            ip += 4;
            int qubitRegs = BytecodeUtils.getInt(bytes, ip);
            ip += 4;
            int address = BytecodeUtils.getInt(bytes, ip);
            ip += 4;

            return new GateAsmSymbol(name, args, regs, qubitRegs, address);

        } catch (UnsupportedEncodingException ueio) {
            throw new RuntimeException("cannot deserialize GateAsmSymbol US-ASCII not available on system");
        }
    }

    public String getName() {
        return name;
    }

    public int getArgs() {
        return args;
    }

    public int getRegs() {
        return regs;
    }

    public int getQubitRegs() {
        return qubitRegs;
    }

    public int getAddress() {
        return address;
    }
}
