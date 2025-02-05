package com.engelhardt.simon.ast;


import com.engelhardt.simon.visitor.Visitor;

public interface Visitable {
    void welcome(Visitor visitor);
}

