package parser;

import ast.*;
import lexer.Token;
import lexer.Tokeniser;
import lexer.Token.TokenClass;

import java.util.*;

import static lexer.Token.TokenClass.*;


/**
 * @author cdubach
 */
public class Parser {

    private Token token;

    // use for backtracking (useful for distinguishing decls from procs when parsing a program for instance)
    private Queue<Token> buffer = new LinkedList<Token>();

    private final Tokeniser tokeniser;



    public Parser(Tokeniser tokeniser) {
        this.tokeniser = tokeniser;
    }

    public Program parse() {
        // get the first token
        nextToken();

        return parseProgram();
    }

    public int getErrorCount() {
        return error;
    }

    private int error = 0;
    private Token lastErrorToken;

    private void error(TokenClass... expected) {

        if (lastErrorToken == token) {
            // skip this error, same token causing trouble
            return;
        }

        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (TokenClass e : expected) {
            sb.append(sep);
            sb.append(e);
            sep = "|";
        }
        System.out.println("Parsing error: expected ("+sb+") found ("+token+") at "+token.position);

        error++;
        lastErrorToken = token;
    }

    /*
     * Look ahead the i^th element from the stream of token.
     * i should be >= 1
     */
    private Token lookAhead(int i) {
        // ensures the buffer has the element we want to look ahead
        while (buffer.size() < i)
            buffer.add(tokeniser.nextToken());
        assert buffer.size() >= i;

        int cnt=1;
        for (Token t : buffer) {
            if (cnt == i)
                return t;
            cnt++;
        }

        assert false; // should never reach this
        return null;
    }


    /*
     * Consumes the next token from the tokeniser or the buffer if not empty.
     */
    private void nextToken() {
        if (!buffer.isEmpty())
            token = buffer.remove();
        else
            token = tokeniser.nextToken();
    }

    /*
     * If the current token is equals to the expected one, then skip it, otherwise report an error.
     * Returns the expected token or null if an error occurred.
     */
    private Token expect(TokenClass... expected) {
        for (TokenClass e : expected) {
            if (e == token.tokenClass) {
                Token cur = token;
                nextToken();
                return cur;
            }
        }

        error(expected);
        assert false;
        return null;
    }

    /*
    * Returns true if the current token is equals to any of the expected ones.
    */
    private boolean accept(TokenClass... expected) {
        boolean result = false;
        for (TokenClass e : expected)
            result |= (e == token.tokenClass);
        return result;
    }


    private Program parseProgram() {
        parseIncludes();
        List<StructType> sts = parseStructDecls();
        List<VarDecl> vds = parseVarDecls();
        List<FunDecl> fds = parseFunDecls();

        expect(TokenClass.EOF);
        return new Program(sts, vds, fds);
    }

    // includes are ignored, so does not need to return an AST node
    private void parseIncludes() {
	    if (accept(TokenClass.INCLUDE)) {               // #include STRING_LITERAL include | ε
            nextToken();
            expect(TokenClass.STRING_LITERAL);
            parseIncludes();
        }

    }

    private List<StructType> parseStructDecls() {
        List<StructType> sts = new LinkedList<StructType>();
        while (accept(TokenClass.STRUCT) && lookAhead(2).tokenClass == TokenClass.LBRA){
            StructType st = parseStructTypes();
            sts.add(st);
        }
        return sts;
    }

    private StructType parseStructTypes() {
        expect(TokenClass.STRUCT);
        String stName = "";
        if (accept(TokenClass.IDENTIFIER)){
            stName = token.data;
            expect(TokenClass.IDENTIFIER);
        }
        if (accept(TokenClass.LBRA)){
            expect(TokenClass.LBRA);
            List<VarDecl> vds = parseVarDeclsRep();
            expect(TokenClass.RBRA);
            expect(TokenClass.SC);
            return new StructType(stName, vds);
        } else {
            return new StructType(stName, new LinkedList<VarDecl>());
        }
    }

