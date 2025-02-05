package com.engelhardt.simon.ast;


import com.engelhardt.simon.visitor.Visitor;

public class Variable extends AST {
    public String name;

    public Variable(int line, int column, String name) {
        super(line, column);
        this.name = name;
    }


    @Override
    public void welcome(Visitor visitor) {
        visitor.visit(this);
    }
}
