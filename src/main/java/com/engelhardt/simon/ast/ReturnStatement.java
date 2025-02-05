package com.engelhardt.simon.ast;


import com.engelhardt.simon.visitor.Visitor;

public class ReturnStatement extends AST {
    public AST expr;

    public ReturnStatement(int line, int column, AST expr) {
        super(line, column);
        this.expr = expr;
    }


    @Override
    public void welcome(Visitor visitor) {
        visitor.visit(this);
    }
}
