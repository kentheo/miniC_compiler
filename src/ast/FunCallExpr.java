package ast;

import java.util.List;

/**
 * Created by TOSHIBA-PC on 12/10/2016.
 */
public class FunCallExpr extends Expr {

    public final String name;
    public final List<Expr>args;
    public FunDecl fd;              // to be filled in by the name analyser

    public FunCallExpr(String name, List<Expr>args){
        this.name = name;
        this.args = args;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitFunCallExpr(this);
    }
}
