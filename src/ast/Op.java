package ast;

/**
 * Created by TOSHIBA-PC on 12/10/2016.
 */

public enum Op implements ASTNode{
    ADD, SUB, MUL, DIV, MOD, GT, LT, GE, LE, NE, EQ, OR, AND;

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitOp(this);
    }
}
