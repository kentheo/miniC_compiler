package ast;

import java.io.PrintWriter;
import java.lang.reflect.Field;

public class ASTPrinter implements ASTVisitor<Void> {

    private PrintWriter writer;

    public ASTPrinter(PrintWriter writer) {
            this.writer = writer;
    }

    @Override
    public Void visitBlock(Block b) {
        writer.print("Block(");
        // to complete
        String delimiter = "";
        for (VarDecl vd : b.varDecls) {
            writer.print(delimiter);
            delimiter = ",";
            vd.accept(this);
        }
        for (Stmt s : b.stmts) {
            writer.print(delimiter);
            delimiter = ",";
            s.accept(this);
        }////////////////////////////////////
        writer.print(")");
        ////////////////////////////////
        return null;
    }

    @Override
    public Void visitFunDecl(FunDecl fd) {
        writer.print("FunDecl(");
        fd.type.accept(this);
        writer.print(","+fd.name+",");
        for (VarDecl vd : fd.params) {
            vd.accept(this);
            writer.print(",");
        }
        fd.block.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitProgram(Program p) {
        writer.print("Program(");
        String delimiter = "";
        for (StructType st : p.structTypes) {
            writer.print(delimiter);
            delimiter = ",";
            st.accept(this);
        }
        for (VarDecl vd : p.varDecls) {
            writer.print(delimiter);
            delimiter = ",";
            vd.accept(this);
        }
        for (FunDecl fd : p.funDecls) {
            writer.print(delimiter);
            delimiter = ",";
            fd.accept(this);
        }
        writer.print(")");
	    writer.flush();
        return null;
    }

    @Override
    public Void visitVarDecl(VarDecl vd){
        writer.print("VarDecl(");
        vd.type.accept(this);
        writer.print(","+vd.varName);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitVarExpr(VarExpr v) {
        writer.print("VarExpr(");
        writer.print(v.name);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitBaseType(BaseType bt) {
        // to complete ...
        //writer.print("BaseType(");
        writer.print(bt);
        //writer.print(")");
        return null;
    }
    @Override
    public Void visitPointerType (PointerType pt){
        //to complete
        writer.print("PointerType(");
        pt.type.accept(this);
        writer.print(")");
        return null;
    }
    @Override
    public Void visitStructType(StructType st) {
        // to complete ...
        writer.print("StructType(");
        writer.print(st.structName);
        String delimiter = ",";
        for (VarDecl vd : st.varDecls) {
            writer.print(delimiter);
            delimiter = ",";
            vd.accept(this);
        }
        writer.print(")");
        return null;
    }
    @Override
    public Void visitArrayType(ArrayType at){
        //to complete
        writer.print("ArrayType(");
        at.type.accept(this);
        writer.print("," + at.numElements);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitArrayAccessExpr(ArrayAccessExpr aae){
        //to complete
        writer.print("ArrayAccessExpr(");
        aae.array.accept(this);
        writer.print(",");
        aae.index.accept(this);
        writer.print(")");
        return null;
    }
    // to complete ...
    @Override
    public Void visitBinOp(BinOp bo){
        //to complete
        writer.print("BinOp(");
        bo.lhs.accept(this);
        writer.print(",");
        bo.op.accept(this);
        writer.print(",");
        bo.rhs.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitOp(Op op){
        //to complete
        writer.print(op);
        return null;
    }

    @Override
    public Void visitIntLiteral(IntLiteral il){
        //to complete
        writer.print("IntLiteral(");
        writer.print(il.integer);
        writer.print(")");
        return null;
    }
    @Override
    public Void visitStrLiteral(StrLiteral sl){
        //to complete
        writer.print("StrLiteral(");
        writer.print(sl.string);
        writer.print(")");
        return null;
    }
    @Override
    public Void visitChrLiteral(ChrLiteral cl){
        //to complete
        writer.print("ChrLiteral(");
        writer.print(cl.character);
        writer.print(")");
        return null;
    }
    @Override
    public Void visitFunCallExpr(FunCallExpr fce){
        //to complete
        writer.print("FunCallExpr(");
        writer.print(fce.name);
        String delimiter = ",";
        for (Expr e : fce.args) {
            writer.print(delimiter);
            e.accept(this);
        }
        writer.print(")");
        return null;
    }
    @Override
    public Void visitFieldAccessExpr(FieldAccessExpr fae){
        //to complete
        writer.print("FieldAccessExpr(");
        fae.structure.accept(this);
        writer.print(",");
        writer.print(fae.fieldName);
        writer.print(")");
        return null;
    }
    @Override
    public Void visitValueAtExpr(ValueAtExpr vae){
        //to complete
        writer.print("ValueAtExpr(");
        vae.expr.accept(this);
        writer.print(")");
        return null;
    }
    @Override
    public Void visitSizeOfExpr(SizeOfExpr soe){
        //to complete
        writer.print("SizeOfExpr(");
        soe.type.accept(this);
        writer.print(")");
        return null;
    }
    @Override
    public Void visitTypecastExpr(TypecastExpr tce){
        //to complete
        writer.print("TypecastExpr(");
        tce.type.accept(this);
        writer.print(",");
        tce.expr.accept(this);
        writer.print(")");
        return null;
    }
    @Override
    public Void visitWhile(While w){
        //to complete
        writer.print("While(");
        w.expr.accept(this);
        writer.print(",");
        w.stmt.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitIf(If i) {
        //to complete
        writer.print("If(");
        i.expr.accept(this);
        writer.print(",");
        i.stmt.accept(this);
        if (i.opt_stmt == null){
            writer.print(")");
            return null;
        }
        writer.print(",");
        i.opt_stmt.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitAssign(Assign a){
        //to complete
        writer.print("Assign(");
        a.lhs.accept(this);
        writer.print(",");
        a.rhs.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitReturn(Return r) {
        //to complete
        writer.print("Return(");
        if (r.expr == null){
            writer.print(")");
            return null;
        }
        r.expr.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt es){
        //to complete
        writer.print("ExprStmt(");
        es.expr.accept(this);
        writer.print(")");
        return null;
    }

}
