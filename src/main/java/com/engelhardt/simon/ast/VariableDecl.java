package com.engelhardt.simon.ast;


import com.engelhardt.simon.visitor.Visitor;

public class VariableDecl extends AST {
    public String varName;
    public String type;
    public AST statement;
    public boolean mutable;


    public VariableDecl(int line, int column, String varName, String type, AST statement, boolean mutable) {
        super(line, column);
        this.varName = varName;
        this.type = type;
        this.statement = statement;
        this.mutable = mutable;
    }


    @Override
    public void welcome(Visitor visitor) {
        visitor.visit(this);
    }
}
