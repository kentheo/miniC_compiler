package ast;

/**
 * Created by TOSHIBA-PC on 12/10/2016.
 */
public class ArrayType implements Type {

    public final Type type;
    public final int numElements;

    public ArrayType(Type type, int numElements){
        this.type = type;
        this.numElements = numElements;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitArrayType(this);
    }
}
