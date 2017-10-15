package cop5556sp17;



import java.util.*;

import cop5556sp17.AST.Dec;
import javafx.util.Pair;


public class SymbolTable {
	
	
	//TODO  add fields
	int current_scope, next_scope;
	Stack<Integer> scopeStack = new Stack<Integer>();
	//List<Pair> p = new ArrayList<Pair>();
	//List<Integer,Dec> att = new Array<Integer,Dec>(); 
	Map<String,Map<Integer,Dec>> map = new HashMap<String,Map<Integer,Dec>>();

	/** 
	 * to be called when block entered
	 */
	public void enterScope(){
		//TODO:  IMPLEMENT THIS
		current_scope = next_scope++;
		scopeStack.push(current_scope);
		
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		//TODO:  IMPLEMENT THIS
		scopeStack.pop();
		current_scope = scopeStack.peek();
	}
	
	public boolean insert(String ident, Dec dec){
		//TODO:  IMPLEMENT THIS
		if(map.containsKey(ident)){
			if(map.get(ident).containsKey(current_scope)) return false;
			else map.get(ident).put(current_scope, dec);
		}
		else {
			Map<Integer,Dec> in = new HashMap<Integer,Dec>();
			in.put(current_scope, dec);
			map.put(ident,in);
			
		}
		return true;
	}
	
	public Dec lookup(String ident){
		//TODO:  IMPLEMENT THIS
		Map<Integer,Dec> m = map.get(ident);
		int i=scopeStack.peek();
		while(i>=0){
			if(m.containsKey(i)){
				return m.get(i);
			}
			else i--;
		}
		return null;
	}
		
	public SymbolTable() {
		//TODO:  IMPLEMENT THIS
		current_scope = 0;
		next_scope = 1;
		scopeStack.push(current_scope);
	}


	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		return "";
	}
	
	


}
