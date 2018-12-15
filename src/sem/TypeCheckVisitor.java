package sem;

import ast.*;

import java.lang.reflect.Field;
import java.util.*;

public class TypeCheckVisitor extends BaseSemanticVisitor<Type> {

	private Map<String, StructType> structNames = new HashMap<String, StructType>();
	//private List<VarDecl> structDecls = new LinkedList<>();
	private Type returnType;

	@Override
	public Type visitBaseType(BaseType bt) {
		// To be completed...
		return bt;
	}

	@Override
	public Type visitStructType(StructType st) {
		// To be completed...

		if (st.varDecls.size() == 0){
			//System.out.println("sgsdfgsdfgsdfgsd");
			if (structNames.containsKey(st.structName)){
				return st;
			} else {
				error("StructType does not exist!");
			}
		} else {
			if (structNames.containsKey(st.structName)){
				error("StructType already exists!");
			} else {
				//structDecls = new LinkedList<>();
				for (VarDecl vd : st.varDecls){
					vd.accept(this);
					//structDecls.add(vd);
				}
				structNames.put(st.structName, new StructType(st.structName, st.varDecls));
//				for (VarDecl vd : st.varDecls){
//					System.out.println(vd.varName + " of type  " + vd.type);
//				}
			}

		}
		//System.out.println("struct size is   " + structNames.size());
		return st;
	}

	@Override
	public Type visitBlock(Block b) {
		// To be completed...
		for (VarDecl vd : b.varDecls){
			vd.accept(this);
		}
		for (Stmt s : b.stmts){
			s.accept(this);
		}
		return null;
	}

	@Override
	public Type visitFunDecl(FunDecl fd) {
		// To be completed... // done?
		for (VarDecl vd : fd.params){
			vd.accept(this);
		}
		returnType = fd.type;
//		System.out.println(fd.type + " sdfkkgdfglkjsdfkjlgsd");
		fd.block.accept(this);
//		System.out.println(p.type + " fundecl");

		return fd.type;
	}

