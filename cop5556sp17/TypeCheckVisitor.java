package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import java.util.ArrayList;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		TypeName t1 = (TypeName)binaryChain.getE0().visit(this, null);
		TypeName t2 = (TypeName)binaryChain.getE1().visit(this, null);

        if(binaryChain.getArrow().isKind(ARROW)){
        if(t1.isType(URL,FILE)&&t2.isType(IMAGE)) binaryChain.setTypeName(IMAGE);
                   else if(t1.isType(FRAME)&&((binaryChain.getE1() instanceof FrameOpChain))){
                   switch(binaryChain.getE1().getFirstToken().kind){
                   case KW_XLOC:
                   case KW_YLOC:binaryChain.setTypeName(INTEGER);break;
                   case KW_SHOW: case KW_HIDE:
                   case KW_MOVE: binaryChain.setTypeName(FRAME);break;
                   default: throw new TypeCheckException("Illegal chain elem");
                   }
                   }
                   else if(t1.isType(IMAGE)&&(binaryChain.getE1() instanceof ImageOpChain)){
                	    if((binaryChain.getE1().getFirstToken().isKind(OP_WIDTH))||(binaryChain.getE1().getFirstToken().isKind(OP_HEIGHT))){
                	    	binaryChain.setTypeName(INTEGER);
                	    }
                	    else if(binaryChain.getE1().getFirstToken().isKind(KW_SCALE)) binaryChain.setTypeName(IMAGE);
                	    else throw new TypeCheckException("Illegal chain elem");
                   }
                   else if(t1.isType(IMAGE)&&t2.isType(FRAME)) binaryChain.setTypeName(FRAME);
                   else if(t1.isType(IMAGE)&&t2.isType(FILE)) binaryChain.setTypeName(NONE);
                   else if((t1.isType(IMAGE))&&(binaryChain.getE1() instanceof FilterOpChain)){
                	       if(binaryChain.getE1().getFirstToken().isKind(OP_GRAY)||binaryChain.getE1().getFirstToken().isKind(OP_BLUR)||binaryChain.getE1().getFirstToken().isKind(OP_CONVOLVE)){
                	    	   binaryChain.setTypeName(IMAGE);
                	       }
                	       else throw new TypeCheckException("Illegal Chain elem");
                         }
                   else if((t1.isType(IMAGE))&&((binaryChain.getE1() instanceof IdentChain)&&t2.isType(IMAGE))){
                	   binaryChain.setTypeName(IMAGE);
                   }
                   else if((t1.isType(INTEGER))&&((binaryChain.getE1() instanceof IdentChain)&&t2.isType(INTEGER))){
                	   binaryChain.setTypeName(INTEGER);
                   }
                   else throw new TypeCheckException("Illegal Chain");}
        else if(binaryChain.getArrow().isKind(BARARROW)){
        	          if((t1.isType(IMAGE))&&(binaryChain.getE1() instanceof FilterOpChain)){
        			  if(binaryChain.getE1().getFirstToken().isKind(OP_GRAY)||binaryChain.getE1().getFirstToken().isKind(OP_BLUR)||binaryChain.getE1().getFirstToken().isKind(OP_CONVOLVE)){
	    	          binaryChain.setTypeName(IMAGE);
	                  }
	                  else throw new TypeCheckException("Illegal Chain elem");
                      }
                      else throw new TypeCheckException("Illegal Chain");}
        else throw new TypeCheckException("Illegal Binary Chain");

		return binaryChain.getTypeName();
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		TypeName t1 = (TypeName)binaryExpression.getE0().visit(this, null);
		TypeName t2 = (TypeName)binaryExpression.getE1().visit(this, null);
        switch(binaryExpression.getOp().kind){
        case PLUS:
        case MINUS:if(t1.isType(INTEGER)&&t2.isType(INTEGER)) binaryExpression.setTypeName(INTEGER);
                   else if(t1.isType(IMAGE)&&t2.isType(IMAGE)) binaryExpression.setTypeName(IMAGE);
                        else throw new TypeCheckException("Expression not valid for binaryop"); break;
        case TIMES:if((t1.isType(INTEGER)&&t2.isType(IMAGE)) || (t1.isType(IMAGE)&&t2.isType(INTEGER))){
        	       binaryExpression.setTypeName(IMAGE);
                    }
                   else if(t1.isType(INTEGER)&&t2.isType(INTEGER)) binaryExpression.setTypeName(INTEGER);
                        else throw new TypeCheckException("Not a legal expression"); break;
        case DIV:if(t1.isType(INTEGER)&&t2.isType(INTEGER)) binaryExpression.setTypeName(INTEGER);
                 else if(t1.isType(IMAGE)&&t2.isType(INTEGER)) binaryExpression.setTypeName(IMAGE);
                 else throw new TypeCheckException("Not a legal expression"); break;
        case LT:
        case GT:
        case LE:
        case GE:if((t1.isType(INTEGER)&&t2.isType(INTEGER))||(t1.isType(BOOLEAN)&&t2.isType(BOOLEAN))) binaryExpression.setTypeName(BOOLEAN);
                else throw new TypeCheckException("Expression not valid for binaryop"); break;
        case EQUAL:
        case NOTEQUAL:if(t1.equals(t2)) binaryExpression.setTypeName(BOOLEAN);break;
        case OR:
        case AND: if(t1.isType(BOOLEAN)&&t2.isType(BOOLEAN)){
        	      binaryExpression.setTypeName(BOOLEAN);
                  }
                  else throw new TypeCheckException("Not a valid boolean exp");break;
        case MOD: if(t1.isType(INTEGER)&&t2.isType(INTEGER)){
        	      binaryExpression.setTypeName(INTEGER);
                   }
                  else if(t1.isType(IMAGE)&&t2.isType(INTEGER)){
  	               binaryExpression.setTypeName(IMAGE);
                   }
        		  else throw new TypeCheckException("Not a valid expression"); break;
        default: throw new TypeCheckException("Not a valid op");
        }
		return binaryExpression.getTypeName();
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		symtab.enterScope();
		for(Dec d:block.getDecs()){
			d.visit(this, null);
		}
		for(Statement s:block.getStatements()){
			s.visit(this, null);
		}
		symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		booleanLitExpression.setTypeName(BOOLEAN);
		return booleanLitExpression.getTypeName();
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		filterOpChain.getArg().visit(this, null);
		if(!(filterOpChain.getArg().getExprList().isEmpty())){
			throw new TypeCheckException("Not a filter op chain");
		}
		else filterOpChain.setTypeName(IMAGE);
		return filterOpChain.getTypeName();
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		frameOpChain.getArg().visit(this, null);
		if((frameOpChain.getFirstToken().isKind(KW_SHOW))||(frameOpChain.getFirstToken().isKind(KW_HIDE))){
			if(frameOpChain.getArg().getExprList().isEmpty()){
				frameOpChain.setTypeName(NONE);
			}
			else throw new TypeCheckException("Not a valid FrameOp chain");
		}
		else if((frameOpChain.getFirstToken().isKind(KW_XLOC))||(frameOpChain.getFirstToken().isKind(KW_YLOC))){
			if(frameOpChain.getArg().getExprList().isEmpty()){
				frameOpChain.setTypeName(INTEGER);
			}
			else throw new TypeCheckException("Not a valid FrameOp chain");
		}
		else if(frameOpChain.getFirstToken().isKind(KW_MOVE)){
			if(frameOpChain.getArg().getExprList().size()==2){
				frameOpChain.setTypeName(NONE);
			}
			else throw new TypeCheckException("Not a valid FrameOp chain");
		}
		else throw new TypeCheckException("Bug in the parser");

		return frameOpChain.getTypeName();
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		if((symtab.lookup(identChain.getFirstToken().getText()))==null){
		 throw new TypeCheckException("not declared or not within scope");
		}
		else {
			identChain.setDec(symtab.lookup(identChain.getFirstToken().getText()));
			identChain.setTypeName((symtab.lookup(identChain.getFirstToken().getText())).getTypeName());
		}
		return identChain.getTypeName();
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		if((symtab.lookup(identExpression.getFirstToken().getText()))==null){
			 throw new TypeCheckException("not declared or not within scope");
			}
		else{
			identExpression.setDec(symtab.lookup(identExpression.getFirstToken().getText()));
			identExpression.setTypeName(identExpression.getDec().getTypeName());
		}
		return identExpression.getTypeName();
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		TypeName t1 = (TypeName)ifStatement.getE().visit(this,null);
		ifStatement.getB().visit(this, null);
		if(t1.isType(BOOLEAN)) return t1;
		else throw new TypeCheckException("Not a valid exp in if");

	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		intLitExpression.setTypeName(INTEGER);
		return intLitExpression.getTypeName();
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		TypeName t1 = (TypeName)sleepStatement.getE().visit(this, null);
		if(t1.isType(INTEGER)) return t1;
		else throw new TypeCheckException("Not a valid expression");
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		TypeName t1 = (TypeName)whileStatement.getE().visit(this,null);
		whileStatement.getB().visit(this, null);
		if(t1.isType(BOOLEAN)) return t1;
		else throw new TypeCheckException("Not a valid exp in while");
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		boolean b = symtab.insert(declaration.getIdent().getText(),declaration);
		if(!b) throw new TypeCheckException("Multiple declaration not allowed within same scope");
		declaration.setTypeName(Type.getTypeName(declaration.getType()));
		return declaration.getTypeName();
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO Auto-generated method stub
		for(ParamDec pd:program.getParams()){
			pd.visit(this, null);
		}
		program.getB().visit(this, null);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		TypeName t1 = (TypeName)assignStatement.getVar().visit(this, null);
		TypeName t2 = (TypeName)assignStatement.getE().visit(this, null);

		if(t1.equals(t2)) return t1;
		else throw new TypeCheckException("Illegal assignment statement");
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		// TODO Auto-generated method stub
		if((symtab.lookup(identX.getFirstToken().getText()))==null){
			 throw new TypeCheckException("not declared or not within scope");
			}
		else{
			identX.setDec(symtab.lookup(identX.getFirstToken().getText()));

		}
		return identX.getDec().getTypeName();
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// TODO Auto-generated method stub
		boolean b = symtab.insert(paramDec.getIdent().getText(), paramDec);
		if(!b) throw new TypeCheckException("Multiple declaration not allowed within same scope");
		paramDec.setTypeName(Type.getTypeName(paramDec.getType()));
		return paramDec.getTypeName();
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		// TODO Auto-generated method stub
		constantExpression.setTypeName(INTEGER);
		return constantExpression.getTypeName();
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		imageOpChain.getArg().visit(this, null);
		if((imageOpChain.getFirstToken().isKind(OP_WIDTH))||(imageOpChain.getFirstToken().isKind(OP_HEIGHT))){
			if(imageOpChain.getArg().getExprList().isEmpty()){
				imageOpChain.setTypeName(INTEGER);
			}
			else throw new TypeCheckException("Not a valid ImageOp chain");
		}
		else if(imageOpChain.getFirstToken().isKind(KW_SCALE)){
			if(imageOpChain.getArg().getExprList().size()==1){
				imageOpChain.setTypeName(IMAGE);
			}
			else throw new TypeCheckException("Not a valid ImageOp chain");
		}
		else throw new TypeCheckException("Not a valid operation");

		return imageOpChain.getTypeName();
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// TODO Auto-generated method stub
		for(Expression e:tuple.getExprList()){
			TypeName t1 = (TypeName)e.visit(this, null);
			if(!t1.isType(INTEGER)) throw new TypeCheckException("Not a valid exp");
		}
		return null;
	}


}
