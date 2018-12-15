package lexer;

import lexer.Token.TokenClass;

import java.io.*;

/**
 * @author cdubach
 */
public class Tokeniser {

    private Scanner scanner;

    private int error = 0;
    public int getErrorCount() {
	return this.error;
    }

    public Tokeniser(Scanner scanner) {
        this.scanner = scanner;
    }

    private void error(char c, int line, int col) {
        System.out.println("Lexing error: unrecognised character ("+c+") at "+line+":"+col);
	error++;
    }


    public Token nextToken() {
        Token result;
        try {
             result = next();
        } catch (EOFException eof) {
            // end of file, nothing to worry about, just return EOF token
            return new Token(TokenClass.EOF, scanner.getLine(), scanner.getColumn());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            // something went horribly wro  ng, abort
            System.exit(-1);
            return null;
        }
        return result;
    }

    /*
     * To be completed
     */

    private Token next() throws IOException {

        int line = scanner.getLine();
        int column = scanner.getColumn();

        // get the next character
        char c = scanner.next();

        // skip white spaces
        if (Character.isWhitespace(c))
            return next();

        // recognise DIV, single and multi-line comments
        if (c == '/'){
            try {
                c = scanner.peek();
            } catch (EOFException eof) {
                return new Token(TokenClass.DIV, line, column);
            }
            if (c == '/'){
                scanner.next();
                try {
                    c = scanner.peek();
                } catch (EOFException eof) {
                    return new Token(TokenClass.EOF, scanner.getLine(), scanner.getColumn());
                }
                while (c != '\n') {
                    scanner.next();
                    try {
                        c = scanner.peek();
                    } catch (EOFException eof) {
                        return new Token(TokenClass.EOF, scanner.getLine(), scanner.getColumn());
                    }
                }
                return next();
            }
            if (c == '*'){
                scanner.next();
                try {
                    c = scanner.peek();
                } catch (EOFException eof) {
                    error(c, line, column);
                    return new Token(TokenClass.INVALID, line, column);
//                    return next();
                }
                while (c != '/'){
                    scanner.next();
                    try {
                        c = scanner.peek();
                    } catch (EOFException eof) {
                        return new Token(TokenClass.EOF, scanner.getLine(), scanner.getColumn());
                    }
                }
                scanner.next();
                return next();
//                    if (c == '*') {
//                        scanner.next();
//                        try {
//                            c = scanner.peek();
//                        } catch (EOFException eof) {
//                            error(c, line, column);
//                            return new Token(TokenClass.INVALID, line, column);
//                        }
//                        if (c == '/') {
//                            scanner.next();
//                            return next();
//                        }
//                    }
//                }
//                scanner.next();
//                try {
//                    c = scanner.peek();
//                } catch (EOFException eof) {
//                    error(c, line, column);
//                    return new Token(TokenClass.INVALID, line, column);
//                }
//                if (c == '/'){
//                    scanner.next();
//                    return next();
//                }
            }
            else {
                return new Token(TokenClass.DIV, line, column);
            }
        }

        // recognises the plus operator
        if (c == '+')
            return new Token(TokenClass.PLUS, line, column);

        // recognises operators
        // MINUS
        if (c == '-')
            return new Token(TokenClass.MINUS, line, column);
        // ASTERIX
        if (c == '*')
            return new Token(TokenClass.ASTERIX, line, column);
        // REM
        if (c == '%')
            return new Token(TokenClass.REM, line, column);

        // recognises delimeters
        // LBRA
        if (c == '{')
            return new Token(TokenClass.LBRA, line, column);
        //RBRA
        if (c == '}')
            return new Token(TokenClass.RBRA, line, column);
        //LPAR
        if (c == '(')
            return new Token(TokenClass.LPAR, line, column);
        //RPAR
        if (c == ')')
            return new Token(TokenClass.RPAR, line, column);
        //LSBR
        if (c == '[')
            return new Token(TokenClass.LSBR, line, column);
        //RSBR
        if (c == ']')
            return new Token(TokenClass.RSBR, line, column);
        //SC
        if (c == ';')
            return new Token(TokenClass.SC, line, column);
        //COMMA
        if (c == ',')
            return new Token(TokenClass.COMMA, line, column);

        // recognises struct member access
        //DOT
        if (c == '.')
            return new Token(TokenClass.DOT, line, column);

        // recognises logical operators
        //AND
        if (c == '&') {
            try {
                c = scanner.peek();
            } catch (EOFException eof) {
                error(c, line, column);
                return new Token(TokenClass.INVALID, line, column);
            }
            if (c == '&') {
                scanner.next();
                return new Token(TokenClass.AND, line, column);
            }
        }
        //OR
        if (c == '|') {
            try {
                c = scanner.peek();
            } catch (EOFException eof) {
                error(c, line, column);
                return new Token(TokenClass.INVALID, line, column);
            }
            if (c == '|') {
                scanner.next();
                return new Token(TokenClass.OR, line, column);
            }
        }

        //recognises comparisons
        //EQ
        if (c == '=') {
            try {
                c = scanner.peek();
            } catch (EOFException eof) {
                return new Token(TokenClass.ASSIGN, line, column);
            }
            if (c == '=') {
                scanner.next();
                return new Token(TokenClass.EQ, line, column);
            }
            // recognises assign
            else {
                return new Token(TokenClass.ASSIGN, line, column);
            }
        }
        //NE
        if (c == '!') {
            try {
                c = scanner.peek();
            } catch (EOFException eof) {
                error(c, line, column);
                return new Token(TokenClass.INVALID, line, column);
            }
            if (c == '=') {
                scanner.next();
                return new Token(TokenClass.NE, line, column);
            }
        }
        //LT and LE
        if (c == '<') {
            try {
                c = scanner.peek();
            } catch (EOFException eof) {
                return new Token(TokenClass.LT, line, column);
            }
            if (c == '=') {
                scanner.next();
                return new Token(TokenClass.LE, line, column);
            } else {
                return new Token(TokenClass.LT, line, column);
            }
        }
        //GT and GE
        if (c == '>') {
            try {
                c = scanner.peek();
            } catch (EOFException eof) {
                return new Token(TokenClass.GT, line, column);
            }
            if (c == '=') {
                scanner.next();
                return new Token(TokenClass.GE, line, column);
            } else {
                return new Token(TokenClass.GT, line, column);
            }
        }

        // recognises include
        if (c == '#'){
            StringBuilder str_include = new StringBuilder();
            str_include.append(c);
            try {
                c = scanner.peek();
            } catch (EOFException eof) {
                error(c, line, column);
                return new Token(TokenClass.INVALID, line, column);
            }
            while (!Character.isWhitespace(c)){
                str_include.append(c);
                scanner.next();
                try {
                    c = scanner.peek();
                } catch (EOFException eof) {
                    if (str_include.toString().equals("#include")) {
                        //scanner.next();
                        return new Token(TokenClass.INCLUDE, line, column);
                    } else {
                        error(c, line, column);
                        return new Token(TokenClass.INVALID, line, column);
                    }
                }
            }
            if (str_include.toString().equals("#include")) {
                scanner.next();
                return new Token(TokenClass.INCLUDE, line, column);
            } else {
                error(c, line, column);
                return new Token(TokenClass.INVALID, line, column);
            }

        }

        // recognise identifiers, types and keywords
        if (Character.isLetter(c) || c == '_') {
            StringBuilder str_ident = new StringBuilder();
            str_ident.append(c);
            try {
                c = scanner.peek();
            } catch (EOFException eof) {
                return new Token(TokenClass.IDENTIFIER, str_ident.toString(), line, column);
            }
            while (Character.isLetterOrDigit(c) || c == '_') {
                str_ident.append(c);
                scanner.next();
                try {
                    c = scanner.peek();
                } catch (EOFException eof) {
                    return new Token(TokenClass.IDENTIFIER, str_ident.toString(), line, column);
                }
            }
            // HERE DO THE CHECKS FOR TYPES AND KEYWORDS
            if (str_ident.toString().equals("int")){
                return new Token(TokenClass.INT, line, column);
            }
            if (str_ident.toString().equals("void")){
                return new Token(TokenClass.VOID, line, column);
            }
            if (str_ident.toString().equals("char")){
                return new Token(TokenClass.CHAR, line, column);
            }
            if (str_ident.toString().equals("if")){
                return new Token(TokenClass.IF, line, column);
            }
            if (str_ident.toString().equals("else")){
                return new Token(TokenClass.ELSE, line, column);
            }
            if (str_ident.toString().equals("while")){
                return new Token(TokenClass.WHILE, line, column);
            }
            if (str_ident.toString().equals("return")){
                return new Token(TokenClass.RETURN, line, column);
            }
            if (str_ident.toString().equals("struct")){
                return new Token(TokenClass.STRUCT, line, column);
            }
            if (str_ident.toString().equals("sizeof")){
                return new Token(TokenClass.SIZEOF, line, column);
            }
            return new Token(TokenClass.IDENTIFIER, str_ident.toString(), line, column);
        }
        /*
        /*

         */

        // recognises literals
        // STRING_LITERAL
        if (c == '"'){
            StringBuilder str_literal = new StringBuilder();
            try {
                c = scanner.peek();
            } catch (EOFException eof) {
                error(c, line, column);
                return new Token(TokenClass.INVALID, line, column);
            }
            while (c != '"'){
                str_literal.append(c);
                scanner.next();
                try {
                    c = scanner.peek();
                } catch (EOFException eof) {
                    // end of file so error
                    error(c, line, column);
                    return new Token(TokenClass.INVALID, line, column);
                }
                if (c == '\\') {
                    scanner.next();
                    try {
                        c = scanner.peek();
                    } catch (EOFException eof) {
                        error(c, line, column);
                        return new Token(TokenClass.INVALID, line, column);
                    }
                    switch (c) {
                        case 't':
                            str_literal.append("\t");
                            break;
                        case 'b':
                            str_literal.append("\b");
                            break;
                        case 'n':
                            str_literal.append("\\n");
                            break;
                        case 'r':
                            str_literal.append("\r");
                            break;
                        case 'f':
                            str_literal.append("\f");
                            break;
                        case '\'':
                            str_literal.append("\'");
                            break;
                        case '\"':
                            str_literal.append("\"");
                            break;
                        case '\\':
                            str_literal.append("\\");
                            break;
                        default:
                            error(c, line, column);
                            return new Token(TokenClass.INVALID, line, column);
                    }
                    scanner.next();
                    try {
                        c = scanner.peek();
                    } catch (EOFException eof) {
                        // end of file so error
                        error(c, line, column);
                        return new Token(TokenClass.INVALID, line, column);
                    }
                }
            }
            if (c == '"'){
                scanner.next();
                return new Token(TokenClass.STRING_LITERAL, str_literal.toString(), line, column);
            }
        }
        // INT_LITERAL
        if (Character.isDigit(c)){
            StringBuilder str_int = new StringBuilder();
            str_int.append(c);
            try {
                c = scanner.peek();
            } catch (EOFException eof) {
                // end of file
                return new Token(TokenClass.INT_LITERAL, str_int.toString(), line, column);
            }
            while (Character.isDigit(c)){
                str_int.append(c);
                scanner.next();
                try {
                    c = scanner.peek();
                } catch (EOFException eof) {
                    return new Token(TokenClass.INT_LITERAL, str_int.toString(), line, column);
                }
            }
            return new Token(TokenClass.INT_LITERAL, str_int.toString(), line, column);
        }
        // CHAR_LITERAL
        if (c == '\'') {
            StringBuilder str_char = new StringBuilder();
            try {
                c = scanner.peek();
            } catch (EOFException eof) {
                error(c, line, column);
                return new Token(TokenClass.INVALID, line, column);
            }
            if (c == '\'') {
                scanner.next();
                return new Token(TokenClass.CHAR_LITERAL, str_char.toString(), line, column);
            }
            if (c == '\\') {
                scanner.next();
                try {
                    c = scanner.peek();
                } catch (EOFException eof) {
                    error(c, line, column);
                    return new Token(TokenClass.INVALID, line, column);
                }
                switch (c) {
                    case 't':
                        str_char.append("\t");
                        break;
                    case 'b':
                        str_char.append("\b");
                        break;
                    case 'n':
                        str_char.append("\n");
                        break;
                    case 'r':
                        str_char.append("\r");
                        break;
                    case 'f':
                        str_char.append("\f");
                        break;
                    case '\'':
                        str_char.append("\'");
                        break;
                    case '\"':
                        str_char.append("\"");
                        break;
                    case '\\':
                        str_char.append("\\");
                        break;
                    default:
                        error(c, line, column);
                        return new Token(TokenClass.INVALID, line, column);
                }
                scanner.next();
                try {
                    c = scanner.peek();
                } catch (EOFException eof) {
                    error(c, line, column);
                    return new Token(TokenClass.INVALID, line, column);
                }
                if (c == '\''){
                    scanner.next();
                    return new Token(TokenClass.CHAR_LITERAL, str_char.toString(), line, column);
                }
            }
            else {
                str_char.append(c);
                scanner.next();
                try {
                    c = scanner.peek();
                } catch (EOFException eof) {
                    error(c, line, column);
                    return new Token(TokenClass.INVALID, line, column);
                }
                if (c == '\''){
                    scanner.next();
                    return new Token(TokenClass.CHAR_LITERAL, str_char.toString(), line, column);
                }
            }

        }


        // if we reach this point, it means we did not recognise a valid token
        error(c, line, column);
        return new Token(TokenClass.INVALID, line, column);
    }

}
