package com.engelhardt.simon.ast;

import com.engelhardt.simon.visitor.Visitor;

public class WhileStatement extends AST {
    public AST condition;
    public Block block;

    WhileStatement(int line, int column, AST condition, Block block) {
        super(line, column);
        this.condition = condition;
        this.block = block;
    }

    @Override
    public void welcome(Visitor visitor) {
        visitor.visit(this);
    }
}
