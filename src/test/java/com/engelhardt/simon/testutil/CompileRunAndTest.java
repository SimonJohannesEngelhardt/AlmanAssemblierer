package com.engelhardt.simon.testutil;

import com.engelhardt.simon.Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompileRunAndTest {
    public static void testDirectory(String folder) throws IOException, InterruptedException {
        File almnDir = new File(STR."src/test/resources/\{folder}");
        File[] almnFiles = almnDir.listFiles(((_, name) -> name.endsWith(".almn")));

        if (almnFiles != null) {
            for (File almnFile : almnFiles) {
                String baseName = almnFile.getName().replace(".almn", "");
                String outputDirectoryAndName = STR."\{almnDir}/\{baseName}";
                String expectedOutput = new String(Files.readAllBytes(Paths.get(STR."\{outputDirectoryAndName}.exp")));


                // Compile the .almn file
                Main.main(new String[]{almnFile.getPath(), "--output-type", "asm", "--no-pretty-printer"});

                // Compile the assembly code with helper.c
                Process gccProcess = new ProcessBuilder("arch", "-x86_64", "gcc", STR."\{outputDirectoryAndName}.s", STR."\{outputDirectoryAndName}.c", "-o", outputDirectoryAndName).start();
                if (gccProcess.waitFor() != 0) {
                    throw new RuntimeException("Couldn't compile");
                }


                // Run the compiled output
                ProcessBuilder pb = new ProcessBuilder(STR."\{almnDir}/\{baseName}");
                pb.redirectErrorStream(true);
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }

                int exitCode = process.waitFor();

                assertEquals(0, exitCode);
                assertEquals(expectedOutput, output.toString());
            }
        }
    }
}
