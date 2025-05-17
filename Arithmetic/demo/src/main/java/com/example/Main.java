package com.example;

import com.example.Arithmetic.io.FileCompressor;
import com.example.Arithmetic.io.FileDecompressor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        // Hardcoded file paths
//        File inputFile = new File("/home/abdelrahman/IdeaProjects/CompressionAlgorithms/Arithmetic/demo/input/download.png");
        File inputFile = new File("/home/abdelrahman/IdeaProjects/CompressionAlgorithms/Arithmetic/demo/input/sample.txt");         // Replace with your input file path
        File parentDir = inputFile.getParentFile();
        if (parentDir == null) {
            parentDir = new File("."); // current directory if no parent
        }

        // Construct output files in the same directory as input
        File compressedFile = new File(parentDir, inputFile.getName() + ".compressed");
        File decompressedFile = new File(parentDir, "decompressed_" + inputFile.getName());


        try {
            // Read original data size
            long inputSize = inputFile.length();

            // Compress
            FileCompressor compressor = new FileCompressor();
            compressor.compress(inputFile, compressedFile);

            // Decompress (need to pass original size)
            FileDecompressor decompressor = new FileDecompressor();
            decompressor.decompress(compressedFile, decompressedFile);

            // Verify decompressed matches original
            byte[] originalBytes = Files.readAllBytes(inputFile.toPath());
            byte[] decompressedBytes = Files.readAllBytes(decompressedFile.toPath());

            boolean isMatch = Arrays.equals(originalBytes, decompressedBytes);

            // Print stats
            long compressedSize = compressedFile.length();
            double ratio = (double) compressedSize / inputSize;

            System.out.println("Input file size:      " + inputSize + " bytes");
            System.out.println("Compressed file size: " + compressedSize + " bytes");
            System.out.printf("Compression ratio:    %.3f\n", ratio);
            System.out.println("Decompression match:  " + (isMatch ? "SUCCESS" : "FAILURE"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}