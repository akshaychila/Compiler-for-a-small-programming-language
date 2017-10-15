package cop5556sp17;

import cop5556sp17.Scanner.Kind;

import static cop5556sp17.Scanner.Kind.*;

import java.util.*;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	ASTNode parse() throws SyntaxException {
		ASTNode p = program();
		matchEOF();
		return p;
	}

	Expression expression() throws SyntaxException {
		//TODO
		Token ft = t;
		Expression e0=null;
		Expression e1=null;
		e0 = term();
		while(t.isKind(LT)||t.isKind(LE)||t.isKind(GT)||t.isKind(GE)||t.isKind(EQUAL)||t.isKind(NOTEQUAL)){
			Token op = t;
			consume();
			e1 = term();
			e0 = new BinaryExpression(ft,e0,op,e1);
		}
		return e0;
	}

	Expression term() throws SyntaxException {
		//TODO
		Token ft = t;
		Expression e0=null;
		Expression e1=null;
		e0 = elem();
		while(t.isKind(PLUS)||t.isKind(MINUS)||t.isKind(OR)){
			Token op = t;
			consume();
			e1 = elem();
			e0 = new BinaryExpression(ft,e0,op,e1);
		}
		return e0;
	}

	Expression elem() throws SyntaxException {
		//TODO
		Token ft = t;
		Expression e0=null;
		Expression e1=null;
		e0=factor();
		while(t.isKind(TIMES)||t.isKind(DIV)||t.isKind(AND)||t.isKind(MOD)){
			Token op = t;
			consume();
			e1=factor();
			e0 = new BinaryExpression(ft,e0,op,e1);

		}
		return e0;
	}

	Expression factor() throws SyntaxException {
		Token ft = t;
		Expression e = null;
		Kind kind = t.kind;
		switch (kind) {
		case IDENT: {
			e = new IdentExpression(ft);
			consume();
		}
			break;
		case INT_LIT: {
			e = new IntLitExpression(ft);
			consume();
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
			e = new BooleanLitExpression(ft);
			consume();
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			e = new ConstantExpression(ft);
			consume();
		}
			break;
		case LPAREN: {
			consume();
			e = expression();
			match(RPAREN);
		}
			break;
		default:
			//you will want to provide a more useful error message
			String s = t.getLinePos().toString(); 
			throw new SyntaxException("illegal factor at "+s);
		}
		return e;
	}

	Block block() throws SyntaxException {
		//TODO
		Token ft = t;
		Block b = null;
		ArrayList<Dec> dec = new ArrayList<Dec>();
		ArrayList<Statement> st= new ArrayList<Statement>();
		match(LBRACE);
		while(t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN)||t.isKind(KW_IMAGE)||t.isKind(KW_FRAME)||
				t.isKind(OP_SLEEP)||t.isKind(KW_WHILE)||t.isKind(KW_IF)||t.isKind(IDENT)||
				t.isKind(OP_BLUR)||t.isKind(OP_GRAY)||t.isKind(OP_CONVOLVE)||t.isKind(KW_SHOW)||t.isKind(KW_HIDE)||t.isKind(KW_MOVE)||
				t.isKind(KW_XLOC)||t.isKind(KW_YLOC)||t.isKind(OP_WIDTH)||t.isKind(OP_HEIGHT)||
				t.isKind(KW_SCALE)){
			
			switch(t.kind){
			case KW_INTEGER:case KW_BOOLEAN:case KW_IMAGE:case KW_FRAME: dec.add(dec()); break;
			case OP_SLEEP: case KW_WHILE: case KW_IF: case IDENT: 
			case OP_BLUR:case OP_GRAY: case OP_CONVOLVE: case KW_SHOW:case KW_HIDE: case KW_MOVE:
			case KW_XLOC: case KW_YLOC: case OP_WIDTH: case OP_HEIGHT:
			case KW_SCALE: st.add(statement()); break;
			default: break;
			}
		}
		if(t.isKind(RBRACE)){
		match(RBRACE);}
		else {
			String s = t.getLinePos().toString();
			throw new SyntaxException("Closing brace expected at "+s);
		}
		b = new Block(ft,dec,st);
		return b;
	}

	Program program() throws SyntaxException {
		//TODO
		Token ft = t;
		ArrayList<ParamDec> l = new ArrayList<ParamDec>();
		Block b = null;
		match(IDENT);
		if(t.isKind(LBRACE)){
			b= block();
		}
		else if(t.isKind(KW_URL)||t.isKind(KW_FILE)||t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN)){
			l.add(paramDec());
			while(t.isKind(COMMA)){
				consume();
				l.add(paramDec());
			}
			b = block();
		}
		else {
			String s = t.getLinePos().toString();
			throw new SyntaxException("Ident should be followed by a block or paramdec at "+s);
		}
		return new Program(ft,l,b);
		
	}

	ParamDec paramDec() throws SyntaxException {
		//TODO
		Token ft = t;
		ParamDec pd= null;
		if(t.isKind(KW_URL)||t.isKind(KW_FILE)||t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN)){
			consume();
			Token i=t;
			match(IDENT);
			pd = new ParamDec(ft,i);
			
		}
		else {
			String s = t.getLinePos().toString();
			throw new SyntaxException("Token not a paramDec at "+s);
		}
		return pd;
	}

	Dec dec() throws SyntaxException {
		//TODO
		Token ft = t;
		Dec d = null;
		if(t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN)||t.isKind(KW_IMAGE)||t.isKind(KW_FRAME)){
			consume();
			Token i = t;
			match(IDENT);
			d = new Dec(ft,i);
		}
		else {
			String s = t.getLinePos().toString();
			throw new SyntaxException("Token not a Dec at "+s);
		}
		return d;
	}

	Statement statement() throws SyntaxException {
		//TODO
		Token ft = t;
		Statement s= null;
		Expression e = null;
		Block b = null;
		IdentLValue ilv = null;
		if(t.isKind(OP_SLEEP)){
			consume();
			e= expression();
			match(SEMI);
			s = new SleepStatement(ft,e);
		}
		else if(t.isKind(KW_WHILE)||t.isKind(KW_IF)){
			consume();
			match(LPAREN);
			e = expression();
			match(RPAREN);
			b = block();
			if(ft.isKind(KW_WHILE)){
				s = new WhileStatement(ft,e,b);
			}
			else s = new IfStatement(ft,e,b);
			
		}
		else if(t.isKind(IDENT)){
			if(scanner.peek().kind.equals(ASSIGN)){
				ilv = new IdentLValue(t);
				consume();
				consume();
				e = expression();
				s = new AssignmentStatement(ft,ilv,e);
			}
			else s = chain();
			match(SEMI);
		}
		else {
			s = chain();
			match(SEMI);
		}
		return s;
		
	}

	Chain chain() throws SyntaxException {
		//TODO
		Token ft = t;
		Chain c0 = null;
		ChainElem c1= null;
		ChainElem c2 = null;
		c0= chainElem();
		Token o = null;
		if(t.isKind(ARROW)||t.isKind(BARARROW)){
		    o = t;
			consume();
		}
		else {
			String s = t.getLinePos().toString();
			throw new SyntaxException("Arrow or bararrow expected at "+s);
		}
		c1= chainElem();
		c0 = new BinaryChain(ft,c0,o,c1);
		while(t.isKind(ARROW)||t.isKind(BARARROW)){
			Token op = t;
			consume();
			c2 = chainElem();
			c0 = new BinaryChain(ft,c0,op,c2);
		}
		return c0;
	}

	ChainElem chainElem() throws SyntaxException {
		//TODO
		Token ft = t;
		ChainElem c = null;
		Tuple tup = null;
		if(t.isKind(IDENT)){
			consume();
			c = new IdentChain(ft); 
		}
		else if(t.isKind(OP_BLUR)||t.isKind(OP_GRAY)||t.isKind(OP_CONVOLVE)||t.isKind(KW_SHOW)||t.isKind(KW_HIDE)||t.isKind(KW_MOVE)||
				t.isKind(KW_XLOC)||t.isKind(KW_YLOC)||t.isKind(OP_WIDTH)||t.isKind(OP_HEIGHT)||
				t.isKind(KW_SCALE)){
			Token tmp = consume();
			tup = arg();
			switch(tmp.kind){
			case OP_BLUR: case OP_GRAY: case OP_CONVOLVE: c = new FilterOpChain(ft,tup);break;
			case KW_SHOW: case KW_HIDE: case KW_MOVE: case KW_XLOC: case KW_YLOC: c= new FrameOpChain(ft,tup);break;
			case OP_WIDTH: case OP_HEIGHT: case KW_SCALE: c = new ImageOpChain(ft,tup);break;
			default: break;
			}
		}
		else {
			String s = t.getLinePos().toString();
			throw new SyntaxException("token is not a chain element at "+s);
		}
		return c;
	}

	Tuple arg() throws SyntaxException {
		//TODO
		Token ft=t;
		
		List<Expression> exp = new ArrayList<Expression>();
		if(t.isKind(LPAREN)){
			consume();
			exp.add(expression());
			while(t.isKind(COMMA)){
				consume();
				exp.add(expression());
			}
			match(RPAREN);
			return new Tuple(ft,exp);
		}
		else return new Tuple(ft,exp);
	}

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		String s = t.getLinePos().toString();
		throw new SyntaxException("saw " + t.kind + "at "+s+" expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		for(Kind k:kinds){
			if(t.isKind(k)){
				return consume();
			}
		}
		String s = t.getLinePos().toString();
		throw new SyntaxException("saw " + t.kind + "at "+s+" expected one of " + kinds);
	
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
