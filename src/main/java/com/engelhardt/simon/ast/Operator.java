package com.engelhardt.simon.ast;

public enum Operator {
    add("+"), mult("*"), sub("-"), div("/"),
    mod("%"), eq("=="), neq("!="), gt(">"), gteq(">="),
    lt("<"), lteq("<="), and("&&"), or("||");
    public final String image;

    Operator(String image) {
        this.image = image;
    }

    public boolean isArithmetic() {
        return switch (this) {
            case add, mult, sub, div, mod -> true;
            default -> false;
        };
    }

    public boolean isLogical() {
        return switch (this) {
            case or, and -> true;
            default -> false;
        };
    }

    public boolean isComparison() {
        return switch (this) {
            case eq, neq, lt, lteq, gt, gteq -> true;
            default -> false;
        };
    }
}
