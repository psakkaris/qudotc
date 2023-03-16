package io.qudot.qudotc.qudir;

import io.qudot.qudotc.utils.BytecodeUtils;
import io.qudot.qudotc.utils.Bytecodes;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

/**
 * QuDotAssembler has all the information necessary to perform a computation on a QuDot VM.
 * It serves as a Tree Visitor for a QuDotAsmParser that has parsed a .qudot input file.
 * As we visit nodes on the AST web generate bytecode, detect the number of qubits, ensemble size,
 * the main gate address, the constant pool and maintain the labels.
 *
 * @since 0.1.0
 */
public class QuDotAssembler extends QuDotAsmBaseVisitor<Void> {
    private static final int INITIAL_CODE_SIZE = 1024;
    private static final String MAIN_GATE_NAME = "main";

    private QuDotAsmLexer lexer;
    private Map<String, Integer> opCodeMapping = new HashMap<>();
    private Map<String, LabelSymbol> labels = new HashMap<>();

    // Gate Definitions go in here
    private List<Object> constPool = new ArrayList<>();

    private GateAsmSymbol mainGate;
    private Integer numQubits;
    private Integer ensembleSize;
    private int ip = 0;
    private byte[] code = new byte[INITIAL_CODE_SIZE];

    public QuDotAssembler(QuDotAsmLexer lexer, Bytecodes.Instruction[] instructions) {
        this.lexer = lexer;
        for (int i=0; i < instructions.length; i++) {
            opCodeMapping.put(instructions[i].getName().toLowerCase(), i);
        }
        assemble();
    }

    private void assemble() {
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        QuDotAsmParser parser = new QuDotAsmParser(tokenStream);

        ParseTree tree = parser.program();
        visit(tree);
        checkForUnresolvedReferences();
    }

    public byte[] getBytecode() {
        return code;
    }

    public int getCodeSize() {
        return ip;
    }

    public GateAsmSymbol getMainGate() {
        return mainGate;
    }

    public List<Object> getConstPool() {
        return constPool;
    }

    public Integer getNumQubits() {
        return numQubits;
    }

    public Integer getEnsembleSize() {
        return ensembleSize;
    }

    // After parser is complete, look for unresolved labels
    public void checkForUnresolvedReferences() {
        for (String name : labels.keySet()) {
            LabelSymbol sym = labels.get(name);
            if ( !sym.isDefined ) {
                System.err.println("unresolved reference: "+ name);
            }
        }
    }

    @Override
    public Void visitQudot(QuDotAsmParser.QudotContext ctx) {
        numQubits = Integer.parseInt(ctx.q.getText());
        ensembleSize = Integer.parseInt(ctx.e.getText());

        return super.visitQudot(ctx);
    }

    @Override
    public Void visitGateDeclaration(QuDotAsmParser.GateDeclarationContext ctx) {
        String name = ctx.name.getText();
        int args = Integer.parseInt(ctx.a.getText());
        int regs = Integer.parseInt(ctx.rn.getText());
        int qubitRegs = Integer.parseInt(ctx.qn.getText());
        // address is where .gate appears
        int address = ip;
        GateAsmSymbol gateSymbol = new GateAsmSymbol(name, args, regs, qubitRegs, address);

        if (name.equals(MAIN_GATE_NAME)) {
            mainGate = gateSymbol;
        }

        if (constPool.contains(gateSymbol)) {
            constPool.set(constPool.indexOf(gateSymbol), gateSymbol);
        } else {
            getConstantPoolIndex(gateSymbol);
        }

        return super.visitGateDeclaration(ctx);
    }

    @Override
    public Void visitLabel(QuDotAsmParser.LabelContext ctx) {
        Token labelToken = ctx.ID().getSymbol();
        defineLabel(labelToken);
        return super.visitLabel(ctx);
    }

    @Override
    public Void visitArrayInstr(QuDotAsmParser.ArrayInstrContext ctx) {
        genOpcode(ctx.getStart(), ctx.a.start, ctx.b.start);
        for (TerminalNode tn : ctx.INT()) {
            genOperand(tn.getSymbol());
        }
        return super.visitArrayInstr(ctx);
    }

    @Override
    public Void visitInstr(QuDotAsmParser.InstrContext ctx) {
        if (ctx.arrayInstr() == null) {
            Token instrToken = ctx.ID().getSymbol();

            if (ctx.a != null && ctx.b != null && ctx.c != null && ctx.d != null && ctx.f != null) {
                genOpcode(instrToken, ctx.a.start, ctx.b.start, ctx.c.start, ctx.d.start, ctx.f.start);
            } else if (ctx.a != null && ctx.b != null && ctx.c != null && ctx.d != null) {
                genOpcode(instrToken, ctx.a.start, ctx.b.start, ctx.c.start, ctx.d.start);
            } else if (ctx.a != null && ctx.b != null && ctx.c != null) {
                genOpcode(instrToken, ctx.a.start, ctx.b.start, ctx.c.start);
            } else if (ctx.a != null && ctx.b != null) {
                genOpcode(instrToken, ctx.a.start, ctx.b.start);
            } else if (ctx.a != null) {
                genOpcode(instrToken, ctx.a.start);
            } else {
                genOpcode(instrToken);
            }
        }
        return super.visitInstr(ctx);
    }

