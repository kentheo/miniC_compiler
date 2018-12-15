package sem;

import ast.VarDecl;

/**
 * Created by ltopuser on 21/10/2016.
 */
public class VarSymbol extends Symbol {

    VarDecl vd;

    public VarSymbol(VarDecl vd) {
        super(vd.varName);
        this.vd = vd;
    }
}
