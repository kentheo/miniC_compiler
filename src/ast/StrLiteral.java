package ast;

/**
 * Created by TOSHIBA-PC on 12/10/2016.
 */
public class StrLiteral extends Expr {

    public final String string;

    public StrLiteral(String string){
        this.string = string;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStrLiteral(this);
    }
}
