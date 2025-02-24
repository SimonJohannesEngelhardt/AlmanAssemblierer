package com.engelhardt.simon.ast;

import com.engelhardt.simon.visitor.Visitor;


public class OpExpr extends AST {
    public AST left;
    public AST right;
    public Operator operator;

    public OpExpr(int line, int colum, AST left, AST right, Operator operator) {
        super(line, colum);
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public void welcome(Visitor visitor) {
        visitor.visit(this);
    }

}
