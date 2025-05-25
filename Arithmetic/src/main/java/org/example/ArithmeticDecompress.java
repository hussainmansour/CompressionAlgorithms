package org.example;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;

public class ArithmeticDecompress {

    public static void decompressFile(String args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        // 1. Read & validate input file
        // System.out.print("Enter the path to the file to decompress: ");
        String inputFilePath = args;
        File inputFile = new File(inputFilePath);
        if (!inputFile.exists() || !inputFile.isFile()) {
            System.err.println("Error: Input file does not exist or is not a file.");
            return;
        }

        // 2. Read & validate output directory
        // System.out.print("Enter the path to the directory to store the decompressed
        // file: ");
        String parentPath = inputFile.getParent();
        String outputDirPath = parentPath;
        File outputDir = new File(outputDirPath);
        if (!outputDir.exists() || !outputDir.isDirectory()) {
            System.err.println("Error: Output path is not a valid directory.");
            return;
        }

        // 3. Determine output file name: strip “.ac” if present, otherwise append
        // “.dec”
        String baseName = inputFile.getName();
        if (baseName.endsWith(".ac")) {
            baseName = baseName.substring(0, baseName.length() - 3);
        }
        File outputFile = new File(outputDir, baseName);

        // ---- capture stats before decompressing ----
        long compressedSize = inputFile.length();
        long startTime = System.nanoTime();

        // 4. Perform decompression
        try (BitInputStream in = new BitInputStream(
                new BufferedInputStream(new FileInputStream(inputFile)));
                OutputStream out = new BufferedOutputStream(
                        new FileOutputStream(outputFile))) {

            FrequencyTable freqs = readFrequencies(in);
            decompress(freqs, in, out);
        }

        long endTime = System.nanoTime();
        long decompressedSize = outputFile.length();
        double expansionRatio = (compressedSize == 0)
                ? 0
                : (double) decompressedSize / compressedSize;
        double timeInSeconds = (endTime - startTime) / 1_000_000_000.0;

        // ---- print summary ----
        System.out.println("Decompression complete. Output saved to: "
                + outputFile.getAbsolutePath());
        System.out.println("----- Decompression Statistics -----");
        System.out.println("Compressed size      : " + compressedSize + " bytes");
        System.out.println("Decompressed size    : " + decompressedSize + " bytes");
        System.out.printf("Expansion ratio      : %.4f%n", expansionRatio);
        System.out.printf("Time taken           : %.4f seconds%n", timeInSeconds);
        // return outputFile.getAbsolutePath();
    }

    // Reads the frequency table header from the compressed bit-stream
    static FrequencyTable readFrequencies(BitInputStream in) throws IOException {
        int[] freqs = new int[257];
        for (int i = 0; i < 256; i++) {
            freqs[i] = readInt(in, 32);
        }
        freqs[256] = 1; // EOF symbol
        return new SimpleFrequencyTable(freqs);
    }

    // Decode symbols until EOF and write to the OutputStream
    static void decompress(FrequencyTable freqs, BitInputStream in, OutputStream out)
            throws IOException {
        ArithmeticDecoder dec = new ArithmeticDecoder(32, in);
        while (true) {
            int symbol = dec.read(freqs);
            if (symbol == 256) // EOF
                break;
            out.write(symbol);
        }
    }

    // Read an unsigned integer of given bit width (big-endian)
    private static int readInt(BitInputStream in, int numBits) throws IOException {
        if (numBits < 0 || numBits > 32)
            throw new IllegalArgumentException();
        int result = 0;
        for (int i = 0; i < numBits; i++) {
            result = (result << 1) | in.readNoEof();
        }
        return result;
    }
}
