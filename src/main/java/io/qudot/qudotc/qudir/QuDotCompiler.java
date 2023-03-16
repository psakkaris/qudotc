package io.qudot.qudotc.qudir;


import io.qudot.qudotc.utils.BytecodeUtils;
import io.qudot.qudotc.utils.Bytecodes;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * This is the main executable program for the QuDot Intermediate Representation. It takes a .qudot file
 * and generates a QuDotAssembler object that has all the information necessary to execute on the QuDot VM.
 * The QuDotAssembler is then serialized and written to a byte file with a .qudotc extension
 *
 * @since 0.1.0
 */
@TopCommand
@CommandLine.Command(name = "qudotc", description = "$ qudotc filename.qudot [-o output dir]")
public class QuDotCompiler implements Runnable {
    public static final byte VERSION = 1;
    public static final String INPUT_FILE_EXT = ".qudot";
    public static final String OUTPUT_FILE_EXT = ".qudotc";

    @CommandLine.Parameters(index = "0")
    private String filename;
    @CommandLine.Option(names = {"-o", "--output-directory"}, defaultValue = ".")
    private String outputDir;

    /**
     * Assemble is what does all the work after this call all the fields of the object are set and we are able
     * to run on a QuDot VM
     */
    public void compile() {
        try {
            CharStream charStream = CharStreams.fromFileName(filename);
            compileToFile(charStream);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void compile(InputStream is) {
        try {
            CharStream charStream = CharStreams.fromStream(is);
            compileToFile(charStream);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void compileToFile(CharStream charStream) {
        try {
            QuDotAsmLexer lexer = new QuDotAsmLexer(charStream);
            QuDotAssembler quDotAssembler = new QuDotAssembler(lexer, Bytecodes.instructions);

            Files.write(Paths.get(outputDir, getOutFileName()), getQuDotByteCodeFile(quDotAssembler));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public byte[] getQuDotByteCodeFile(QuDotAssembler assembler) {
        try {
            ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
            BytecodeUtils.writeInt(outBytes, VERSION);
            BytecodeUtils.writeInt(outBytes, assembler.getNumQubits());
            BytecodeUtils.writeInt(outBytes, assembler.getEnsembleSize());

            writeConstPoolObj(outBytes, assembler.getMainGate());

            List<Object> constPool = assembler.getConstPool();
            BytecodeUtils.writeInt(outBytes, constPool.size());

            for (Object obj : constPool) {
                writeConstPoolObj(outBytes, obj);
            }

            outBytes.write(assembler.getBytecode(), 0, assembler.getCodeSize());
            return outBytes.toByteArray();
        } catch (IOException ioe) {
            throw new RuntimeException("unable to write bytes");
        }
    }

    private void writeConstPoolObj(ByteArrayOutputStream byteArrayOutputStream, Object obj) throws IOException {
        if (obj instanceof GateAsmSymbol) {
            GateAsmSymbol gateAsmSymbol = (GateAsmSymbol) obj;
            byteArrayOutputStream.write(ConstPoolType.GATE);

            byte[] gateBytes = gateAsmSymbol.getBytes();
            BytecodeUtils.writeInt(byteArrayOutputStream, gateBytes.length);
            byteArrayOutputStream.write(gateBytes);
        }
    }

    public String getOutFileName() {
        String outFile = Paths.get(filename).getFileName().toString();
        outFile = outFile.split("\\.")[0];
        return outFile + OUTPUT_FILE_EXT;
    }

    public String getFilename() {
        return filename;
    }

    public String getOutputDir() {
        return outputDir;
    }

    @Override
    public void run() {
        boolean disassemble = false;
        boolean debug = false;

        try {
            compile();
        } catch (RuntimeException e) {
            System.err.println("error:" + e.getMessage());
            System.exit(1);
        }
    }
}
