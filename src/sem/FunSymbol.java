package sem;

import ast.FunCallExpr;
import ast.FunDecl;

/**
 * Created by s1317642 on 22/10/16.
 */
public class FunSymbol extends Symbol {

    FunDecl fd;

    public FunSymbol(FunDecl fd) {
        super(fd.name);
        this.fd = fd;
    }
}
