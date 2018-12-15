package ast;

/**
 * Created by TOSHIBA-PC on 12/10/2016.
 */
public class ValueAtExpr extends Expr {

    public final Expr expr;

    public ValueAtExpr(Expr expr){
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitValueAtExpr(this);
    }


}
