package ast;

/**
 * Created by TOSHIBA-PC on 14/10/2016.
 */
public class Return extends Stmt {

    public final Expr expr;

    public Return(Expr expr){
        this.expr = expr;

    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitReturn(this);
    }
}
