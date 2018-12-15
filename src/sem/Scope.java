package sem;

import ast.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Scope {
	private Scope outer;
	private Map<String, Symbol> symbolTable = new HashMap<String, Symbol>();

	public Scope(Scope outer) {
		this.outer = outer; 
	}
	
	public Scope() { this(null); }


	// Lookup checks if the symbol exists in the file
	public Symbol lookup(String name) {
		// To be completed... // done?
		Symbol s = lookupCurrent(name);
		if (s != null) {
			return s;
		} else if (outer != null) {
			return outer.lookup(name);
		} else {
			return null;
		}
	}
	
	public Symbol lookupCurrent(String name) {
		// To be completed... //done?
		return symbolTable.get(name);
	}
	
	public void put(Symbol sym) {
		symbolTable.put(sym.name, sym);
	}
}
