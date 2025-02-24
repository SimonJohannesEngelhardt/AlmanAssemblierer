package com.engelhardt.simon.ast;

import com.engelhardt.simon.visitor.Visitor;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class IntLiteralTest {
    @Test
    public void testConstructor() {
        IntLiteral intLiteral = new IntLiteral(1, 1, 42);
        assert intLiteral.line == 1;
        assert intLiteral.column == 1;
        assert intLiteral.n == 42;
    }

    @Test
    public void testWelcome() {
        IntLiteral intLiteral = new IntLiteral(1, 1, 42);
        Visitor mockedVisitor = mock(Visitor.class);
        intLiteral.welcome(mockedVisitor);
        verify(mockedVisitor).visit(intLiteral);
    }
}
