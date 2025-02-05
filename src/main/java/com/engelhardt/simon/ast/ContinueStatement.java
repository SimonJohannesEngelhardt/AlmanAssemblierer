package com.engelhardt.simon.ast;

import com.engelhardt.simon.visitor.Visitor;

public class ContinueStatement extends AST {
    public ContinueStatement(int line, int column) {
        super(line, column);
    }

    @Override
    public void welcome(Visitor visitor) {
        visitor.visit(this);
    }
}
