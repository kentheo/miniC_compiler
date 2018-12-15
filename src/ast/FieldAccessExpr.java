package ast;

/**
 * Created by TOSHIBA-PC on 12/10/2016.
 */
public class FieldAccessExpr extends Expr {

    public final Expr structure;
    public final String fieldName;

    public FieldAccessExpr(Expr structure, String fieldName){
        this.structure = structure;
        this.fieldName = fieldName;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitFieldAccessExpr(this);
    }
}
