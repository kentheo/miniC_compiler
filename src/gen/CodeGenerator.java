package gen;

import ast.*;
import com.sun.org.apache.regexp.internal.RE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.sql.Struct;
import java.util.*;

public class CodeGenerator implements ASTVisitor<Register> {

    private int strName = 0;

    private boolean areParams = false;
    private boolean isGlobal = true;

    private List<Register> usedRegs = new LinkedList<>();
    private HashMap<Register, Integer> usedRegsMap= new HashMap<>();

    private List<String> globalVars = new LinkedList<>();
    private HashMap<String, Integer> offsetTable = new HashMap<>();
    private HashMap<String, Integer> paramsTable = new HashMap<>();

    private HashMap<String, HashMap<String, Integer>> globalStructs = new HashMap<>();

    private int paramsCounter = 0;
    private int varCounter = 0;
    private int counter = 0;

    private int binOpCounter = 0;
    private int logicalCounter = 0;
    private int logicalOut = 0;

    private int ifCounter = 0;
    private int skipCounter = 1;

    private int whileCounter = 0;
    private int checkCounter = 1;


    // write a visitor to print string literals
    //
    /*
     * Simple register allocator.
     */

    // contains all the free temporary registers
    private Stack<Register> freeRegs = new Stack<Register>();

    public CodeGenerator() {
        freeRegs.addAll(Register.tmpRegs);
    }

    private class RegisterAllocationError extends Error {}

    private Register getRegister() {
        try {
            return freeRegs.pop();
        } catch (EmptyStackException ese) {
            throw new RegisterAllocationError(); // no more free registers, bad luck! Or bad code
        }
    }

    private void freeRegister(Register reg) {
        freeRegs.push(reg);
    }

    private PrintWriter writer; // use this writer to output the assembly instructions


    public void emitProgram(Program program, File outputFile) throws FileNotFoundException {
        writer = new PrintWriter(outputFile);
        visitProgram(program);
        writer.close();
    }

    private boolean isStruct = false;
//    private String structName = "";
//    private String varName = "";

    @Override
    public Register visitBaseType(BaseType bt) {
        return null;
    }

    @Override
    public Register visitStructType(StructType st) {
        HashMap<String, Integer> innerGlobalStructs = new HashMap<>();
        int structCount = 0;
        for (VarDecl vd : st.varDecls){
            //isStruct = true;
            //vd.accept(this);
            if (isGlobal){
                innerGlobalStructs.put(vd.varName, structCount);
                System.out.println(innerGlobalStructs.get(vd.varName) + ">>>>>>>");
                structCount = structCount + 4;
            }
        }
        if (isGlobal){
            globalStructs.put(st.structName, innerGlobalStructs);
        }
        // store here the number of varDecls so that i can use offset


        return null;
    }

    @Override
    public Register visitBlock(Block b) {
        // TODO: to complete

        for (VarDecl vd : b.varDecls) {
            vd.accept(this);
            //System.out.println(counter + "<<<<<<<<<<<<<<<<<<<<<<<<<<");
            if (offsetTable.containsKey(vd.varName)) {

            } else {
                offsetTable.put(vd.varName, (80+counter));
                //registerTable.put(counter, getRegister());
                counter = counter + varCounter;
            }
        }
        for (Stmt s : b.stmts){
            s.accept(this);
        }
        return null;
    }

