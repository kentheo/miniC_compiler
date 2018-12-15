package sem;

import ast.*;

import java.util.*;


public class NameAnalysisVisitor extends BaseSemanticVisitor<Void> {

	private Scope scope;

	@Override
	public Void visitBaseType(BaseType bt) {
		// To be completed...
		return null;
	}

	@Override
	public Void visitStructType(StructType st) {
		// To be completed...
		if (st.varDecls != null){
			Scope oldScope = scope;
			scope = new Scope(oldScope);
			for (VarDecl vd : st.varDecls){
				vd.accept(this);
			}
			scope = oldScope;
		}
		return null;
	}

	@Override
	public Void visitBlock(Block b) {
		// To be completed...
		// visit children
		for (VarDecl vd : b.varDecls) {
			Symbol x = scope.lookupCurrent(vd.varName);
			if (x != null){
				error("Variable Declaration already exists! block");
			}
//			else if (parameters.containsKey(x.name)){
//				error("Variable cannot be declared the same name as one of the parameters!");
//			}
			else {
				vd.accept(this);
			}
		}
		for (Stmt s : b.stmts) {
			if (s instanceof Block){
				Scope oldScope = scope;
				scope = new Scope(oldScope);
				s.accept(this);
				scope = oldScope;
			} else {
				s.accept(this);
			}
		}
		return null;
	}

	@Override
	public Void visitFunDecl(FunDecl fd) {
		// To be completed...
		Symbol s = scope.lookupCurrent(fd.name);
		if (s != null){
			error("Function Declaration already exists!");
		}
		else {
			scope.put(new FunSymbol(fd));
		}
		Scope oldScope = scope;
		scope = new Scope(oldScope);
		for (VarDecl vd : fd.params){
			vd.accept(this);
			//vd.type.accept(this);
		}
		if (fd.block != null) {
            fd.block.accept(this);
        } else {
            error("block empty");
        }
		scope = oldScope;
		return null;
	}

	@Override
	public Void visitProgram(Program p) {
		// To be completed...//done
		scope = new Scope();
		List<VarDecl> a = new LinkedList<VarDecl>(){{
			add(new VarDecl(new PointerType(BaseType.CHAR),"s"));}};
		List<VarDecl> b = new LinkedList<VarDecl>(){{
			add(new VarDecl(BaseType.CHAR,"c"));}};
		List<VarDecl> c = new LinkedList<VarDecl>(){{
			add(new VarDecl(BaseType.INT,"i"));}};
		List<VarDecl> d = new LinkedList<VarDecl>();
		List<VarDecl> e = new LinkedList<VarDecl>();
		List<VarDecl> f = new LinkedList<VarDecl>(){{
			add(new VarDecl(BaseType.INT,"s"));}};
		scope.put(new FunSymbol(new FunDecl(BaseType.VOID, "print_s",a , new Block(null,null) )));
		scope.put(new FunSymbol(new FunDecl(BaseType.VOID, "print_c", b, new Block(null, null))));
		scope.put(new FunSymbol(new FunDecl(BaseType.VOID, "print_i", c, new Block(null, null))));
		scope.put(new FunSymbol(new FunDecl(BaseType.CHAR, "read_c", d, new Block(null, null))));
		scope.put(new FunSymbol(new FunDecl(BaseType.INT, "read_i", e, new Block(null, null))));
		scope.put(new FunSymbol(new FunDecl(new PointerType(BaseType.VOID), "mcmalloc", f, new Block(null, null))));

		for (StructType st : p.structTypes){
			st.accept(this);
		}
		for (VarDecl vd : p.varDecls) {
			vd.accept(this);
		}
		for (FunDecl fd : p.funDecls) {
			fd.accept(this);
		}
		return null;
	}

	@Override
	public Void visitVarDecl(VarDecl vd) {
		// To be completed...
		Symbol s = scope.lookupCurrent(vd.varName);
		if (s != null){
			error("Variable Declaration already exists!");
		} else {
			scope.put(new VarSymbol(vd));
			//System.out.println(vd.type);
		}
		return null;
	}

	@Override
	public Void visitVarExpr(VarExpr v) {
		// To be completed...
		Symbol vs = scope.lookup(v.name);
		//System.out.println("Variable " + v.name);
		if (vs == null){
			error("Variable does not exist! Name Analysis");
		} else if (!vs.isVar(vs)){
			error("This is not a variable!");
		} else {
			//System.out.println("VAr symbol " + ((VarSymbol) vs).vd);
			v.vd = ((VarSymbol) vs).vd;
		}
		return null;
	}

