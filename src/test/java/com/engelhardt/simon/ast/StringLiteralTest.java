package com.engelhardt.simon.ast;

import com.engelhardt.simon.visitor.Visitor;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class StringLiteralTest {
    @Test
    public void testConstructor() {
        StringLiteral stringLiteral = new StringLiteral(1, 1, "Hello, World!");
        assert stringLiteral.line == 1;
        assert stringLiteral.column == 1;
        assert stringLiteral.s.equals("Hello, World!");
    }

    @Test
    public void testWelcome() {
        StringLiteral stringLiteral = new StringLiteral(1, 1, "Hello, World!");
        Visitor mockedVisitor = mock(Visitor.class);
        stringLiteral.welcome(mockedVisitor);
        verify(mockedVisitor).visit(stringLiteral);
    }
}
