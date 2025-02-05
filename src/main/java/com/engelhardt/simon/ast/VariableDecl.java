package com.engelhardt.simon.ast;


import com.engelhardt.simon.visitor.Visitor;

public class VariableDecl extends AST {
    public String varName;
    public String type;
    public AST statement;


    public VariableDecl(int line, int column, String varName, String type, AST statement) {
        super(line, column);
        this.varName = varName;
        this.type = type;
        this.statement = statement;
    }

    public VariableDecl(int line, int column, String varName, String type) {
        this(line, column, varName, type, null);
    }

    @Override
    public void welcome(Visitor visitor) {
        visitor.visit(this);
    }
}
