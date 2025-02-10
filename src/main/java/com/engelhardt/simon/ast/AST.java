package com.engelhardt.simon.ast;

import com.engelhardt.simon.utils.Attribute;
import com.engelhardt.simon.utils.Type;

/**
 * Abstract class that implements a node in the ast
 */
public abstract class AST implements Visitable {
    public int line;
    public int column;
    public Type theType = null;
    public Attribute attribute = new Attribute();

    public AST(int line, int column) {
        this.line = line;
        this.column = column;
    }
}
