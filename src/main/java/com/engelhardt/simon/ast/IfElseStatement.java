package com.engelhardt.simon.ast;


import com.engelhardt.simon.visitor.Visitor;

public class IfElseStatement extends AST {
    public AST ifCondition;
    public Block ifBlock;
    public Block elseBlock;

    public IfElseStatement(int line, int column, AST ifCondition, Block ifBlock, Block elseBlock) {
        super(line, column);
        this.ifCondition = ifCondition;
        this.ifBlock = ifBlock;
        this.elseBlock = elseBlock;
    }

    public IfElseStatement(int line, int column, AST ifCondition, Block ifBlock) {
        this(line, column, ifCondition, ifBlock, null);
    }

    @Override
    public void welcome(Visitor visitor) {
        visitor.visit(this);
    }
}
