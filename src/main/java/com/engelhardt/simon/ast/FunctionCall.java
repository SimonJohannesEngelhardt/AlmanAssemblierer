package com.engelhardt.simon.ast;


import com.engelhardt.simon.visitor.Visitor;

import java.util.List;

public class FunctionCall extends AST {
    public String functionName;
    public List<AST> args;

    public FunctionCall(int line, int column, String functionName, List<AST> args) {
        super(line, column);
        this.functionName = functionName;
        this.args = args;
    }

    @Override
    public void welcome(Visitor visitor) {
        visitor.visit(this);
    }
}
