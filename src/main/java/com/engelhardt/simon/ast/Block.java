package com.engelhardt.simon.ast;
import com.engelhardt.simon.visitor.Visitor;

import java.util.List;

public class Block extends AST {
    public List<AST> statements;
    public Block(int line, int column, List<AST> statements) {
        super(line, column);
        this.statements = statements;
    }

    @Override
    public void welcome(Visitor visitor) {
        visitor.visit(this);
    }
}
