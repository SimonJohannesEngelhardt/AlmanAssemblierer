package com.engelhardt.simon.visitor;

import com.engelhardt.simon.ast.*;

public class MarkTailCall implements Visitor {
    FunctionDefinition currentFunctionDefinition;

    @Override
    public void visit(IntLiteral intLiteral) {

    }

    @Override
    public void visit(OpExpr opExpr) {

    }

    @Override
    public void visit(Variable variable) {

    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        if (returnStatement.expr instanceof FunctionCall fc) {
            if (fc.functionName.equals(currentFunctionDefinition.name)) {
                fc.attribute.isTailCall = true;
                currentFunctionDefinition.attribute.hasTailCall = true;
                System.out.println(currentFunctionDefinition.name + " has tail call");

            }
        }
    }

    @Override
    public void visit(FunctionDefinition functionDefinition) {
        currentFunctionDefinition = functionDefinition;
        functionDefinition.block.welcome(this);
    }

    @Override
    public void visit(VariableDecl variableDecl) {

    }

    @Override
    public void visit(Prog prog) {

    }

    @Override
    public void visit(Block block) {
        block.statements.forEach(statement -> statement.welcome(this));
    }

    @Override
    public void visit(FunctionCall functionCall) {

    }

    @Override
    public void visit(WhileStatement whileStatement) {

    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {
        ifElseStatement.ifBlock.welcome(this);
        ifElseStatement.elseifBlocks.forEach(block -> block.welcome(this));
        if (ifElseStatement.elseBlock != null) {
            ifElseStatement.elseBlock.welcome(this);
        }
    }

    @Override
    public void visit(ContinueStatement continueStatement) {

    }

    @Override
    public void visit(BreakStatement breakStatement) {

    }

    @Override
    public void visit(VarAssignment varAssignment) {

    }
}
