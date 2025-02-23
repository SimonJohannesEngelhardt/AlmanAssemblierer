package com.engelhardt.simon.visitor;

import com.engelhardt.simon.ast.*;

public class EvalVisitor implements Visitor {
    public long result;

    @Override
    public void visit(IntLiteral intLiteral) {
        result = intLiteral.n;
    }

    @Override
    public void visit(OpExpr opExpr) {
        opExpr.left.welcome(this);
        var l = result;
        opExpr.right.welcome(this);
        var r = result;
        switch (opExpr.operator) {
            case add -> result = l + r;
            case sub -> result = l - r;
            case mult -> result = l * r;
            case div -> result = l / r;
            case mod -> result = l % r;
            default -> throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public void visit(Variable variable) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(FunctionDefinition functionDefinition) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(VariableDecl variableDecl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(Prog prog) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(Block block) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(FunctionCall functionCall) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(ContinueStatement continueStatement) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(BreakStatement breakStatement) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(VarAssignment varAssignment) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
