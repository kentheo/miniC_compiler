package ast;

public interface ASTVisitor<T> {
    public T visitBaseType(BaseType bt);
    public T visitStructType(StructType st);
    public T visitBlock(Block b);
    public T visitFunDecl(FunDecl p);
    public T visitProgram(Program p);
    public T visitVarDecl(VarDecl vd);
    public T visitVarExpr(VarExpr v);
    public T visitPointerType(PointerType pt);
    public T visitArrayType(ArrayType at);
    public T visitIntLiteral(IntLiteral il);
    public T visitStrLiteral(StrLiteral sl);
    public T visitChrLiteral(ChrLiteral cl);
    public T visitFunCallExpr(FunCallExpr fce);
    public T visitBinOp(BinOp bo);
    public T visitOp(Op op);
    public T visitArrayAccessExpr(ArrayAccessExpr aae);
    public T visitFieldAccessExpr(FieldAccessExpr fa);
    public T visitValueAtExpr(ValueAtExpr vae);
    public T visitSizeOfExpr(SizeOfExpr soe);
    public T visitTypecastExpr(TypecastExpr tce);
    public T visitWhile(While w);
    public T visitIf(If i);
    public T visitAssign(Assign a);
    public T visitReturn(Return r);
    public T visitExprStmt(ExprStmt es);

    // to complete ... (should have one visit method for each concrete AST node class)
}
