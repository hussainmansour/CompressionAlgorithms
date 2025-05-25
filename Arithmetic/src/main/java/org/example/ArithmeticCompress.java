package org.example;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;

public class ArithmeticCompress {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the path to the file to compress: ");
        String inputFilePath = args[0];
        File inputFile = new File(inputFilePath);

        if (!inputFile.exists() || !inputFile.isFile()) {
            System.err.println("Input file does not exist or is not a file.");
            return;
        }

        // Determine the directory where the JAR is being run
        // String jarDirPath = new
        // File(System.getProperty("user.dir")).getAbsolutePath();
        // String outputDirPath = jarDirPath + File.separator + "arithmetic";
        String outputDirPath = "arithmetic";
        File outputDir = new File(outputDirPath);

        // Create the "arithmetic" directory if it does not exist
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                System.err.println("Failed to create output directory: " + outputDirPath);
                return;
            }
        } else if (!outputDir.isDirectory()) {
            System.err.println("A file named 'arithmetic' exists but is not a directory.");
            return;
        }

        File outputFile = new File(outputDir, inputFile.getName() + ".ac");

        long originalSize = inputFile.length();
        long startTime = System.nanoTime();

        FrequencyTable freqs = getFrequencies(inputFile);
        freqs.increment(256); // EOF symbol

        try (InputStream in = new BufferedInputStream(new FileInputStream(inputFile));
                BitOutputStream out = new BitOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
            writeFrequencies(out, freqs);
            compress(freqs, in, out);
        }

        long endTime = System.nanoTime();
        long compressedSize = outputFile.length();
        double compressionRatio = (originalSize == 0) ? 0 : (double) compressedSize / originalSize;
        double timeInSeconds = (endTime - startTime) / 1_000_000_000.0;

        // Print statistics
        System.out.println("Compression complete. Compressed file saved at: " + outputFile.getAbsolutePath());
        System.out.println("----- Compression Statistics -----");
        System.out.println("Original size     : " + originalSize + " bytes");
        System.out.println("Compressed size   : " + compressedSize + " bytes");
        System.out.printf("Compression ratio : %.4f%n", compressionRatio);
        System.out.printf("Time taken        : %.4f seconds%n", timeInSeconds);
        ArithmeticDecompress.decompressFile(outputFile.getAbsolutePath());
        // if(areFilesIdenticalBySHA(outputFile.getAbsolutePath(), decompressedPath))
        // System.out.println("The same file");
        // else
        // System.out.println("Not the same file");
    }

    // private static boolean areFilesIdenticalBySHA(String path1, String path2)
    // throws IOException, NoSuchAlgorithmException {
    // byte[] hash1 = computeSHA256(new File(path1));
    // byte[] hash2 = computeSHA256(new File(path2));
    // return Arrays.equals(hash1, hash2);
    // }

    // private static byte[] computeSHA256(File file) throws IOException,
    // NoSuchAlgorithmException {
    // MessageDigest digest = MessageDigest.getInstance("SHA-256");
    // try (FileInputStream fis = new FileInputStream(file)) {
    // byte[] buffer = new byte[8192];
    // int bytesRead;
    // while ((bytesRead = fis.read(buffer)) != -1) {
    // digest.update(buffer, 0, bytesRead);
    // }
    // }
    // return digest.digest();
    // }

    // Returns a frequency table based on the bytes in the given file.
    // Also contains an extra entry for symbol 256, whose frequency is set to 0.
    private static FrequencyTable getFrequencies(File file) throws IOException {
        FrequencyTable freqs = new SimpleFrequencyTable(new int[257]);
        try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
            while (true) {
                int b = input.read();
                if (b == -1)
                    break;
                freqs.increment(b);
            }
        }
        return freqs;
    }

    // To allow unit testing, this method is package-private instead of private.
    static void writeFrequencies(BitOutputStream out, FrequencyTable freqs) throws IOException {
        for (int i = 0; i < 256; i++)
            writeInt(out, 32, freqs.get(i));
    }

    // To allow unit testing, this method is package-private instead of private.
    static void compress(FrequencyTable freqs, InputStream in, BitOutputStream out) throws IOException {
        ArithmeticEncoder enc = new ArithmeticEncoder(32, out);
        while (true) {
            int symbol = in.read();
            if (symbol == -1)
                break;
            enc.write(freqs, symbol);
        }
        enc.write(freqs, 256); // EOF
        enc.finish(); // Flush remaining code bits
    }

    // Writes an unsigned integer of the given bit width to the given stream.
    private static void writeInt(BitOutputStream out, int numBits, int value) throws IOException {
        if (numBits < 0 || numBits > 32)
            throw new IllegalArgumentException();

        for (int i = numBits - 1; i >= 0; i--)
            out.write((value >>> i) & 1); // Big endian
    }

}