package com.engelhardt.simon.ast;

import com.engelhardt.simon.visitor.Visitor;

public class BooleanLiteral extends AST{
    public boolean b;

    public BooleanLiteral(int l, int c, boolean b) {
        super(l, c);
        this.b = b;
    }

    @Override
    public void welcome(Visitor visitor) {
        visitor.visit(this);
    }
}
