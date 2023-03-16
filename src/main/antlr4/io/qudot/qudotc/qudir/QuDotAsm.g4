grammar QuDotAsm;

program: qudot?
         ( gateDeclaration | instr | label | NEWLINE )+
         ;

qudot : NEWLINE* '.qudot' 'qubits' '=' q=INT ',' 'ensemble' '=' e=INT NEWLINE;

//  .gate bell2: args=3, regs=1, qubit_regs=3
gateDeclaration
    : '.gate' name=ID ':' 'args' '=' a=INT ',' 'regs' '=' rn=INT ',' 'qubit_regs' '=' qn=INT NEWLINE
    ;

instr
    :   ID NEWLINE                         // {gen($ID);}
    |   ID a=operand NEWLINE                 // {gen($ID,$operand.start);}
    |   ID a=operand ',' b=operand NEWLINE // {gen($ID,$a.start,$b.start);}
    |   ID a=operand ',' b=operand ',' c=operand NEWLINE
    |   ID a=operand ',' b=operand ',' c=operand ',' d=operand NEWLINE
    |   ID a=operand ',' b=operand ',' c=operand ',' d=operand ',' f=operand NEWLINE
    |   arrayInstr
    ;

arrayInstr: 'qload_array' a=operand ',' b=operand ',' (INT (','INT)*) NEWLINE;

operand
    :   ID   // basic code label; E.g., "loop"
    |   REG  // register name; E.g., "r0"
    |   QUREG // qubit register name E.g., "q0"
    |   GATE // function label; E.g., "f()"
    |   INT
    ;

label:   ID ':';

REG :   'r' INT ;

QUREG:  'q' INT ;

ID  :   LETTER (LETTER | '0'..'9')* ;

GATE:   ID '()' ;

fragment
LETTER
    :   ('a'..'z' | 'A'..'Z' | '_')
    ;

INT :   '-'? '0'..'9'+ ;

WS  :   (' '|'\t')+ -> skip ;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip ;

NEWLINE
    :   ('//' .)*? '\r'? '\n'  // optional comment followed by newline
    ;