package com.engelhardt.simon.ast;



import com.engelhardt.simon.utils.Parameter;
import com.engelhardt.simon.visitor.Visitor;

import java.util.List;

public class FunctionDefinition extends AST {
    public String name;
    public Block block;
    public String resultType;
    public List<Parameter> parameters;

    public FunctionDefinition(int line, int column, String name, String resultType, Block block, List<Parameter> parameters) {
        super(line, column);
        this.name = name;
        this.parameters = parameters;
        this.block = block;
        this.resultType = resultType;
    }


    @Override
    public void welcome(Visitor visitor) {
        visitor.visit(this);
    }
}
