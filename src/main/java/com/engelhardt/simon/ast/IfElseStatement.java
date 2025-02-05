package com.engelhardt.simon.ast;


import com.engelhardt.simon.visitor.Visitor;

import java.util.List;

public class IfElseStatement extends AST {
    public AST ifCondition;
    public Block ifBlock;
    public List<AST> elseifConditions;
    public List<Block> elseifBlocks;
    public Block elseBlock;

    public IfElseStatement(int line, int column, AST ifCondition, Block ifBlock, List<AST> elseifConditions, List<Block> elseifBlocks, Block elseBlock) {
        super(line, column);
        this.ifCondition = ifCondition;
        this.ifBlock = ifBlock;
        this.elseifConditions = elseifConditions;
        this.elseifBlocks = elseifBlocks;
        this.elseBlock = elseBlock;
    }


    @Override
    public void welcome(Visitor visitor) {
        visitor.visit(this);
    }
}
