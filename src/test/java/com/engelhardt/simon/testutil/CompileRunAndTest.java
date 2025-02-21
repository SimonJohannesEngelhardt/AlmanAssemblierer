package com.engelhardt.simon.testutil;

import com.engelhardt.simon.Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompileRunAndTest {
    public static void testDirectory(String folder) throws IOException, InterruptedException {
        File almnDir = new File("src/test/resources/" + folder);
        File[] almnFiles = almnDir.listFiles(((_, name) -> name.endsWith(".almn")));
        if (almnFiles == null) {
            throw new IOException("Couldn't find files");
        }

        for (File almnFile : almnFiles) {
            String baseName = almnFile.getName().replace(".almn", "");
            String outputDirectoryAndName = almnDir + "/" + baseName;
            String expectedOutput = new String(Files.readAllBytes(Paths.get(outputDirectoryAndName + ".exp")));


            // Compile the .almn file
            Main.main(new String[]{almnFile.getPath(), "--no-pretty-printer"});

            // Compile the assembly code with helper.c
            Process gccProcess = new ProcessBuilder("arch", "-x86_64", "gcc", outputDirectoryAndName + ".s", "-o", outputDirectoryAndName).start();
            if (gccProcess.waitFor() != 0) {
                gccProcess.errorReader().lines().forEach(System.out::println);
                throw new RuntimeException("Couldn't compile");
            }


            // Run the compiled output
            ProcessBuilder pb = new ProcessBuilder(outputDirectoryAndName);
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
            var expected = expectedOutput.split("\n");
            var received = output.toString().split("\n");
            assertArrayEquals(expected, received);
        }

    }
}
