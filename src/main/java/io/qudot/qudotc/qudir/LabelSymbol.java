package io.qudot.qudotc.qudir;

import io.qudot.qudotc.utils.BytecodeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for management of Labels in "label:" definitions. We track the name, address of label,
 * if its defined and if it is a forward reference.
 *
 * @since 0.1.0
 */
public class LabelSymbol {
    String name;
    // Address in code memory
    int address;
    boolean isForwardRef = false;
    // Set when we see actual ID: definition
    boolean isDefined = true;
    // List of operands in memory we need to update after seeing def
    List<Integer> forwardReferences = null;

    public LabelSymbol(String name) {
        this.name = name;
    }

    public LabelSymbol(String name, int address) {
        this(name);
        this.address = address;
    }

    public LabelSymbol(String name, int address, boolean forward) {
        this(name);
        isForwardRef = forward;
        if ( forward ) {
            // if forward reference, then address is address to update
            addForwardReference(address);
        }
        else {
            this.address = address;
        }
    }

    /**
     * If we find a label that has not been seen yet such as br, lab1 then we add it to the forward references
     * list to resolve at a later time. Only at the end to we throw an error for a label that we cannot see.
     * @param address IP where we saw the label
     */
    public void addForwardReference(int address) {
        if ( forwardReferences==null ) {
            forwardReferences = new ArrayList<>();
        }
        forwardReferences.add(address);
    }

    /**
     * For all forward reference we have to go back and patch up all references to this symbol
     * with the appropriate code addresses.
     * @param code bytecode array
     */
    public void resolveForwardReferences(byte[] code) {
        isForwardRef = false;
        List<Integer> opndsToPatch = forwardReferences;
        for (int addrToPatch : opndsToPatch) {
            BytecodeUtils.writeInt(code, addrToPatch, address);
        }
    }

    public String toString() {
        String refs = "";
        if ( forwardReferences!=null ) {
            refs = "[refs="+forwardReferences.toString()+"]";
        }
        return name+"@"+address+refs;
    }
}