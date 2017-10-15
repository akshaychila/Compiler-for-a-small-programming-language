package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
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
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	int index=0;

	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec dec : params)
			dec.visit(this, arg);
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
//TODO  visit the local variables
		//mv.visitLocalVariable("block",classDesc, null, startRun, endRun,1);
		ArrayList<Dec> dec = program.getB().getDecs();
		for (Dec d : dec){
			mv.visitLocalVariable(d.getIdent().getText(),d.getTypeName().getJVMTypeDesc(), null, startRun, endRun,d.getSlotNo());
		}

		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method


		cw.visitEnd();//end of class

		//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypeName());
		assignStatement.getVar().visit(this, arg);
		if(assignStatement.getVar().getDec().getTypeName().isType(IMAGE)){
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "copyImage", PLPRuntimeImageOps.copyImageSig, false);
			//mv.visitInsn(ARETURN);
		}
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		//assert false : "not yet implemented";
		binaryChain.getE0().visit(this, 0);
		if(binaryChain.getE0().getTypeName().isType(TypeName.URL)){
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromURL",PLPRuntimeImageIO.readFromURLSig,false);
			//mv.visitInsn(ARETURN);

		}
		else if(binaryChain.getE0().getTypeName().isType(TypeName.FILE)){
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromFile",PLPRuntimeImageIO.readFromFileDesc,false );
			//mv.visitInsn(ARETURN);

		}
		binaryChain.getE1().visit(this, 1);
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
      //TODO  Implement this
		binaryExpression.getE0().visit(this, arg);
		binaryExpression.getE1().visit(this, arg);
		Label L1 = new Label();
		Label L2 = new Label();

		CodeGenUtils.genPrint(DEVEL, mv, "\nbinaryExpression =");
		switch(binaryExpression.getOp().kind){
		case PLUS:if(binaryExpression.getE0().getTypeName().isType(TypeName.INTEGER)&&binaryExpression.getE1().getTypeName().isType(TypeName.INTEGER)){
			      mv.visitInsn(IADD);}
		          else if(binaryExpression.getE0().getTypeName().isType(IMAGE)&&binaryExpression.getE1().getTypeName().isType(IMAGE)){
		        	   mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "add", PLPRuntimeImageOps.addSig, false);
		          }
		          else mv.visitLdcInsn(binaryExpression.getTypeName()); break;
		case MINUS: if(binaryExpression.getE0().getTypeName().isType(TypeName.INTEGER)&&binaryExpression.getE1().getTypeName().isType(TypeName.INTEGER)){
		          mv.visitInsn(ISUB);}
		          else if(binaryExpression.getE0().getTypeName().isType(IMAGE)&&binaryExpression.getE1().getTypeName().isType(IMAGE)){
     	          mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "sub", PLPRuntimeImageOps.subSig, false);
                 }
                  else mv.visitLdcInsn(binaryExpression.getTypeName()); break;
		case TIMES: if(binaryExpression.getE0().getTypeName().isType(TypeName.INTEGER)&&binaryExpression.getE1().getTypeName().isType(TypeName.INTEGER)){
	          mv.visitInsn(IMUL);}
		     else if(binaryExpression.getE0().getTypeName().isType(IMAGE)&&binaryExpression.getE1().getTypeName().isType(TypeName.INTEGER)){
     	     mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mul", PLPRuntimeImageOps.mulSig, false);
             }
             else mv.visitLdcInsn(binaryExpression.getTypeName()); break;
		case DIV: if(binaryExpression.getE0().getTypeName().isType(TypeName.INTEGER)&&binaryExpression.getE1().getTypeName().isType(TypeName.INTEGER)){
	          mv.visitInsn(IDIV);}
		     else if(binaryExpression.getE0().getTypeName().isType(IMAGE)&&binaryExpression.getE1().getTypeName().isType(TypeName.INTEGER)){
   	         mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "div", PLPRuntimeImageOps.divSig, false);
             }
             else mv.visitLdcInsn(binaryExpression.getTypeName()); break;
		case MOD: if(binaryExpression.getE0().getTypeName().isType(IMAGE)&&binaryExpression.getE1().getTypeName().isType(TypeName.INTEGER)){
   	     mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mod", PLPRuntimeImageOps.modSig, false);
           }
           else mv.visitInsn(IREM); break;
		case AND: mv.visitInsn(IAND);break;
		case OR: mv.visitInsn(IOR);break;

		case LT: mv.visitJumpInsn(IF_ICMPGT,L1);mv.visitInsn(ICONST_0);mv.visitJumpInsn(GOTO,L2); break;
		case GT: mv.visitJumpInsn(IF_ICMPLT,L1);mv.visitInsn(ICONST_0);mv.visitJumpInsn(GOTO,L2);break;
		case LE: mv.visitJumpInsn(IF_ICMPGE,L1);mv.visitInsn(ICONST_0);mv.visitJumpInsn(GOTO,L2);break;
		case GE: mv.visitJumpInsn(IF_ICMPLE,L1);mv.visitInsn(ICONST_0);mv.visitJumpInsn(GOTO,L2);break;
		case EQUAL: mv.visitJumpInsn(IF_ICMPEQ,L1);mv.visitInsn(ICONST_0);mv.visitJumpInsn(GOTO,L2);break;
		case NOTEQUAL: mv.visitJumpInsn(IF_ICMPNE,L1);mv.visitInsn(ICONST_0);mv.visitJumpInsn(GOTO,L2);break;
		default: break;
		}

		mv.visitLabel(L1);

		mv.visitInsn(ICONST_1);

		mv.visitLabel(L2);

		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//TODO  Implement this
		int slot=1;
		Label startBlock = new Label();
		mv.visitLabel(startBlock);
		for(Dec d:block.getDecs()){
			d.visit(this,slot++);
		}
		for(Statement s:block.getStatements()){
			s.visit(this, arg);

		}
        mv.visitInsn(RETURN);
		Label endBlock = new Label();
		mv.visitLabel(endBlock);
		for(Dec d:block.getDecs()){
		mv.visitLocalVariable(d.getIdent().getText(), d.getTypeName().getJVMTypeDesc(), null, startBlock, endBlock, d.getSlotNo());
		}
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		//TODO Implement this
		mv.visitLdcInsn(booleanLitExpression.getValue());
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
        if(constantExpression.getFirstToken().isKind(KW_SCREENWIDTH)){
        	mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenWidth", PLPRuntimeFrame.getScreenWidthSig, false);
        }
        else{
        	mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenHeight", PLPRuntimeFrame.getScreenHeightSig, false);
        }
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		//TODO Implement this
		declaration.setSlotNo((Integer)arg);
		//mv.visitVarInsn(ALOAD, declaration.getSlotNo());

		if(declaration.getTypeName().isType(TypeName.FRAME)){
            //mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(NEW, "cop5556sp17/PLPRuntimeFrame");
		    //mv.visitInsn(DUP);
    		//mv.visitInsn(ACONST_NULL);

			//mv.visitMethodInsn(INVOKESPECIAL, "cop5556sp17/PLPRuntimeFrame", "<init>", "()V",false);
			mv.visitVarInsn(ASTORE,declaration.getSlotNo());

		}
		else if(declaration.getTypeName().isType(TypeName.IMAGE)){
            //mv.visitVarInsn(ALOAD, 0);
           mv.visitTypeInsn(NEW, "java/awt/image/BufferedImage");
			//mv.visitInsn(DUP);
            //mv.visitInsn(ACONST_NULL);
			//mv.visitMethodInsn(INVOKESPECIAL, "java/awt/image/BufferedImage", "<init>", "()V",false);
			mv.visitVarInsn(ASTORE,declaration.getSlotNo());

		}

		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		//filterOpChain.getArg().visit(this, arg);
		mv.visitInsn(ACONST_NULL);

		if(filterOpChain.getFirstToken().isKind(OP_BLUR)){
		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "blurOp", PLPRuntimeFilterOps.opSig, false);
		}
		else if(filterOpChain.getFirstToken().isKind(OP_CONVOLVE)){
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "convolveOp", PLPRuntimeFilterOps.opSig, false);
			}
		else if(filterOpChain.getFirstToken().isKind(OP_GRAY)){
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "grayOp", PLPRuntimeFilterOps.opSig, false);
			}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
        frameOpChain.getArg().visit(this, arg);
        if(frameOpChain.getFirstToken().isKind(KW_MOVE)){
        	mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "moveFrame", PLPRuntimeFrame.moveFrameDesc, false);
        }
        else if(frameOpChain.getFirstToken().isKind(KW_HIDE)){
        	mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "hideImage", PLPRuntimeFrame.hideImageDesc, false);
        }
        else if(frameOpChain.getFirstToken().isKind(KW_SHOW)){
        	mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "showImage", PLPRuntimeFrame.showImageDesc, false);
        }
        else if(frameOpChain.getFirstToken().isKind(KW_XLOC)){
        	mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "getXVal", PLPRuntimeFrame.getXValDesc, false);
        }
        else if(frameOpChain.getFirstToken().isKind(KW_YLOC)){
        	mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "getYVal", PLPRuntimeFrame.getYValDesc, false);
        }
        else if(frameOpChain.getFirstToken().isKind(KW_SCREENHEIGHT)){
        	mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenHeight", PLPRuntimeFrame.getScreenHeightSig, false);
        }
        else if(frameOpChain.getFirstToken().isKind(KW_SCREENWIDTH)){
        	mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenWidth", PLPRuntimeFrame.getScreenWidthSig, false);
        }
       return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
			//
			if((int)arg==0){
				if(identChain.getDec() instanceof ParamDec){
				mv.visitFieldInsn(GETSTATIC, className, identChain.getFirstToken().getText(), identChain.getTypeName().getJVMTypeDesc());
				}
				else {
					if(identChain.getTypeName().isType(TypeName.INTEGER,TypeName.BOOLEAN)){
					mv.visitVarInsn(ILOAD, identChain.getDec().getSlotNo());}
					else mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNo());

				}
				}//
			else if((int)arg==1){
				if(identChain.getTypeName().isType(TypeName.INTEGER,TypeName.BOOLEAN)){
					if(identChain.getDec() instanceof ParamDec){
					mv.visitFieldInsn(PUTSTATIC, className, identChain.getFirstToken().getText(),identChain.getDec().getTypeName().getJVMTypeDesc() );}
					else mv.visitVarInsn(ISTORE, identChain.getDec().getSlotNo());
				}
				else if(identChain.getTypeName().isType(TypeName.IMAGE)){
					mv.visitVarInsn(ASTORE,identChain.getDec().getSlotNo());
				}
				else if(identChain.getTypeName().isType(TypeName.FILE)){
					mv.visitFieldInsn(GETSTATIC, className, identChain.getFirstToken().getText(), identChain.getTypeName().getJVMTypeDesc());
					mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "write", PLPRuntimeImageIO.writeImageDesc, false);
				}
				else if(identChain.getTypeName().isType(FRAME)){
					mv.visitVarInsn(ALOAD, 0);
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNo());
					mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);
				}
			}

		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		//TODO Implement this
		if(identExpression.getDec() instanceof ParamDec){
			mv.visitFieldInsn(GETSTATIC, className, identExpression.getFirstToken().getText(), identExpression.getTypeName().getJVMTypeDesc());
		}
		else if(identExpression.getDec().getTypeName().isType(TypeName.INTEGER,TypeName.BOOLEAN)){
			mv.visitVarInsn(ILOAD, identExpression.getDec().getSlotNo());}
		else mv.visitVarInsn(ALOAD, identExpression.getDec().getSlotNo());
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		//TODO Implement this
		if(identX.getDec() instanceof ParamDec){
			mv.visitFieldInsn(PUTSTATIC, className, identX.getText(),identX.getDec().getTypeName().getJVMTypeDesc() );
		}
		else if(identX.getDec().getTypeName().isType(TypeName.INTEGER,TypeName.BOOLEAN)){
			mv.visitVarInsn(ISTORE, identX.getDec().getSlotNo());}
		else mv.visitVarInsn(ASTORE, identX.getDec().getSlotNo());
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		//TODO Implement this
		ifStatement.getE().visit(this, arg);
		Label AFTER = new Label();
		mv.visitJumpInsn(IFEQ, AFTER);
		ifStatement.getB().visit(this, arg);
		mv.visitLabel(AFTER);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		imageOpChain.getArg().visit(this, arg);
		if(imageOpChain.getFirstToken().isKind(KW_SCALE)){
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "scale", PLPRuntimeImageOps.scaleSig, false);
		}
		else if(imageOpChain.getFirstToken().isKind(OP_WIDTH)){
			mv.visitMethodInsn(INVOKESTATIC, "java/awt/image/BufferedImage", "getWidth", PLPRuntimeImageOps.getWidthSig, false);
		}
		else if(imageOpChain.getFirstToken().isKind(OP_HEIGHT)){
			mv.visitMethodInsn(INVOKESTATIC, "java/awt/image/BufferedImage", "getHeight", PLPRuntimeImageOps.getHeightSig, false);
		}
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		//TODO Implement this
		mv.visitLdcInsn(intLitExpression.value);
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//TODO Implement this
		//For assignment 5, only needs to handle integers and booleans

		FieldVisitor fv;
		if(paramDec.getTypeName().isType(TypeName.INTEGER)){
		fv = cw.visitField(ACC_STATIC, paramDec.getIdent().getText(), "I", null, null);
		}
		else if(paramDec.getTypeName().isType(TypeName.BOOLEAN)){
		fv = cw.visitField(ACC_STATIC, paramDec.getIdent().getText(), "Z", null, null);
		}
		else if(paramDec.getTypeName().isType(TypeName.URL)){
			fv = cw.visitField(ACC_STATIC, paramDec.getIdent().getText(), "Ljava/net/URL;", null, null);
			}
		else if(paramDec.getTypeName().isType(TypeName.FILE)){
			fv = cw.visitField(ACC_STATIC, paramDec.getIdent().getText(), "Ljava/io/File;", null, null);
			}

		//mv.visitVarInsn(ALOAD,0);
		//mv.visitVarInsn(ALOAD, 1);
	   // mv.visitVarInsn(ALOAD, 2);
	    //mv.visitLdcInsn(index++);
	    //mv.visitInsn(AALOAD);
		if(paramDec.getTypeName().isType(TypeName.INTEGER)){

			//mv.visitVarInsn(ALOAD,0);
			mv.visitVarInsn(ALOAD, 1);
		    mv.visitLdcInsn(index++);
		    mv.visitInsn(AALOAD);
			//mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC,"java/lang/Integer","parseInt","(Ljava/lang/String;)I",false);
		mv.visitFieldInsn(PUTSTATIC, className, paramDec.getIdent().getText(), "I");}
		else if(paramDec.getTypeName().isType(TypeName.BOOLEAN)){
			//mv.visitVarInsn(ALOAD, 1);
			//mv.visitVarInsn(ALOAD,0);
			mv.visitVarInsn(ALOAD, 1);
		    mv.visitLdcInsn(index++);
		    mv.visitInsn(AALOAD);

			mv.visitMethodInsn(INVOKESTATIC,"java/lang/Boolean","parseBoolean","(Ljava/lang/String;)Z",false);
			mv.visitFieldInsn(PUTSTATIC, className, paramDec.getIdent().getText(), "Z");
		}
		else if(paramDec.getTypeName().isType(TypeName.URL)){
			//mv.visitVarInsn(ALOAD,0);
			mv.visitVarInsn(ALOAD, 1);
		    mv.visitLdcInsn(index++);
		    //mv.visitInsn(SWAP);
		    //mv.visitInsn(AALOAD);

			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "getURL", PLPRuntimeImageIO.getURLSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, paramDec.getIdent().getText(), "Ljava/net/URL;");
		}
		else if(paramDec.getTypeName().isType(TypeName.FILE)){
			//mv.visitVarInsn(ALOAD,0);
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);

			mv.visitVarInsn(ALOAD, 1);
		    mv.visitLdcInsn(index++);
		    mv.visitInsn(AALOAD);

			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitFieldInsn(PUTSTATIC, className, paramDec.getIdent().getText(), "Ljava/io/File;");

		}
		return null;

	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.getE().visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
        for(Expression e:tuple.getExprList()){
        	e.visit(this, arg);
        }
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		//TODO Implement this
        Label GUARD = new Label();
        mv.visitJumpInsn(GOTO, GUARD);
        Label BODY = new Label();
        mv.visitLabel(BODY);
        whileStatement.getB().visit(this, arg);
        mv.visitLabel(GUARD);
        whileStatement.getE().visit(this, arg);
        mv.visitJumpInsn(IFNE, BODY);
		return null;
	}

}
