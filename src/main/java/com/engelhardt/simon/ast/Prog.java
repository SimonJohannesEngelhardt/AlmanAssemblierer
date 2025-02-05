package com.engelhardt.simon.ast;


import com.engelhardt.simon.visitor.Visitor;

import java.util.List;

public class Prog extends AST {
    public List<VariableDecl> variableDecls;
    public List<FunctionDefinition> functionDefinitions;
    public List<AST> statements;

    public Prog(
            int line,
            int column,
            List<VariableDecl> variableDecls,
            List<FunctionDefinition> functionDefinitions,
            List<AST> statements
    ) {
        super(line, column);
        this.variableDecls = variableDecls;
        this.functionDefinitions = functionDefinitions;
        this.statements = statements;
    }


    @Override
    public void welcome(Visitor visitor) {
        visitor.visit(this);
    }
}
