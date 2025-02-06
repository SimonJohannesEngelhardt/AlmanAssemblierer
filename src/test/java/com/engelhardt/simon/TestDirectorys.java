package com.engelhardt.simon;

import com.engelhardt.simon.testutil.CompileRunAndTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestDirectorys {
    @Test
    public void testGod() throws IOException, InterruptedException {
        CompileRunAndTest.testDirectory("god");
    }
}
