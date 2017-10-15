package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

public abstract class Expression extends ASTNode {
	
	TypeName type;
	
	protected Expression(Token firstToken) {
		super(firstToken);
	}
	
	public TypeName getTypeName(){
		return type;
	}
	
	public void setTypeName(TypeName type){
		this.type = type;
	}
	

	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

}
