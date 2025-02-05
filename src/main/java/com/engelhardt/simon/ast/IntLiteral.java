package com.engelhardt.simon.ast;


import com.engelhardt.simon.visitor.Visitor;

public class IntLiteral extends AST {
    public long n;

    public IntLiteral(int l, int c, long n) {
        super(l, c);
        this.n = n;
    }


    @Override
    public void welcome(Visitor visitor) {
        visitor.visit(this);
    }
}
