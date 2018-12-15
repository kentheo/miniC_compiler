package ast;

/**
 * Created by TOSHIBA-PC on 14/10/2016.
 */
public class If extends Stmt {

    public final Expr expr;
    public final Stmt stmt;
    public final Stmt opt_stmt;

    public If(Expr expr, Stmt stmt, Stmt opt_stmt){
        this.expr = expr;
        this.stmt = stmt;
        this.opt_stmt = opt_stmt;
//        if (opt_stmt == null){
//            this.opt_stmt = null;
//        } else {
//            this.opt_stmt = opt_stmt;
//        }
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitIf(this);
    }
}
