package ast;

/**
 * Created by TOSHIBA-PC on 12/10/2016.
 */
public class IntLiteral extends Expr {
    public final int integer;

    public IntLiteral(int integer){
        this.integer = integer;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitIntLiteral(this);
    }
}
