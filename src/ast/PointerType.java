package ast;

/**
 * Created by TOSHIBA-PC on 12/10/2016.
 */
public class PointerType implements Type{

    public final Type type;

    public PointerType(Type type) {
        this.type = type;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitPointerType(this);
    }

}