	@Override
	public Void visitFunCallExpr(FunCallExpr fce) {
		Symbol fs = scope.lookup(fce.name);

		if (fs == null){
			error("Function does not exist!");
		} else if (!fs.isFun(fs)){
			error("This is not a function!");
		} else {
			// args need to be accepted here
			for (Expr e : fce.args){
				e.accept(this);
			}
			fce.fd = ((FunSymbol) fs).fd;
		}
		return null;
	}

	// To be completed...

	@Override
	public Void visitPointerType(PointerType pt) {
		return null;
	}

	@Override
	public Void visitArrayType(ArrayType at) {
		return null;
	}

	@Override
	public Void visitIntLiteral(IntLiteral il) {
		return null;
	}

	@Override
	public Void visitStrLiteral(StrLiteral sl) {
		return null;
	}

	@Override
	public Void visitChrLiteral(ChrLiteral cl) {
		return null;
	}

	@Override
	public Void visitBinOp(BinOp bo) {
		if (bo.lhs != null && bo.rhs != null){
			bo.lhs.accept(this);
			bo.rhs.accept(this);
		} else {
			error("empty expressions!");
		}
		return null;
	}

	@Override
	public Void visitOp(Op op) {
		return null;
	}

	@Override
	public Void visitArrayAccessExpr(ArrayAccessExpr aae) {
		if (aae.array != null && aae.index != null){
			aae.array.accept(this);
			aae.index.accept(this);
		} else {
			error("Something doesn't exist in the ArrayAccessExpr!");
		}

//		System.out.println(aae.array.accept(this) + "  Name Analysis");
		return null;
	}

	@Override
	public Void visitFieldAccessExpr(FieldAccessExpr fa) {
		if (fa.structure != null) {
			fa.structure.accept(this);
		} else {
			error("Field access structure doesn't exist!");
		}
		//fa.type.accept(this);
		return null;
	}

	@Override
	public Void visitValueAtExpr(ValueAtExpr vae) {
		if (vae.expr != null) {
			vae.expr.accept(this);
		} else {
			error("Value at expr doesn't exist!");
		}
		return null;
	}

	@Override
	public Void visitSizeOfExpr(SizeOfExpr soe) {

		return null;
	}

	@Override
	public Void visitTypecastExpr(TypecastExpr tce) {
		if (tce.expr != null) {
			tce.expr.accept(this);
		} else {
			error("Type cast expression doesn't exist!");
		}
		return null;
	}

	@Override
	public Void visitWhile(While w) {
		if (w.expr != null) {
			w.expr.accept(this);
		} else {
			error("Empty while expr!");
		}
		// Statement so Block so new Scope
		if (w.stmt instanceof Block){
			Scope oldScope = scope;
			scope = new Scope(oldScope);
			w.stmt.accept(this);
			scope = oldScope;
		} else if (w.stmt != null) {
			w.stmt.accept(this);
		} else {
			error("While statement empty!");
		}
		return null;
	}

	@Override
	public Void visitIf(If i) {
		if (i.expr != null) {
			i.expr.accept(this);
		} else {
			error("If expression doesn't exist!");
		}
		// Statement so Block so new Scope
		if (i.stmt instanceof Block){
			Scope oldScope = scope;
			scope = new Scope(oldScope);
			i.stmt.accept(this);
			scope = oldScope;
		} else if (i.stmt != null){
			i.stmt.accept(this);
		} else {
			error("If statement empty!");
		}
		if (i.opt_stmt instanceof Block){
			Scope opt_oldScope = scope;
			scope = new Scope(opt_oldScope);
			i.opt_stmt.accept(this);
			scope = opt_oldScope;
		} else if (i.opt_stmt != null){
			i.opt_stmt.accept(this);
		}
		return null;
	}

	@Override
	public Void visitAssign(Assign a) {
		if (a.lhs != null && a.rhs != null){
			a.lhs.accept(this);
			a.rhs.accept(this);
		} else {
			error("empty assign");
		}

		return null;
	}

	@Override
	public Void visitReturn(Return r) {
		//if (r.expr instanceof VarExpr || r.expr instanceof FunCallExpr){
		if (r.expr != null){
			r.expr.accept(this);
		}
		return null;
	}

	@Override
	public Void visitExprStmt(ExprStmt es) {
		//if (es.expr instanceof VarExpr || es.expr instanceof FunCallExpr)
		if (es.expr != null) {
			es.expr.accept(this);
		} else {
			error("empty exprstmt");
		}
		return null;
	}

}