    private void genOpcode(Token token) {
        String opCodeName = token.getText();
        int lineNumber = token.getLine();

        Integer opCode = opCodeMapping.get(opCodeName);
        if (opCode == null) {
            System.err.println("line: " + lineNumber + " Invalid instruction (" + opCodeName + ")");
            return;
        }
        ensureCapacity(ip+1);
        code[ip++] = (byte)(opCode&0xFF);
    }

    private void genOpcode(Token instrToken, Token opAToken) {
        genOpcode(instrToken);
        genOperand(opAToken);
    }

    private void genOpcode(Token instrToken, Token opAToken, Token opBToken) {
        genOpcode(instrToken);
        genOperand(opAToken);
        genOperand(opBToken);
    }

    private void genOpcode(Token instrToken, Token opAToken, Token opBToken, Token opCToken) {
        genOpcode(instrToken);
        genOperand(opAToken);
        genOperand(opBToken);
        genOperand(opCToken);
    }

    private void genOpcode(Token instrToken, Token opAToken, Token opBToken, Token opCToken, Token opDToken) {
        genOpcode(instrToken);
        genOperand(opAToken);
        genOperand(opBToken);
        genOperand(opCToken);
        genOperand(opDToken);
    }

    private void genOpcode(Token instrToken, Token opAToken, Token opBToken, Token opCToken, Token opDToken, Token opFToken) {
        genOpcode(instrToken);
        genOperand(opAToken);
        genOperand(opBToken);
        genOperand(opCToken);
        genOperand(opDToken);
        genOperand(opFToken);
    }

    private void genOperand(Token operandToken) {
        String text = operandToken.getText();
        int v = 0;
        switch ( operandToken.getType() ) { // switch on token type
            case Bytecodes.INT :
                v = Integer.valueOf(text);
                break;
            case Bytecodes.ID :
                v = getLabelAddress(text);
                break;
            case Bytecodes.GATE :
                String gateText = text.substring(0, text.indexOf("("));
                v = getGateIndex(gateText);
                break;
            case Bytecodes.REG :
                v = getRegisterNumber(operandToken);
                break;
            case Bytecodes.QUREG :
                v = getRegisterNumber(operandToken);
                break;
        }
        ensureCapacity(ip+4);  // expand code array if necessary
        BytecodeUtils.writeInt(code, ip, v); // write operand to code byte array
        ip += 4;               // we've written four bytes
    }

    private int getConstantPoolIndex(Object o) {
        if ( constPool.contains(o) ) {
            return constPool.indexOf(o);
        }
        constPool.add(o);
        return constPool.size()-1;
    }

    private void ensureCapacity(int index) {
        if ( index >= code.length ) { // expand
            int newSize = Math.max(index, code.length) * 2;
            byte[] bigger = new byte[newSize];
            System.arraycopy(code, 0 , bigger, 0, code.length);
            code = bigger;
        }
    }

    // convert "rN" -> N
    private int getRegisterNumber(Token rtoken) {
        String rs = rtoken.getText();
        rs = rs.substring(1);
        return Integer.valueOf(rs);
    }

    private int getGateIndex(String id) {
        int i = constPool.indexOf(new GateAsmSymbol(id));
        if ( i>=0 ) {
            // already in system; return index
            return i;
        }
        // must be a forward function reference
        // create the constant pool entry; we'll fill in later
        return getConstantPoolIndex(new GateAsmSymbol(id));
    }

    private int getLabelAddress(String id) {
        LabelSymbol sym = labels.get(id);
        if ( sym==null ) {
            // assume it's a forward code reference; record opnd address
            sym = new LabelSymbol(id, ip, true);
            sym.isDefined = false;
            labels.put(id, sym);
        }
        else {
            if ( sym.isForwardRef ) {
                // address is unknown, must simply add to forward ref list
                // record where in code memory we should patch later
                sym.addForwardReference(ip);
            }
            else {
                // all is well; it's defined--just grab address
                return sym.address;
            }
        }
        // we don't know the real address yet
        return 0;
    }

    private void defineLabel(Token idToken) {
        String id = idToken.getText();
        LabelSymbol sym = labels.get(id);
        if ( sym==null ) {
            LabelSymbol lsym = new LabelSymbol(id, ip, false);
            labels.put(id, lsym);
        }
        else {
            if ( sym.isForwardRef ) {
                // we have found definition of forward
                sym.isDefined = true;
                sym.address = ip;
                sym.resolveForwardReferences(code);
            }
            else {
                // redefinition of symbol
                System.err.println("line " + idToken.getLine() + ": redefinition of symbol "+id);
            }
        }
    }

}
