package sem;

public abstract class Symbol {
	public String name;

	boolean isVar(Symbol sym) {
		if (sym instanceof VarSymbol){
			return true;
		}
		return false;
	}
	boolean isFun(Symbol sym){
		if (sym instanceof FunSymbol){
			return true;
		}
		return false;
	}

	public Symbol(String name) {
		this.name = name;
	}
}
