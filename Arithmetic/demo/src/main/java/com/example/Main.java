package com.example;

import com.example.Arithmetic.io.FileCompressor;
import com.example.Arithmetic.io.FileDecompressor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        // Get paths relative to project root
        File projectRoot = new File(System.getProperty("user.dir"));
        File inputDir = new File(projectRoot, "input");
        File outputDir = new File(projectRoot, "output");

        // Create output directory if it doesn't exist
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        // Process all files in input directory
        File[] inputFiles = inputDir.listFiles();
        if (inputFiles == null || inputFiles.length == 0) {
            System.out.println("No files found in input directory");
            return;
        }

        FileCompressor compressor = new FileCompressor();
        FileDecompressor decompressor = new FileDecompressor();

        for (File inputFile : inputFiles) {
            if (inputFile.isDirectory()) {
                continue; // Skip subdirectories
            }

            System.out.println("\nProcessing: " + inputFile.getName());

            // Prepare output paths
            String baseName = inputFile.getName();
            File compressedFile = new File(outputDir, baseName + ".compressed");
            File decompressedFile = new File(outputDir, "decompressed_" + baseName);

            try {
                // Compression
                System.out.println("Compressing...");
                compressor.compress(inputFile, compressedFile);

                // Decompression
                System.out.println("Decompressing...");
                decompressor.decompress(compressedFile, decompressedFile);

                // Verification
                byte[] original = Files.readAllBytes(inputFile.toPath());
                byte[] decompressed = Files.readAllBytes(decompressedFile.toPath());
                boolean isMatch = Arrays.equals(original, decompressed);

                System.out.println("Verification: " + (isMatch ? "SUCCESS" : "FAILURE"));
                System.out.println("Original size: " + original.length + " bytes");
                System.out.println("Compressed size: " + compressedFile.length() + " bytes");
                System.out.printf("Compression ratio: %.2f%%\n",
                        (1 - (double)compressedFile.length()/original.length) * 100);

            } catch (IOException e) {
                System.err.println("Error processing " + inputFile.getName());
                e.printStackTrace();
            }
        }
    }
}