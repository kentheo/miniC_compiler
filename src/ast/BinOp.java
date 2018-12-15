package ast;

/**
 * Created by TOSHIBA-PC on 12/10/2016.
 */
public class BinOp extends Expr {

    public final Op op;
    public final Expr lhs;
    public final Expr rhs;

    public BinOp(Expr lhs, Op op, Expr rhs){
        this.lhs = lhs;
        this.op = op;
        this.rhs = rhs;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitBinOp(this);
    }
}
