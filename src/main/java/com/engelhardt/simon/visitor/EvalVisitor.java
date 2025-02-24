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
            default ->
                    reportError(opExpr.line, opExpr.column, "Nicht unterstützter Operator: " + opExpr.operator);
        }
    }

    @Override
    public void visit(Variable variable) {
        throw new UnsupportedOperationException("Nicht unterstützt.");
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        throw new UnsupportedOperationException("Nicht unterstützt.");
    }

    @Override
    public void visit(FunctionDefinition functionDefinition) {
        throw new UnsupportedOperationException("Nicht unterstützt.");
    }

    @Override
    public void visit(VariableDecl variableDecl) {
        throw new UnsupportedOperationException("Nicht unterstützt.");
    }

    @Override
    public void visit(Prog prog) {
        throw new UnsupportedOperationException("Nicht unterstützt.");
    }

    @Override
    public void visit(Block block) {
        throw new UnsupportedOperationException("Nicht unterstützt.");
    }

    @Override
    public void visit(FunctionCall functionCall) {
        throw new UnsupportedOperationException("Nicht unterstützt.");
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        throw new UnsupportedOperationException("Nicht unterstützt.");
    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {
        throw new UnsupportedOperationException("Nicht unterstützt.");
    }

    @Override
    public void visit(ContinueStatement continueStatement) {
        throw new UnsupportedOperationException("Nicht unterstützt.");
    }

    @Override
    public void visit(BreakStatement breakStatement) {
        throw new UnsupportedOperationException("Nicht unterstützt.");
    }

    @Override
    public void visit(VarAssignment varAssignment) {
        throw new UnsupportedOperationException("Nicht unterstützt.");
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
        throw new UnsupportedOperationException("Nicht unterstützt.");
    }

    @Override
    public void visit(BooleanLiteral booleanLiteral) {
        result = booleanLiteral.b ? 1 : 0;
    }
}
