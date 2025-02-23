package com.engelhardt.simon.ast;

import com.engelhardt.simon.visitor.Visitor;

/**
 * Stellt eine Variablenzuweisung dar. Es ist explizit keine Deklaration. Eine bereits
 * deklarierte Variable bekommt einen neuen Wert zugewiesen.
 */
public class VarAssignment extends AST {
    public String varName;
    public AST expr;

    public VarAssignment(int line, int column, String varName, AST expr) {
        super(line, column);
        this.varName = varName;
        this.expr = expr;
    }

    @Override
    public void welcome(Visitor visitor) {
        visitor.visit(this);
    }
}