	@Override
	public Type visitProgram(Program p) {
		// To be completed...
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
	public Type visitVarDecl(VarDecl vd) {
		// To be completed... // done?
		if (vd.type == BaseType.VOID) {
			error("Cannot declare a VOID variable!");
			return null;
		} else {
			//System.out.println(vd.type.accept(this));
			if (vd.type instanceof StructType){
				vd.type.accept(this);
			}
			return null;
		}
	}

	@Override
	public Type visitVarExpr(VarExpr v) {

		if (v.vd != null) {
			v.type = v.vd.type;
			//System.out.println(v.vd.type);
			return v.vd.type;
		} else {
			if (!structNames.containsKey(v.name)){
				error("Variable doesn't exist!!!!!!!!!");
			}
			return null;
		}
//		System.out.println("ENTERING VAREXPR >>>>>>>>>>>>");
//		System.out.println(v.vd + "????????????????");
//		if (v.vd != null){
//			v.type = v.vd.type;
//			return v.vd.type;
//		} else {
//			//System.out.println("lkjsdfglkjhdlkgdlkjsfdjklfsdljksfslfdkj");
//			error("Variable doesn't exist !!!!!!");
//			return null;
//		}
	}

	@Override
	public Type visitPointerType(PointerType pt) {
		//pt.type.accept(this);
		return pt.type.accept(this);
	}

	@Override
	public Type visitArrayType(ArrayType at) {
		//at.type.accept(this);
		//System.out.println(at.type);
		return at.type.accept(this);
	}

	@Override
	public Type visitIntLiteral(IntLiteral il) {
		il.type = BaseType.INT;
		return BaseType.INT;
	}

	@Override
	public Type visitStrLiteral(StrLiteral sl) {
		sl.type = new ArrayType(BaseType.CHAR, (sl.string.length()+1));
		return sl.type;
	}

	@Override
	public Type visitChrLiteral(ChrLiteral cl) {
		cl.type = BaseType.CHAR;
		return BaseType.CHAR;
	}

	@Override
	public Type visitFunCallExpr(FunCallExpr fce) {
		if (fce.fd != null){
			FunDecl fd = fce.fd;
			switch (fce.name){
				case "read_i":
					return BaseType.INT;
				case "read_c":
					return BaseType.CHAR;
				default:
					//check if it exists
					if (fce.args.size() != fd.params.size()){
						error("Parameter number doesn't match with the Function Declaration!");
					} else {
						for (int i = 0; i < fce.args.size(); i++){
							Expr e = fce.args.get(i);
							Type argiT = e.accept(this);
							VarDecl argiD = fd.params.get(i);
							Type argiDT = argiD.type;
//					System.out.println(argiT.accept(this) + "   >>>>>>");
//					System.out.println(argiDT.accept(this) + " <<<<<<<<<");
							if (argiT != null && argiDT != null) {
								if (argiT != argiDT) {
									if (argiT.getClass().equals(argiDT.getClass())) {
//								System.out.println(argiT.accept(this) + ">>>>>>>>>>>");
//								System.out.println(argiDT.accept(this) + "<<<<<<<<<<<");
										if (argiT.accept(this) != argiDT.accept(this)) {
											error("Types of parameters don't match");
										}
									} else {
										error("Arguments don't match!");
									}
								}
							} else {
								error("One of them is null");
							}

						}
						Type returnDT = fd.type;
						fce.type = returnDT;
						return returnDT;
					}
			}

		} else {
			error("Function doesn't exist!");
		}


		return null;
	}

	@Override
	public Type visitBinOp(BinOp bo) {
		Expr lhs = bo.lhs;
		Expr rhs = bo.rhs;
//		System.out.println(lhs + " lhs >>>>>>>>>>>>>");
//		System.out.println(rhs + " rhs>>>>>>>>>>>>>");
		if (lhs == null || rhs == null){
			error("Something is empty!");
		} else {
			Type lhsT = lhs.accept(this);
			Type rhsT = rhs.accept(this);
			if (bo.op == Op.ADD || bo.op == Op.SUB || bo.op == Op.MUL || bo.op == Op.DIV || bo.op == Op.MOD || bo.op == Op.OR || bo.op == Op.AND || bo.op == Op.GT || bo.op == Op.LT || bo.op == Op.GE || bo.op == Op.LE){

//			System.out.println(lhs  + "     lhs");
//			System.out.println(rhs  + "     rhs");

//			System.out.println(lhsT  + "     lhsType  !!!!!!!");
				if (lhsT == BaseType.INT && rhsT == BaseType.INT ){
//				System.out.println("You are in the BinOp");
					bo.type = BaseType.INT;
					return BaseType.INT;
				} else {
					error("The values you have entered are not of Type INT!");
				}
			} else {
				if (lhsT != null && rhsT != null && lhsT != rhsT){
//					System.out.println(lhsT + " lhs >>>>>>>>>>>>>");
//					System.out.println(rhsT + " rhs>>>>>>>>>>>>>");
					if (lhsT.getClass().equals(rhsT.getClass())) {
						if (lhsT.accept(this) != rhsT.accept(this)) {
							error("Types of parameters don't match");
						} else {
							bo.type = BaseType.INT;
							return BaseType.INT;
						}
					} else {
						error("Wrong types");
					}
				} else {
					bo.type = BaseType.INT;
					return BaseType.INT;
				}
			}
		}
		return null;
	}

	@Override
	public Type visitOp(Op op) {
		return null;
	}

	@Override
	public Type visitArrayAccessExpr(ArrayAccessExpr aae) {
		Type arrayT = aae.array.accept(this);
//		System.out.println(arrayT + "  array");
		//System.out.println(aae.index + "   index");
		Type indexT = aae.index.accept(this);
		if (arrayT != null) {
			if (indexT != BaseType.INT) {                // check number of index. must be equal to arraytype
				error("Index not an integer!");
			}
			aae.type = arrayT.accept(this);
			Type returnAT = aae.type;

			if (arrayT instanceof ArrayType) {
				//System.out.println(((ArrayType) arrayT).type + " instance of >>>>>>>>>>");
				//System.out.println(returnAT + " returnAT dfglkjksdfgjklsdfjlksgjkld");
				if (((ArrayType) arrayT).type == returnAT) {
					return returnAT;
				} else {
					error("Not ArrayType of elemType!");
				}
			} else if (arrayT instanceof PointerType) {
				//System.out.println(((PointerType) arrayT).type + " instance of ");
				if (((PointerType) arrayT).type == returnAT) {
					return returnAT;
				} else {
					error("Not PointerType of elemType!");
				}
			} else {
				error("Not PointerType of elemType!");
			}
		}
		return null;
	}

	@Override
	public Type visitFieldAccessExpr(FieldAccessExpr fa) {
		Expr e = fa.structure;
		Type returnF = fa.type;
//		System.out.println(e + "   Begin of fae>>>>>>>>>>>>");
		if (e != null){
			e.accept(this);     // essential to check if the name exists
//			System.out.println(e.accept(this) + "   e.accept this >>>>>>>>>>");
			if (e.accept(this) instanceof StructType){
				StructType st = (StructType)e.accept(this);
//				System.out.println( " <<<<<<<<<<<<<<<  Instance of StructType >>>>>>>>>>");
//				System.out.println(fa.fieldName + "  fieldname");
//				System.out.println(st.structName + "   name of struct");
				if (structNames.containsKey(st.structName)){
					StructType temp = structNames.get(st.structName);
					for (VarDecl vd : temp.varDecls){
//						System.out.println(vd.varName + "  of type >>>>  " + vd.type);
						if (vd.varName.equals(fa.fieldName)) {
							returnF = vd.type;
						}
					}
					if (returnF != null){
//						System.out.println("Return value >>>>>>>>>>  " + returnF);
						return returnF;
					} else {
						error("Variable non existant!");
					}
				} else {
					error("Error! Doesn't exist");
				}

			} else {
				error("The structure you have entered is not a structure!");
			}
		} else {
			error("Empty structure");
		}
		return null;
	}

	@Override
	public Type visitValueAtExpr(ValueAtExpr vae) {
		Type vaeT = vae.expr.accept(this);
		vae.type = vaeT.accept(this);
		Type returnT = vae.type;
		if (vaeT instanceof PointerType){
			if (((PointerType) vaeT).type == returnT) {
				return returnT;
			} else {
				error("Not PointerType");
			}
		} else {
			error("Noot PointerType");
		}
		return null;
	}

	@Override
	public Type visitSizeOfExpr(SizeOfExpr soe) {
		// WHAT ELSE?????????????????????????????????????????????????????????????????
		return BaseType.INT;
	}

	@Override
	public Type visitTypecastExpr(TypecastExpr tce) {
		Type checkT = tce.type;
		Type eT = tce.expr.accept(this);
		//System.out.println(checkT + "   TypeCast type");
		//System.out.println(eT + "   type   ");
		//Type t = eT.accept(this);
		if (eT != null) {
			if (checkT == BaseType.INT && eT == BaseType.CHAR){
				return BaseType.INT;
			}
			else if (eT instanceof ArrayType) {
				Type t = (((ArrayType) eT).type);
				if (t != checkT.accept(this)) {
					error("Wrong use of TypeCast!");
				} else {
					//System.out.println("You will return this in the typecast       "  + checkT);
					return checkT;
				}
			}
			else if (eT instanceof PointerType){
				Type t = (((PointerType) eT).type);
				if (t == checkT.accept(this)){
					error("You trying to cast a Pointer of the same type");
				} else {
					//System.out.println("You will return this in the typecast       "  + checkT);
					return checkT;
				}
			}
			else {
				error("Types don't match");
			}
		} else {
			error("Empty typecast");
		}

		return null;
	}

	@Override
	public Type visitWhile(While w) {
		//w.expr.accept(this);
		if (w.expr != null) {
			if (w.expr.accept(this) != BaseType.INT) {
				error("While expression isn't of type INT!");
			}
		}
		if (w.stmt != null) {
			w.stmt.accept(this);
		} else {
			error("While is empty!");
		}
		return null;
	}

	@Override
	public Type visitIf(If i) {

//		Type exprT = i.expr.accept(this);
//		i.expr.type = exprT;
		if (i.expr.accept(this) != BaseType.INT){
			error("If Expression isn't of type INT!");
		}
		if (i.stmt != null) {
			i.stmt.accept(this);
		}
		if (i.opt_stmt != null){
			i.opt_stmt.accept(this);
		}
		return null;
	}

	@Override
	public Type visitAssign(Assign a) {

//		System.out.println(a.lhs+ "  lhsT <<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//		System.out.println(a.rhs + "  rhsT <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		if (a.lhs.accept(this) != null && a.rhs.accept(this) != null){
			Type lhsT = a.lhs.accept(this);
			Type rhsT = a.rhs.accept(this);
//			System.out.println(lhsT + "  lhsT >>>>>>>>>>>>>>dfvsdfg");
//			System.out.println(rhsT + "  rhsT >>>>>>>>>>>>>>sdfgsdf");
			if (a.lhs instanceof VarExpr || a.lhs instanceof FieldAccessExpr || a.lhs instanceof ArrayAccessExpr || a.lhs instanceof ValueAtExpr){

//				System.out.println(lhsT + "  lhsT >>>>>>>>>>>>>>");
//				System.out.println(rhsT + "  rhsT >>>>>>>>>>>>>>");

				if (a.lhs instanceof ArrayAccessExpr){
//					System.out.println("Start of arrayaccessexpr");
					if (a.rhs instanceof ArrayAccessExpr){
						Expr lhsN = ((ArrayAccessExpr) a.lhs).index;
						Expr rhsN = ((ArrayAccessExpr) a.rhs).index;
						if (lhsN != rhsN){
							error("Indexes do not match!");
						}
					} else {
//						System.out.println(">>>>>>>>>>>>>>>>>");
//						System.out.println(lhsT + "  lhsT >>>>>>>>>>>>>>dfvsdfg");
//						System.out.println(rhsT + "  rhsT >>>>>>>>>>>>>>sdfgsdf");
						if (lhsT != rhsT) {
							if (lhsT.getClass().equals(rhsT.getClass())) {
								if (lhsT.accept(this) != rhsT.accept(this)) {
									error("Types of Assign don't match!!!!!!!!");
								}
							}
						}
					}
				}
				if (!(lhsT instanceof ArrayType) || lhsT == BaseType.VOID){
//					System.out.println(lhsT + "  lhsT >>>>>>>>>>>>>>  I enter the correct one");
//					System.out.println("---------------------");
					if (lhsT != rhsT) {
						if (lhsT.getClass().equals(rhsT.getClass())) {
							if (lhsT.accept(this) != rhsT.accept(this)) {
								error("Types of Assign don't match!!!!!!!!");
							}
						}
					}
				} else {
					error("Lhs is of unsupported type");
				}

			} else {
				error("Invalide type of left hand side");
			}
		} else {
			error("something doesn't exist");
		}
		return null;
	}

	@Override
	public Type visitReturn(Return r) {
		if (r.expr != null){
			Type returnT = r.expr.accept(this);
//			System.out.println(returnT);
			if (returnType == BaseType.VOID){
				if (returnT != null){
					error("Error on Return VOID!");
				}
			} else {
				if (returnT != returnType){
//					System.out.println(returnT + "   returnT");
//					System.out.println(returnType + "   returnType");
					if (returnT.getClass().equals(returnType.getClass())){
						if (returnT.accept(this) != returnType.accept(this)){
							error("Returns don't match!!!!");
						}
					} else {
						error("Different type of return than function");
					}
				}
			}
			return returnT;
		} else {
			if (returnType != BaseType.VOID){
				error("Only VOID functions don't have a return expression");
			}
		}
		return null;
	}

	@Override
	public Type visitExprStmt(ExprStmt es) {
		return es.expr.accept(this);
	}



	// To be completed...


}
