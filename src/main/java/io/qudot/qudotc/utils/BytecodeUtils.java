package io.qudot.qudotc.utils;

import java.io.ByteArrayOutputStream;

/**
 * Utility classes for reading and writing from bytecode arrays.
 *
 * @since 0.1.0
 */
public class BytecodeUtils {

    /**
     * Read value at index into a byte array highest to lowest byte, left to right.
     * @param memory the bytecode array
     * @param index the position to read
     * @return the int read
     */
    public static int getInt(byte[] memory, int index) {
        int b1 = memory[index++]&0xFF; // mask off sign-extended bits
        int b2 = memory[index++]&0xFF;
        int b3 = memory[index++]&0xFF;
        int b4 = memory[index++]&0xFF;
        int word = b1<<(8*3) | b2<<(8*2) | b3<<(8*1) | b4;
        return word;
    }

    /**
     * Write value at index into a byte array highest to lowest byte, left to right.
     * @param bytes the bytecode array
     * @param index position to write
     * @param value the value to write
     */
    public static int writeInt(byte[] bytes, int index, int value) {
        bytes[index+0] = (byte)((value>>(8*3))&0xFF); // get highest byte
        bytes[index+1] = (byte)((value>>(8*2))&0xFF);
        bytes[index+2] = (byte)((value>>(8*1))&0xFF);
        bytes[index+3] = (byte)(value&0xFF);

        return index + 4;
    }

    public static void writeInt(ByteArrayOutputStream outputStream, int value) {
        outputStream.write((value>>(8*3))&0xFF);
        outputStream.write((value>>(8*2))&0xFF);
        outputStream.write((value>>(8*1))&0xFF);
        outputStream.write(value&0xFF);
    }
}