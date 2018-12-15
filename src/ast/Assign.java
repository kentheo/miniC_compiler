package ast;

/**
 * Created by TOSHIBA-PC on 12/10/2016.
 */
public class Assign extends Stmt {

    public final Expr lhs;
    public final Expr rhs;

    public Assign(Expr lhs, Expr rhs){
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public <T> T accept(ASTVisitor<T> v) { return v.visitAssign(this);}
}
