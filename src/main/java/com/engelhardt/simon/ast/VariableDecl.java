package com.engelhardt.simon.ast;


import com.engelhardt.simon.visitor.Visitor;

public class VariableDecl extends AST {
    public String varName;
    public String type;
    public AST expr;
    public boolean mutable;
    public boolean global;


    public VariableDecl(int line, int column, String varName, String type, AST expr, boolean mutable, boolean global) {
        super(line, column);
        this.varName = varName;
        this.type = type;
        this.expr = expr;
        this.mutable = mutable;
        this.global = global;
    }


    @Override
    public void welcome(Visitor visitor) {
        visitor.visit(this);
    }
}
