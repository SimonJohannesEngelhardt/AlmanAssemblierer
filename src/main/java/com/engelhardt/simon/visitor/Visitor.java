package com.engelhardt.simon.visitor;

import com.engelhardt.simon.ast.*;

public interface Visitor {
    void visit(IntLiteral intLiteral);

    void visit(OpExpr opExpr);

    void visit(Variable variable);

    void visit(ReturnStatement returnStatement);

    void visit(FunctionDefinition functionDefinition);

    void visit(VariableDecl variableDecl);

    void visit(Prog prog);

    void visit(Block block);

    void visit(FunctionCall functionCall);

    void visit(WhileStatement whileStatement);

    void visit(IfElseStatement ifElseStatement);

    void visit(ContinueStatement continueStatement);

    void visit(BreakStatement breakStatement);

    void visit(VarAssignment varAssignment);
    
    default void reportError(int line, int column, String message) {
        System.err.println(STR."Error at (\{line},\{column}): \{message}");
    }
}
