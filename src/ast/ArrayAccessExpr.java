package ast;

/**
 * Created by TOSHIBA-PC on 12/10/2016.
 */
public class ArrayAccessExpr extends Expr {

    public final Expr array;
    public final Expr index;

    public ArrayAccessExpr(Expr array, Expr index){
        this.array = array;
        this.index = index;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitArrayAccessExpr(this);
    }
}
