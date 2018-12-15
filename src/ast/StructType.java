package ast;

import java.util.List;

/**
 * @author cdubach
 */
public class StructType implements Type {
    // to be completed//done
    public final String structName;
    public final List<VarDecl> varDecls;

    public StructType(String structName, List<VarDecl>varDecls){
        this.structName = structName;
        this.varDecls = varDecls;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStructType(this);
    }

}
