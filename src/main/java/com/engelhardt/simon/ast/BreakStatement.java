package com.engelhardt.simon.ast;

import com.engelhardt.simon.visitor.Visitor;

public class BreakStatement extends AST {
    public BreakStatement(int line, int column) {
        super(line, column);
    }

    @Override
    public void welcome(Visitor visitor) {
        visitor.visit(this);
    }
}
