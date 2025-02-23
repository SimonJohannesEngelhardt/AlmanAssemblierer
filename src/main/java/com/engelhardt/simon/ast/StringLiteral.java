package com.engelhardt.simon.ast;

import com.engelhardt.simon.visitor.Visitor;

public class StringLiteral  extends AST {
    public String s;
    public String id;

    public StringLiteral(int l, int c, String s) {
        super(l, c);
        this.s = s;
    }


    @Override
    public void welcome(Visitor visitor) {
        visitor.visit(this);
    }
}
