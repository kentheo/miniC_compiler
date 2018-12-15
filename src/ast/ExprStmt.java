package ast;

/**
 * Created by TOSHIBA-PC on 12/10/2016.
 */
public class ExprStmt extends Stmt {

    public final Expr expr;

    public ExprStmt(Expr expr){
        this.expr = expr;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitExprStmt(this);
    }
}