    private List<VarDecl> parseVarDecls() {
        List<VarDecl> vds = new LinkedList<VarDecl>();
        while (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT) && (lookAhead(2).tokenClass == TokenClass.SC || lookAhead(3).tokenClass == TokenClass.SC || lookAhead(4).tokenClass == TokenClass.SC || lookAhead(2).tokenClass == TokenClass.LSBR || lookAhead(3).tokenClass == TokenClass.LSBR || lookAhead(4).tokenClass == TokenClass.LSBR)){
            Type t = parseTypes();
            String varName = "";
            if (accept(TokenClass.IDENTIFIER)){
                varName = token.data;
                expect(TokenClass.IDENTIFIER);
                if (accept(TokenClass.LSBR)){
                    ArrayType at = parseArrayType(t);
                    t = at;
                    expect(TokenClass.SC);
                } else {
                    expect(TokenClass.SC);
                }
            } else {
                error(TokenClass.IDENTIFIER);
            }

            VarDecl vd = new VarDecl(t,varName);
            vds.add(vd);
        }
        return vds ;

    }

    private IntLiteral parseNumber() {
        String s = "";
        if (accept(TokenClass.INT_LITERAL)) {
            s = token.data;
            int i = Integer.parseInt(s);
            expect(INT_LITERAL);
            return new IntLiteral(i);
        } else {
            expect(TokenClass.INT_LITERAL);
            return null;
        }
    }

    private List<VarDecl> parseVarDeclsRep() {
        List<VarDecl> vds = new LinkedList<VarDecl>();
        Type t = parseTypes();
        String varName = "";
        if (accept(TokenClass.IDENTIFIER)){
            varName = token.data;
            expect(TokenClass.IDENTIFIER);
            // Possible ArrayType
            if (accept(TokenClass.LSBR)){
                ArrayType at = parseArrayType(t);
                t = at;
                expect(TokenClass.SC);
            } else {
                expect(TokenClass.SC);
            }
        } else {
            error(TokenClass.IDENTIFIER);
        }
        VarDecl vd = new VarDecl(t,varName);
        vds.add(vd);
        while (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT) && (lookAhead(2).tokenClass == TokenClass.SC || lookAhead(3).tokenClass == TokenClass.SC || lookAhead(4).tokenClass == TokenClass.SC || lookAhead(2).tokenClass == TokenClass.LSBR || lookAhead(3).tokenClass == TokenClass.LSBR || lookAhead(4).tokenClass == TokenClass.LSBR)){
            Type t1 = parseTypes();
            String varName1 = "";
            if (accept(TokenClass.IDENTIFIER)){
                varName1 = token.data;
                expect(TokenClass.IDENTIFIER);
                if (accept(TokenClass.LSBR)){
                    ArrayType at = parseArrayType(t);
                    t = at;
                    expect(TokenClass.SC);
                } else {
                    expect(TokenClass.SC);
                }
            } else {
                error(TokenClass.IDENTIFIER);
            }
            VarDecl vd1 = new VarDecl(t1,varName1);
            vds.add(vd1);
        }
        return vds ;

    }

    private List<FunDecl> parseFunDecls() {
        List<FunDecl> fds = new LinkedList<FunDecl>();
        while (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT) && (lookAhead(2).tokenClass == TokenClass.LPAR || lookAhead(3).tokenClass == TokenClass.LPAR) || lookAhead(4).tokenClass == TokenClass.LPAR){
            Type t = parseTypes();
            String funName = "";
            if (accept(TokenClass.IDENTIFIER)){
                funName = token.data;
                expect(TokenClass.IDENTIFIER);
                expect(TokenClass.LPAR);
                List<VarDecl> vds = parseParams();
                expect(TokenClass.RPAR);
                Block b = parseBlocks();
                FunDecl fd = new FunDecl(t, funName, vds, b);
                fds.add(fd);
            } else {
                error(TokenClass.IDENTIFIER);
            }

        }
        return fds;
    }

    private Type parseTypes() {
        BaseType bt;
        if (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID)){
            if (accept(TokenClass.INT)) {
                bt = BaseType.INT;
                expect(TokenClass.INT);
            } else if (accept(TokenClass.CHAR)) {
                expect(TokenClass.CHAR);
                bt = BaseType.CHAR;
            } else {
                expect(TokenClass.VOID);
                bt = BaseType.VOID;
            }
            //////////////////////////////////////////
            if (accept(TokenClass.ASTERIX)){
                return parsePointerType(bt);
            } else {
                return bt;
            }
        } else {
            StructType st = parseStructTypes();
            if (accept(TokenClass.ASTERIX)){
                PointerType pt = parsePointerType(st);
                return pt;
            } else {
                return st;
            }
        }
    }

    private PointerType parsePointerType(Type t){
        PointerType pt = new PointerType(t);
        expect(TokenClass.ASTERIX);
        return pt;
    }

    private ArrayType parseArrayType(Type t){
        expect(TokenClass.LSBR);
        String n = "";
        if (accept(TokenClass.INT_LITERAL)){
            n = token.data;
            int i = Integer.parseInt(n);
            expect(TokenClass.INT_LITERAL);
            expect(TokenClass.RSBR);
            return new ArrayType(t, i);
        } else {
            error(TokenClass.INT_LITERAL);
            return null;
        }
    }

    private List<VarDecl> parseParams(){
        List<VarDecl> vds = new LinkedList<VarDecl>();
        if (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT)){
            Type t = parseTypes();
            String varName = "";
            if (accept(TokenClass.IDENTIFIER)){
                varName = token.data;
                expect(TokenClass.IDENTIFIER);
            } else {
                error(TokenClass.IDENTIFIER);
            }
            VarDecl vd = new VarDecl(t, varName);
            vds.add(vd);
            while (accept(TokenClass.COMMA)){
                expect(TokenClass.COMMA);
                t = parseTypes();
                if (accept(TokenClass.IDENTIFIER)){
                    varName = token.data;
                    expect(TokenClass.IDENTIFIER);
                } else {
                    error(TokenClass.IDENTIFIER);
                }
                vd = new VarDecl(t, varName);
                vds.add(vd);
            }
        }
        return vds;

    }

    private Block parseBlocks(){
        expect(TokenClass.LBRA);
        List<VarDecl> vds = parseVarDecls();
        List<Stmt> sts = parseStmtsRep();
        expect(TokenClass.RBRA);
        return new Block(vds, sts);

    }

    private List<Stmt> parseStmtsRep(){
        List<Stmt> sts = new LinkedList<Stmt>();
        while (accept(WHILE, LBRA, IF, RETURN, TokenClass.LPAR, TokenClass.LSBR, TokenClass.MINUS, TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL, TokenClass.ASTERIX, TokenClass.IDENTIFIER, TokenClass.SIZEOF)) {
            Stmt stmt = parseStmts();
            sts.add(stmt);
        }
        return sts;

    }

    private Stmt parseStmts(){
        if (accept(TokenClass.LBRA)){
            Block b = parseBlocks();
            return b;
        }
        else if (accept(TokenClass.WHILE)){
            While w = parseWhile();
            return w;
        }
        else if (accept(TokenClass.IF)){
            If i = parseIf();
            return i;
        }
        else if (accept(TokenClass.RETURN)){
            Return r = parseReturn();
            return r;
        }

        else if (accept(TokenClass.LPAR, TokenClass.LSBR, TokenClass.MINUS, TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL, TokenClass.ASTERIX, TokenClass.IDENTIFIER, TokenClass.SIZEOF)){
            Expr e = parseExps();
            if (accept(TokenClass.SC)){
                expect(TokenClass.SC);
                return new ExprStmt(e);
            } else {
                Assign a = parseAssign(e);
                return a;
            }
        }
        else {
            error(TokenClass.LBRA);
        }
        return null;
    }

    private While parseWhile(){
        expect(TokenClass.WHILE);
        expect(TokenClass.LPAR);
        Expr e = parseExps();
        expect(TokenClass.RPAR);
        Stmt stmt = parseStmts();
        return new While(e, stmt);
    }

    private If parseIf(){
        expect(TokenClass.IF);
        expect(TokenClass.LPAR);
        Expr e = parseExps();
        if (e == null) {
            error(TokenClass.IDENTIFIER);
        }
        expect(TokenClass.RPAR);
        Stmt stmt = parseStmts();
        if (accept(TokenClass.ELSE)){
            expect(TokenClass.ELSE);
            Stmt opt_stmt = parseStmts();
            return new If(e, stmt, opt_stmt);
        }
        return new If(e,stmt, null);
    }

    private Return parseReturn(){
        expect(TokenClass.RETURN);
        ////////////////////////////////// OPTIONAL EXP
        if (accept(TokenClass.LPAR, TokenClass.LSBR, TokenClass.MINUS, TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL, TokenClass.ASTERIX, TokenClass.IDENTIFIER, TokenClass.SIZEOF)){
            Expr e = parseExps();
            expect(TokenClass.SC);
            return new Return(e);
        }
        expect(TokenClass.SC);
        return new Return(null);
    }

    private Op convertOp(Token t){
        Op op;
        if (t.tokenClass == ASTERIX){
            op = Op.MUL;
        } else if (t.tokenClass == DIV){
            op = Op.DIV;
        } else if (t.tokenClass == REM){
            op = Op.MOD;
        } else if (t.tokenClass == PLUS){
            op = Op.ADD;
        } else if (t.tokenClass == MINUS){
            op = Op.SUB;
        } else if (t.tokenClass == GT){
            op = Op.GT;
        } else if (t.tokenClass == GE){
            op = Op.GE;
        } else if (t.tokenClass == LT){
            op = Op.LT;
        } else if (t.tokenClass == LE){
            op = Op.LE;
        } else if (t.tokenClass == EQ){
            op = Op.EQ;
        } else if (t.tokenClass == NE){
            op = Op.NE;
        } else if (t.tokenClass == AND){
            op = Op.AND;
        } else {
            op = Op.OR;
        }
        return op;
    }
    private Expr parseExps(){
        Expr e = parseExps1();
        return e;
    }

    private Expr parseExps1(){
        Expr lhs = parseExps2();
        while (accept(OR)) {
            Token operator = token;
            nextToken();
            Expr rhs = parseExps2();
            Expr e = new BinOp(lhs, convertOp(operator), rhs); // where conv is some conversion function : Token -> Op
            lhs = e;
        }
        return lhs;

    }

    private Expr parseExps2(){
        Expr lhs = parseExps3();
        while (accept(AND)) {
            Token operator = token;
            nextToken();
            Expr rhs = parseExps3();
            Expr e = new BinOp(lhs, convertOp(operator), rhs); // where conv is some conversion function : Token -> Op
            lhs = e;
        }
        return lhs;
    }

    private Expr parseExps3(){
        Expr lhs = parseExps4();
        while (accept(EQ, NE)) {
            Token operator = token;
            nextToken();
            Expr rhs = parseExps4();
            Expr e = new BinOp(lhs, convertOp(operator), rhs); // where conv is some conversion function : Token -> Op
            lhs = e;
        }
       return lhs;
    }



    private Expr parseExps4(){
        Expr lhs = parseExps5();
        while (accept(GT, GE, LT, LE)) {
            Token operator = token;
            nextToken();
            Expr rhs = parseExps5();
            Expr e = new BinOp(lhs, convertOp(operator), rhs); // where conv is some conversion function : Token -> Op
            lhs = e;
        }
        return lhs;
    }

    private Expr parseExps5(){
        Expr lhs = parseExps6();
        while (accept(PLUS, MINUS)) {
            Token operator = token;
            nextToken();
            Expr rhs = parseExps6();
            Expr e = new BinOp(lhs, convertOp(operator), rhs); // where conv is some conversion function : Token -> Op
            lhs = e;
        }
        return lhs;
    }

    private Expr parseExps6(){
        Expr lhs = parseExps7();
        while (accept(ASTERIX, DIV, REM)) {
            Token operator = token;
            nextToken();
            Expr rhs = parseExps7();
            Expr e = new BinOp(lhs, convertOp(operator), rhs); // where conv is some conversion function : Token -> Op
            lhs = e;
        }
        return lhs;

    }

    // precedence for valueat
    private Expr parseExps7(){
        if (accept(TokenClass.ASTERIX)){
            ValueAtExpr vae = parseValueAt();
            Expr e = vae;
            return e;
        } else {
            Expr e1 = parseExps8();
            return e1;
        }
    }

    private Expr parseExps8(){
        if (accept(TokenClass.SIZEOF)){
            SizeOfExpr soe = parseSizeOf();
            return soe;
        } else {
            return parseExps9();
        }
    }
    // precedence for typecast
    private Expr parseExps9(){
        if (accept(TokenClass.LPAR)){
            if (lookAhead(1).tokenClass == TokenClass.INT || lookAhead(1).tokenClass == TokenClass.CHAR || lookAhead(1).tokenClass == TokenClass.VOID){
                TypecastExpr te = parseTypeCast();
                return te;
            } else {
                return parseExps10();
            }
        } else {
            return parseExps10();
        }

    }
    // precedence of fieldaccess
    private Expr parseExps10() {
        Expr e = parseExps11();
        if (accept(TokenClass.DOT)) {
            FieldAccessExpr fae = parseFieldAccess(e);
            return fae;
        } else {
            return e;
        }

    }
    //precedence for arrayaccess
    private Expr parseExps11(){
        Expr e = parseExps12();
        if (accept(TokenClass.LSBR)) {
            ArrayAccessExpr aae = parseArrayAccessExpr(e);
            return aae;
//            if (accept(TokenClass.LSBR)) {
//                ArrayAccessExpr aae = parseArrayAccessExpr();
//            }
        } else {
            return e;
        }
    }
    // precedence for funcall
    private Expr parseExps12(){
        if (accept(TokenClass.IDENTIFIER)){
            if (lookAhead(1).tokenClass == TokenClass.LPAR) {
                FunCallExpr fce = parseFuncallExpr();
                return fce;
            } else {
                Expr e = parseExps13();
                return e;
            }
        } else {
            Expr e = parseExps13();
            return e;
        }
    }

    private Expr parseExps13(){
        if (accept(TokenClass.LPAR)){
            expect(TokenClass.LPAR);
            Expr e = parseExps();
            if (e == null) {
                error(TokenClass.IDENTIFIER);
            }
            expect(TokenClass.RPAR);
            return e;
        } else {
            if (accept(TokenClass.MINUS)){
                expect(TokenClass.MINUS);
                Op op = Op.SUB;
                if (accept(TokenClass.INT_LITERAL)){
                    IntLiteral i = parseNumber();
                    IntLiteral zero = new IntLiteral(0);
                    return new BinOp(zero, op, i);
                }
                String id = "";
                if (accept(TokenClass.IDENTIFIER)){
                    id = token.data;
                    expect(TokenClass.IDENTIFIER);
                    IntLiteral zero = new IntLiteral(0);
                    VarExpr ve = new VarExpr(id);
                    return new BinOp(zero, op, ve);
                }
                error(TokenClass.IDENTIFIER,TokenClass.INT_LITERAL);
            } else {
                if (accept(TokenClass.INT_LITERAL)){
                    IntLiteral i = parseNumber();
                    return i;
                }
                if (accept(TokenClass.CHAR_LITERAL)){
                    ChrLiteral cl = parseChar();
                    return cl;
                }
                if (accept(TokenClass.STRING_LITERAL)){
                    StrLiteral sl = parseString();
                    return sl;
                }
                if (accept(TokenClass.IDENTIFIER)){
                    VarExpr ve = parseVarExpr();
                    return ve;
                }
                error(TokenClass.IDENTIFIER,TokenClass.STRING_LITERAL, TokenClass.CHAR_LITERAL);
            }
        }
        return null;

    }