    @Override
    public Register visitFunDecl(FunDecl fd) {
        // TODO: to complete
        counter = 0;
        writer.println(fd.name + ":");
        int fd_params = fd.params.size();

        if (fd_params != 0) {
            areParams = true;
            paramsCounter = 4;
            for (int i = 0; i < fd_params; i++) {
                VarDecl vd = fd.params.get(i);
                vd.accept(this);
                paramsTable.put(vd.varName, paramsCounter);
                paramsCounter = paramsCounter + varCounter;
            }
            paramsCounter = 0;
            areParams = false;
        }

        // store all used registers and setup frame pointer
        writer.println("move  " + Register.fp.toString() + ", " + Register.sp.toString());
        writer.println("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", -80");
        // save $fp, $ra onto stack and decrement stack pointer accordingly
        writer.println("sw  " + Register.fp.toString() + ", " + "4(" + Register.sp.toString() + ")");
        writer.println("sw  " + Register.ra.toString() + ", " + "8(" + Register.sp.toString() + ")");

        int regCount = 12;
        for (Register r : Register.tmpRegs) {
            writer.println("sw  " + r.toString() + ", " + regCount + "(" + Register.sp.toString() + ")");
            regCount = regCount + 4;
        }

        writer.println("addiu  " + Register.fp.toString() + ", " + Register.sp.toString() + ", 80");

        fd.block.accept(this);

        // restore original registers from stack and increment stack pointer accordingly
        writer.println("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", " + counter);
        writer.println();
        writer.println("lw  " + Register.fp.toString() + ", " + "4(" + Register.sp.toString() + ")");
        writer.println("lw  " + Register.ra.toString() + ", " + "8(" + Register.sp.toString() + ")");
        //writer.println("lw  " + Register.v0.toString() + ", " + "8(" + Register.sp.toString() + ")");

        regCount = 12;
        for (Register r : Register.tmpRegs) {
            writer.println("lw  " + r.toString() + ", " + regCount + "(" + Register.sp.toString() + ")");
            regCount = regCount + 4;
        }

        //writer.println("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", " + counter);
        writer.println("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", 80");
        writer.println("jr  " + Register.ra.toString());

        //counter = 0;     // check again when you should make these 2 zero
        varCounter = 0;
        return null;
    }

    @Override
    public Register visitProgram(Program p) {
        // TODO: to complete
        isGlobal = true;
        writer.println(".data");
        for (StructType st : p.structTypes){
            st.accept(this);
        }
        for (VarDecl vd : p.varDecls) {
            vd.accept(this);
            globalVars.add(vd.varName);
        }
        isGlobal = false;
        writer.println("\n\n");
        writer.println(".text\n\n");
        writer.println("start:");          // start of program
        writer.println("jal   main");
        writer.println("li  " + Register.v0.toString() + ", 10");
        writer.println("syscall");
        writer.println("\n");
        for (FunDecl fd : p.funDecls) {
            fd.accept(this);
        }
        // exit
        writer.flush();
        return null;
    }

    @Override
    public Register visitVarDecl(VarDecl vd) {
        // TODO: to complete
        if (isGlobal){
            if (vd.type instanceof StructType){
                Map<String, Integer> map = globalStructs.get(((StructType) vd.type).structName);
                writer.println(vd.varName + ":    .space  " + (map.size()*4));

            }
            else if (vd.type != BaseType.CHAR && !(vd.type instanceof ArrayType)) {
                writer.println(vd.varName + ":  .space  4");
            }
            else if (vd.type == BaseType.CHAR){
                writer.println(vd.varName + ":  .space  1");
            }
            if (vd.type instanceof ArrayType) {
                //System.out.println(((ArrayType) vd.type).type);
                if (((ArrayType) vd.type).type == BaseType.CHAR) {
                    writer.println(vd.varName + ":    .space  " + ((ArrayType) vd.type).numElements);
                } else {
                    writer.println(vd.varName + ":    .space  " + ((ArrayType) vd.type).numElements * 4);
                }
            }
        } else {
            // stack
            if (!(vd.type instanceof ArrayType)) {
                if (!areParams) {
                    writer.println("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", -4");
                }
                varCounter = 4;
            } else {
                //System.out.println(((ArrayType) vd.type).type);
                if (((ArrayType) vd.type).type == BaseType.CHAR) {
                    if (!areParams) {
                        writer.println("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", " + ((ArrayType) vd.type).numElements * -1);
                    }
                    varCounter = ((ArrayType) vd.type).numElements;
                } else {
                    if (!areParams) {
                        writer.println("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", " + ((ArrayType) vd.type).numElements * -4);
                    }
                    varCounter = ((ArrayType) vd.type).numElements * 4;
                }
            }
        }

        return null;
    }

    @Override
    public Register visitVarExpr(VarExpr v) {
        // TODO: to complete
        // doesnt have to be in globals
//        Register addrR = getRegister();
        Register result = getRegister();
//        writer.println("li " + addrR.toString() + ", " + v.name);
//        writer.println("la " + result.toString() + ", " + addrR.toString());
//        freeRegister(addrR);
        if (offsetTable.containsKey(v.name) && paramsTable.containsKey(v.name)){
//            int k = offsetTable.get(v.name);
//            System.out.println(-(offsetTable.get(v.name)));
            writer.println("lw  " + result.toString() + ", " + -(offsetTable.get(v.name)) + "(" + Register.fp.toString() + ")");
//        if (offsetTable.containsKey(v.name)){
//            writer.println("sw  " + );
//            writer.println("lw  " + result.toString() + ", " + offsetTable.get(v.name).toString() + "(" + Register.fp.toString() + ")");
        }
        else if (paramsTable.containsKey(v.name)){
            //System.out.println(paramsTable.get(v.name));
            writer.println("lw  " + result.toString() + ", " + paramsTable.get(v.name) + "(" + Register.fp.toString() +")");
        }
        else if (offsetTable.containsKey(v.name)){
            writer.println("lw  " + result.toString() + ", " + -(offsetTable.get(v.name)) + "(" + Register.fp.toString() + ")");
        }

        else {
            writer.println("lw  " + result.toString() + ", " + v.name);
        }
        return result;
    }

    public Register visitOp(Op op){
        return null;
    }

    public Register visitFunCallExpr(FunCallExpr fce){
            Register result = getRegister();
        if (fce.args.size() == 0){
            switch (fce.name) {
                case "read_i": {
                    writer.println("li " + Register.v0.toString() + ", " + "5");
                    writer.println("syscall");
                    //writer.println("move  " + readR.toString() + ", " + Register.v0.toString());
                    writer.println();
                    break;
                }
                case "read_c": {
                    //Register readR = e.accept(this);
                    //writer.println("la " + Register.paramRegs[0] + ", (" + readR.toString() + ")");
                    writer.println("li " + Register.v0.toString() + ", " + "12");
                    writer.println("syscall");
                    //writer.println("move  " + readR.toString() + ", " + Register.v0.toString());
                    writer.println();
                    break;
                }
                default:{
                    writer.println("jal  " + fce.name);
                    writer.println();
                }
            }
        } else {
            switch (fce.name) {
                case "print_i": {
                    // la  $a0 , $ --> address of IntLiteral
                    // li $v0 , 1  ---> for print integers
                    Register readR = fce.args.get(0).accept(this);
                    writer.println("la " + Register.paramRegs[0] + ", (" + readR.toString() + ")");
                    //writer.println("lw  " + getRegister().toString() + ", (" + readR.toString() + ")");
                    writer.println("li " + Register.v0.toString() + ", " + "1");
                    writer.println("syscall");
                    writer.println();
                    freeRegister(readR);
                    break;
                }
                case "print_c": {
                    Register readR = fce.args.get(0).accept(this);
                    writer.println("la " + Register.paramRegs[0] + ", (" + readR.toString() + ")");
                    writer.println("li " + Register.v0.toString() + ", " + "11");
                    writer.println("syscall");
                    writer.println();
                    freeRegister(readR);
                    break;
                }
                case "print_s": {
                    Register readR = fce.args.get(0).accept(this);
                    writer.println("la  " + Register.paramRegs[0] + ", (" + readR.toString() + ")");
                    writer.println("li " + Register.v0.toString() + ", " + "4");
                    writer.println("syscall");
                    writer.println();
                    freeRegister(readR);
                    break;
                }
                default:
                    int helperCount = 4;   // return value must be considered
                    // Store the args right before the fp
                    writer.println("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", -4\t## space for return value");
                    writer.println("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", -" + (fce.args.size()*4) + "\t# decrement stack for args");
                    for (Expr e: fce.args) {
                        Register readR = e.accept(this);
                        writer.println("sw  " + readR.toString() + ", "  + (helperCount) + "(" + Register.sp.toString() + ")");  // stores the arguments by adding the counter of the variables above the fce
                        helperCount = helperCount + 4;
                        freeRegister(readR);
                    }
                    helperCount = (fce.args.size() * 4);   // need it to store the return value
                    writer.println("jal  " + fce.fd.name);
                    writer.println("sw  " + Register.v0.toString() + ", " + -(80+counter) + "(" + Register.fp.toString() + ")");
                    writer.println("add " + Register.sp.toString() + ", " + Register.sp.toString() + ", " + (fce.args.size()*4) + "\t# restore stack from args");
                    writer.println("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", 4");
                    writer.println("add  " + result.toString() + ", $zero, " + Register.v0.toString());
                    return result;
            }
        }
        return result;
    }

    public Register visitArrayType(ArrayType at){
        return null;
    }

    public Register visitAssign(Assign a) {
        Register lhsReg = a.lhs.accept(this);
        Register rhsReg = a.rhs.accept(this);
        //Register helperR = getRegister();

        if (a.lhs instanceof VarExpr){
            //writer.println("la  " + lhsReg.toString() + ", (" + rhsReg.toString() + ")");
            if (globalVars.contains(((VarExpr) a.lhs).name)) {
                // so far to handle read_i and read_c
                if (a.rhs instanceof FunCallExpr){
                    writer.println("sw  " + Register.v0.toString() + ", " + ((VarExpr) a.lhs).name);
                } else {
                    writer.println("sw  " + rhsReg.toString() + ", " + ((VarExpr) a.lhs).name);// store it back so that when it's called again it has the value we want
                }
            } else {
                // so far to handle read_i and read_c
                if (a.rhs instanceof FunCallExpr) {
                    if (((FunCallExpr) a.rhs).name.equals("read_i") || ((FunCallExpr) a.rhs).name.equals("read_c")) {
                        writer.println("sw  " + Register.v0.toString() + ", " + -(offsetTable.get(((VarExpr) a.lhs).name)) + "(" + Register.fp.toString() + ")");
                    }
                    else {
                        //writer.println("jal  " + ((FunCallExpr) a.rhs).name);
                        writer.println("sw  " + Register.v0.toString() + ", " + "4(" + Register.fp.toString() + ")");
                        writer.println("sw  " + Register.v0.toString() + ", " + -(offsetTable.get(((VarExpr) a.lhs).name)) + "(" + Register.fp.toString() + ")");
                    }
                } else {
                    if (offsetTable.containsKey(((VarExpr) a.lhs).name)) {
                        writer.println("sw  " + rhsReg.toString() + ", " + -(offsetTable.get(((VarExpr) a.lhs).name)) + "(" + Register.fp.toString() + ")");
                    } else {
                        writer.println("sw  " + rhsReg.toString() + ", " + paramsTable.get(((VarExpr) a.lhs).name) + "(" + Register.fp.toString() + ")");
                    }
                }
            }
        }
        if (a.lhs instanceof ArrayAccessExpr){
            //writer.println("la  " + lhsReg.toString() + ", (" + rhsReg.toString() + ")");
            if (globalVars.contains(((VarExpr)((ArrayAccessExpr) a.lhs).array).name)){
                writer.println("sw  " + rhsReg.toString() + ", 0(" + lhsReg.toString() + ")");  // store it back so that when it's called again it has the value we want
            } else {
                writer.println("sw  " + rhsReg.toString() + ", " + offsetTable.get(((VarExpr)((ArrayAccessExpr) a.lhs).array).name) + "(" + Register.fp.toString() + ")");
            }
        }
        if (a.lhs instanceof FieldAccessExpr){
            writer.println("sw  " + rhsReg.toString() + ", (" + lhsReg.toString() + ")");
        }
        freeRegister(lhsReg);
        freeRegister(rhsReg);

        return null;
    }

    public Register visitBinOp(BinOp bo){
        Register lhsReg = bo.lhs.accept(this);
        Register rhsReg = bo.rhs.accept(this);
        Register result = getRegister();

        switch (bo.op){
            case ADD:
                writer.println("add "+ result.toString() + ", " + lhsReg.toString() + ", " + rhsReg.toString());
                break;
            case SUB:
                System.out.println(result + ">>>>>>>>>>> RESULT");
                writer.println("sub "+ result.toString() + ", " + lhsReg.toString() + ", " + rhsReg.toString());
                break;
            case MUL:
                writer.println("mul "+ result.toString() + ", " + lhsReg.toString() + ", " + rhsReg.toString());
                break;
            case DIV:
                writer.println("div "+ result.toString() + ", " + lhsReg.toString() + ", " + rhsReg.toString());
                break;
            case MOD:
                writer.println("rem "+ result.toString() + ", " + lhsReg.toString() + ", " + rhsReg.toString());
                break;
            case GT:
                writer.println("bgt  " + lhsReg.toString() + ", " + rhsReg.toString() + ", Lt" + binOpCounter);
                writer.println("li  " + result.toString() + ", 0");
                writer.println("b  Le" + binOpCounter);
                writer.println("Lt" + binOpCounter + ":");
                writer.println("li  " + result.toString() + ", 1");
                writer.println("Le" + binOpCounter + ":");
                binOpCounter++;
                break;
            case LT:
                writer.println("blt  " + lhsReg.toString() + ", " + rhsReg.toString() + ", Lt" + binOpCounter);
                writer.println("li  " + result.toString() + ", 0");
                writer.println("b  Le" + binOpCounter);
                writer.println("Lt" + binOpCounter + ":");
                writer.println("li  " + result.toString() + ", 1");
                writer.println("Le" + binOpCounter + ":");
                binOpCounter++;
                break;
            case GE:
                writer.println("bge  " + lhsReg.toString() + ", " + rhsReg.toString() + ", Lt" + binOpCounter);
                writer.println("li  " + result.toString() + ", 0");
                writer.println("b  Le" + binOpCounter);
                writer.println("Lt" + binOpCounter + ":");
                writer.println("li  " + result.toString() + ", 1");
                writer.println("Le" + binOpCounter + ":");
                binOpCounter++;
                break;
            case LE:
                writer.println("ble  " + lhsReg.toString() + ", " + rhsReg.toString() + ", Lt" + binOpCounter);
                writer.println("li  " + result.toString() + ", 0");
                writer.println("b  Le" + binOpCounter);
                writer.println("Lt" + binOpCounter + ":");
                writer.println("li  " + result.toString() + ", 1");
                writer.println("Le" + binOpCounter + ":");
                binOpCounter++;
                break;
            case EQ:
                writer.println("beq  " + lhsReg.toString() + ", " + rhsReg.toString() + ", Lt" + binOpCounter);
                writer.println("li  " + result.toString() + ", 0");
                writer.println("b  Le" + binOpCounter);
                writer.println("Lt" + binOpCounter + ":");
                writer.println("li  " + result.toString() + ", 1");
                writer.println("Le" + binOpCounter + ":");
                binOpCounter++;
                break;
            case NE:
                writer.println("bne  " + lhsReg.toString() + ", " + rhsReg.toString() + ", Lt" + binOpCounter);
                writer.println("li  " + result.toString() + ", 0");
                writer.println("b  Le" + binOpCounter);
                writer.println("Lt" + binOpCounter + ":");
                writer.println("li  " + result.toString() + ", 1");
                writer.println("Le" + binOpCounter + ":");
                binOpCounter++;
                break;
            case AND:
                writer.println("bnez  " + lhsReg.toString() + ", checker" + logicalCounter);
                writer.println("b  checker" + (logicalCounter+1));
                writer.println("checker" + logicalCounter + ":");
                writer.println("bnez  " + rhsReg.toString() + ", checker" + (logicalCounter+2));
                writer.println("checker" + (logicalCounter+1) + ":");
                writer.println("li  " + result.toString() + ", 0");
                writer.println("b  out" + logicalOut);
                writer.println("checker" + (logicalCounter+2) + ":");
                writer.println("li  " + result.toString() + ", 1");
                writer.println("out" + logicalOut + ":");

                logicalCounter++;
                logicalOut++;
                break;
            case OR:
                writer.println("bnez  " + lhsReg.toString() + ", checker" + logicalCounter);
                writer.println("bnez  " + rhsReg.toString() + ", checker" + logicalCounter);
                writer.println("li  " + result.toString() + ", 0");
                writer.println("b  out" + logicalOut);
                writer.println("checker" + logicalCounter + ":");
                writer.println("li  " + result.toString() + ", 1");
                writer.println("out" + logicalOut + ":");
                logicalCounter++;
                logicalOut++;
                break;
        }
        freeRegister(lhsReg);
        freeRegister(rhsReg);
        return result;
    }

    public Register visitReturn(Return r){
        Register readR = r.expr.accept(this);
        writer.println("add  " + Register.v0.toString() + ", $zero, " + readR.toString());
        freeRegister(readR);
        return Register.v0;
    }

    public Register visitPointerType(PointerType pt){
        return null;
    }

    public Register visitFieldAccessExpr(FieldAccessExpr fae){
//        System.out.println(fae.structure.accept(this));
        System.out.println(fae.fieldName + "<<<<<<<<<<<<<<fieldname");
        Register readR = fae.structure.accept(this);  // register of VarExpr st
        Register result = getRegister();
        if (globalVars.contains(((VarExpr) fae.structure).name)) {
            writer.println("la  " + result.toString() + ", " + ((VarExpr) fae.structure).name);
        }
        //writer.println("sll  " + indexR.toString() + ", " + indexR.toString() + ", 2");   // shift left by 2 bits --> multiplies register by 4
        System.out.println(((VarExpr) fae.structure).name);
//        Map<String, Integer> map = globalStructs.get(((VarExpr) fae.structure).name);
        //System.out.println(map.containsKey(fae.fieldName) + "does it?");
//        System.out.println(map.get(fae.fieldName));
//        writer.println("add  " + result.toString() + ", " + result.toString() + ", " + map.get(fae.fieldName));
        return result;
    }

    public Register visitArrayAccessExpr(ArrayAccessExpr aae){
        Register indexR = aae.index.accept(this);
        Register arrayR = aae.array.accept(this);
        Register result = getRegister();

        if (globalVars.contains(((VarExpr) aae.array).name)) {
            writer.println("la  " + result.toString() + ", " + ((VarExpr) aae.array).name);
        } else {
            writer.println("la  " + result.toString() + ", " + arrayR.toString());
        }
        writer.println("sll  " + indexR.toString() + ", " + indexR.toString() + ", 2");   // shift left by 2 bits --> multiplies register by 4
        writer.println("add  " + result.toString() + ", " + result.toString() + ", " + indexR.toString());

        freeRegister(indexR);
        freeRegister(arrayR);
        return result;
    }

    public Register visitSizeOfExpr(SizeOfExpr soe){
        Register result = getRegister();
        if (soe.type != BaseType.CHAR) {
            writer.println("li  " + result.toString() + ", " + 4);
        }
        if (soe.type instanceof ArrayType) {
            if (((ArrayType) soe.type).type ==BaseType.CHAR) {
                writer.println("li  " + result.toString() + ", " + ((ArrayType) soe.type).numElements);
            } else {
                writer.println("li  " + result.toString() + ", " + ((ArrayType) soe.type).numElements * 4);
            }
        }
        if (soe.type == BaseType.CHAR){
            writer.println("li  " + result.toString() + ", " + 1);
        }
        return result;
    }

    public Register visitValueAtExpr(ValueAtExpr vae){

        return vae.expr.accept(this);
    }

    public Register visitChrLiteral(ChrLiteral cl){
        Register result = getRegister();
        writer.println("li  " + result.toString() + ", '" + cl.character + "'");
        return result;
    }

    public Register visitIntLiteral(IntLiteral il){
        Register result = getRegister();
        writer.println("li  " + result.toString() + ", " + il.integer);
        return result;
    }

    public Register visitStrLiteral(StrLiteral sl){
        writer.println(".data");
        writer.println("str" + strName + ":    .asciiz  " + "\"" + sl.string + "\"");
        writer.println(".text");
        Register strR = getRegister();
        writer.println("la  " + strR.toString() + ", " + "str" + strName);
        strName++;
        return strR;
    }

    public Register visitWhile(While w){
        whileCounter++;
        writer.println("check" + whileCounter + ":");
        Register readR = w.expr.accept(this);
        writer.println("beqz  " + readR.toString() + ", label" + whileCounter);
        w.stmt.accept(this);
        writer.println("j check" + checkCounter);
        writer.println("label" + checkCounter + ":");
        checkCounter++;
        freeRegister(readR);
        return null;
    }

    public Register visitIf(If i){
        ifCounter++;
        Register readR = i.expr.accept(this);
        writer.println("beqz  " + readR.toString() + ", next" + ifCounter);
        i.stmt.accept(this);
        if (i.opt_stmt != null){
            writer.println("next" + ifCounter  + ":");
            writer.println("bnez  " + readR.toString() + ", skip" + ifCounter);
            i.opt_stmt.accept(this);
            writer.println("skip" + skipCounter + ":");
        } else {
            writer.println("next" + ifCounter + ":");
            writer.println();
        }
        skipCounter++;
        freeRegister(readR);
        return null;
    }

    public Register visitExprStmt(ExprStmt es){

        return es.expr.accept(this);
    }

    public Register visitTypecastExpr(TypecastExpr tce){

        return tce.expr.accept(this);
    }

}
