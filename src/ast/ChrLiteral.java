package ast;

/**
 * Created by TOSHIBA-PC on 12/10/2016.
 */
public class ChrLiteral extends Expr {

    public final char character;

    public ChrLiteral(char character){
        this.character = character;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitChrLiteral(this);
    }
}
