package ast;

/**
 * Created by TOSHIBA-PC on 12/10/2016.
 */
public class SizeOfExpr extends Expr {

    public final Type type;

    public SizeOfExpr(Type type){
        this.type = type;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitSizeOfExpr(this);
    }

}
