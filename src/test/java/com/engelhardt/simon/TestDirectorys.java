package com.engelhardt.simon;

import com.engelhardt.simon.testutil.CompileRunAndTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestDirectorys {
    @Test
    public void testDirectorys() throws IOException, InterruptedException {
        CompileRunAndTest.testDirectory("maintest");
        CompileRunAndTest.testDirectory("operators");
        CompileRunAndTest.testDirectory("boolean");
        CompileRunAndTest.testDirectory("fizzbuzz");
        CompileRunAndTest.testDirectory("print");
    }
}
