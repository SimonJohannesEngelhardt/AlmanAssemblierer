package com.engelhardt.simon;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AlmanCompilerTest {
    @Test
    public void testAlmnFiles() throws IOException, InterruptedException {
        String testPath = "src/test/resources";
        File almnDir = new File(testPath + "/almn");
        File[] almnFiles = almnDir.listFiles(((dir, name) -> name.endsWith(".almn")));

        if (almnFiles != null) {
            for (File almnFile : almnFiles) {
                String baseName = almnFile.getName().replace(".almn", "");
                File expectedFile = new File(almnDir, baseName + ".expected");

                // Compile the .almn file
                Process compileProcess = new ProcessBuilder("java", "-jar", "build/libs/Alman_Assemblierer-1.0-SNAPSHOT.jar", almnFile.getPath(), "--output-type", "asm").start();
                compileProcess.waitFor();

                // Compile the assembly code with helper.c
                Process gccProcess = new ProcessBuilder("gcc", "-o", testPath + "/generated/output", testPath + "/C-testfiles/output.s", testPath + "/test1/helper.c").start();
                gccProcess.waitFor();


                // Run the compiled output
                Process runProcess = new ProcessBuilder("./output").start();
                BufferedReader outputReader = new BufferedReader(new FileReader("output.txt"));
                BufferedReader expectedReader = new BufferedReader(new FileReader(expectedFile));

                String outputLine;
                String expectedLine;
                while ((outputLine = outputReader.readLine()) != null && (expectedLine = expectedReader.readLine()) != null) {
                    assertEquals(expectedLine, outputLine);
                }

                outputReader.close();
                expectedReader.close();
            }
        }
    }
}