//////////////////////////////////////////////////////////////////////////////////////

    private VarExpr parseVarExpr(){
        String varName = "";
        if (accept(TokenClass.IDENTIFIER)){
            varName = token.data;
            expect(TokenClass.IDENTIFIER);
            return new VarExpr(varName);
        } else {
            error(TokenClass.IDENTIFIER);
            return null;
        }
            }

    private StrLiteral parseString(){
        String s = "";
        if (accept(TokenClass.STRING_LITERAL)) {
            s = token.data;
            expect(TokenClass.STRING_LITERAL);
            return new StrLiteral(s);
        } else {
            error(TokenClass.STRING_LITERAL);
            return null;
        }


    }

    private ChrLiteral parseChar(){
        String n = "";
        if (accept(TokenClass.CHAR_LITERAL)){
            n = token.data;
            char[] c = n.toCharArray();
            char ch = c[0];
            expect(TokenClass.CHAR_LITERAL);
            return new ChrLiteral(ch);
        } else {
            error(TokenClass.CHAR_LITERAL);
            return null;
        }
    }

    private Assign parseAssign(Expr lhs){
        expect(TokenClass.ASSIGN);
        Expr rhs = parseExps();
        expect(TokenClass.SC);
        return new Assign(lhs, rhs);
    }

    private FunCallExpr parseFuncallExpr(){
        List<Expr> exprList = new LinkedList<>();
        String fName = "";
        fName = token.data;
        expect(TokenClass.IDENTIFIER);
        expect(TokenClass.LPAR);
        if (accept(TokenClass.LPAR, TokenClass.LSBR, TokenClass.MINUS, TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL, TokenClass.ASTERIX, TokenClass.IDENTIFIER, TokenClass.SIZEOF)){
            Expr e = parseExps();
            exprList.add(e);
            while (accept(TokenClass.COMMA)){
                expect(TokenClass.COMMA);
                Expr e1 = parseExps();
                exprList.add(e1);
            }
        }
        expect(TokenClass.RPAR);


        return new FunCallExpr(fName, exprList);
    }

    private ArrayAccessExpr parseArrayAccessExpr(Expr lhs){
        //parseExps();
        expect(TokenClass.LSBR);
        Expr rhs = parseExps();
        expect(TokenClass.RSBR);
        while (accept(TokenClass.LSBR)){
            expect(TokenClass.LSBR);
            lhs = parseExps();                                       ///// CHECK THIS LATER 2-D Arrays
            expect(TokenClass.RSBR);
        }
        return new ArrayAccessExpr(lhs, rhs);

    }

    private FieldAccessExpr parseFieldAccess(Expr e){
        expect(TokenClass.DOT);
        String name = "";
        if (accept(TokenClass.IDENTIFIER)){
            name = token.data;
            expect(TokenClass.IDENTIFIER);
        } else {
            error(TokenClass.IDENTIFIER);
            return null;
        }
        return new FieldAccessExpr(e, name);

    }

    private ValueAtExpr parseValueAt(){
        expect(TokenClass.ASTERIX);
        Expr e = parseExps();
        return new ValueAtExpr(e);
    }

    private SizeOfExpr parseSizeOf(){
        expect(TokenClass.SIZEOF);
        expect(TokenClass.LPAR);
        Type t = parseTypes();
        expect(TokenClass.RPAR);
        return new SizeOfExpr(t);

    }

    private TypecastExpr parseTypeCast(){
        expect(TokenClass.LPAR);
        Type t = parseTypes();
        expect(TokenClass.RPAR);
        Expr e = parseExps();
        return new TypecastExpr(t, e);

    }
}
