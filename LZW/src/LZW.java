import java.io.*;
import java.util.*;

public class LZW {

    private static final int BIT_WIDTH = 12;
    private static final int MAX_DICT_SIZE = 1 << BIT_WIDTH;

    private static int dictSz = 0;

    public static void main(String[] args) throws IOException {
        if (args.length != 2 || !(args[0].equals("c") || args[0].equals("d"))) {
            System.err.println("Usage: java LZW c|d <inputFile>");
            System.exit(1);
        }

        String mode = args[0];
        String inputPath = args[1];

        File outDir = new File("LZW");
        if (!outDir.exists())
            outDir.mkdirs();

        if (mode.equals("c"))
            compress(inputPath);
        else
            decompress(inputPath);
    }

    private static void compress(String inputPath) throws IOException {
        long startTime = System.nanoTime();

        File inputFile = new File(inputPath);
        byte[] input = readAllBytes(inputPath);
        byte[] comp = encode(input);

        File parentDir = inputFile.getParentFile();
        File lzwDir = new File(parentDir, "LZW");
        if (!lzwDir.exists())
            lzwDir.mkdirs();

        String outputFileName = inputFile.getName() + ".lzw";
        File outputFile = new File(lzwDir, outputFileName);
        writeAllBytes(outputFile.getAbsolutePath(), comp);

        long endTime = System.nanoTime();
        double durationSec = (endTime - startTime) / 1_000_000_000.0;

        int originalSize = input.length;
        int compressedSize = comp.length;

        double originalSizeMB = originalSize / (1024.0 * 1024.0);
        double compressedSizeMB = compressedSize / (1024.0 * 1024.0);
        double compressionRatio = ((double) compressedSize / originalSize) * 100;

        System.out.println("Original file name : " + inputFile.getName());
        System.out.println("Type: compression");
        System.out.printf("Original size (MB): %.6f%n", originalSizeMB);
        System.out.printf("Compressed size (MB): %.6f%n", compressedSizeMB);
        System.out.printf("Compression/decompression time (S): %.6f%n", durationSec);
        System.out.printf("Compression ratio: %.2f%%\n", compressionRatio);
        System.out.println("Dictionary size: " + dictSz);
        System.out.println("-----------------------------------------------------");
    }

    private static void decompress(String inputPath) throws IOException {
        long startTime = System.nanoTime();

        File compressedFile = new File(inputPath);
        byte[] compressedData = readAllBytes(inputPath);
        if (compressedData.length == 0)
            throw new IOException("Empty input");

        byte[] decompressedData = decode(compressedData);

        String compressedFileName = compressedFile.getName();
        String originalFileName = compressedFileName.endsWith(".lzw")
                ? compressedFileName.substring(0, compressedFileName.length() - 4)
                : compressedFileName + ".out";

        // Write decompressed file to same directory as the .lzw input
        File lzwDir = compressedFile.getParentFile();
        File decompressedFile = new File(lzwDir, originalFileName);
        writeAllBytes(decompressedFile.getAbsolutePath(), decompressedData);

        long endTime = System.nanoTime();
        double durationSec = (endTime - startTime) / 1_000_000_000.0;

        // Locate original file one level above
        File parentDir = lzwDir.getParentFile();
        File originalFile = new File(parentDir, originalFileName);

        // Compute and compare SHA-256
        String shaOriginal = sha256(originalFile);
        String shaDecompressed = sha256(decompressedFile);

        System.out.println("Original file name : " + compressedFileName);
        System.out.println("Type: decompression");
        System.out.printf("Compression/decompression time (S): %.6f%n", durationSec);
        System.out.println("-----------------------------------------------------");
        System.out.println("SHA-256 original    : " + shaOriginal);
        System.out.println("SHA-256 decompressed: " + shaDecompressed);
        System.out.println("Match: " + shaOriginal.equals(shaDecompressed));
        System.out.println("-----------------------------------------------------");
    }

    // encoder

    private static byte[] encode(byte[] inputBytes) throws IOException {
        Map<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(String.valueOf((char) i), i);
        }
        int nextCode = 256;

        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
        BitOutputStream bitOut = new BitOutputStream(byteArrayOut);

        String currentStr = "";

        for (byte inputByte : inputBytes) {
            char currChar = (char) (inputByte & 0xFF);
            String combined = currentStr + currChar;

            if (dictionary.containsKey(combined)) {
                currentStr = combined;
            } else {
                bitOut.writeBits(dictionary.get(currentStr), BIT_WIDTH);
                if (nextCode < MAX_DICT_SIZE) {
                    dictionary.put(combined, nextCode++);
                }
                currentStr = String.valueOf(currChar);
            }
        }
        if (!currentStr.isEmpty()) {
            bitOut.writeBits(dictionary.get(currentStr), BIT_WIDTH);
        }
        bitOut.flush();

        dictSz = nextCode;
        return byteArrayOut.toByteArray();
    }

    // decoder

    private static byte[] decode(byte[] compressedBytes) throws IOException {
        Map<Integer, String> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(i, String.valueOf((char) i));
        }
        int nextCode = 256;

        ByteArrayInputStream byteArrayIn = new ByteArrayInputStream(compressedBytes);
        BitInputStream bitIn = new BitInputStream(byteArrayIn);

        Integer firstCode = bitIn.readBits(BIT_WIDTH);
        if (firstCode == null)
            return new byte[0];

        String currentStr = dictionary.get(firstCode);
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        outputBuffer.write(currentStr.getBytes("ISO-8859-1"));

        Integer nextCodeValue;
        while ((nextCodeValue = bitIn.readBits(BIT_WIDTH)) != null) {
            String nextStr;
            if (dictionary.containsKey(nextCodeValue)) {
                nextStr = dictionary.get(nextCodeValue);
            } else if (nextCodeValue == nextCode) {
                nextStr = currentStr + currentStr.charAt(0);
            } else {
                throw new IOException("Invalid LZW code: " + nextCodeValue);
            }

            outputBuffer.write(nextStr.getBytes("ISO-8859-1"));

            if (nextCode < MAX_DICT_SIZE) {
                dictionary.put(nextCode++, currentStr + nextStr.charAt(0));
            }

            currentStr = nextStr;
        }

        return outputBuffer.toByteArray();
    }

    private static byte[] readAllBytes(String path) throws IOException {
        try (InputStream in = new FileInputStream(path);
                ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            byte[] tmp = new byte[4096];
            int r;
            while ((r = in.read(tmp)) != -1)
                buf.write(tmp, 0, r);
            return buf.toByteArray();
        }
    }

    private static void writeAllBytes(String path, byte[] data) throws IOException {
        try (OutputStream out = new FileOutputStream(path)) {
            out.write(data);
        }
    }

    private static class BitOutputStream {
        private final OutputStream out;
        private int currentByte = 0, numBitsFilled = 0;

        BitOutputStream(OutputStream os) {
            out = os;
        }

        void writeBits(int b, int len) throws IOException {
            for (int i = len - 1; i >= 0; i--) {
                currentByte = (currentByte << 1) | ((b >> i) & 1);
                if (++numBitsFilled == 8) {
                    out.write(currentByte);
                    numBitsFilled = 0;
                    currentByte = 0;
                }
            }
        }

        void flush() throws IOException {
            if (numBitsFilled > 0) {
                currentByte <<= (8 - numBitsFilled);
                out.write(currentByte);
            }
            out.flush();
        }
    }

    private static class BitInputStream {
        private final InputStream in;
        private int currentByte = 0, numBitsRemaining = 0;

        BitInputStream(InputStream is) {
            in = is;
        }

        private int readBit() throws IOException {
            if (numBitsRemaining == 0) {
                currentByte = in.read();
                if (currentByte == -1)
                    return -1;
                numBitsRemaining = 8;
            }
            numBitsRemaining--;
            return (currentByte >> numBitsRemaining) & 1;
        }

        Integer readBits(int len) throws IOException {
            int val = 0;
            for (int i = 0; i < len; i++) {
                int bit = readBit();
                if (bit < 0)
                    return null;
                val = (val << 1) | bit;
            }
            return val;
        }
    }

    // SHA-256 utility
    private static String sha256(File file) throws IOException {
    try (InputStream fis = new FileInputStream(file)) {
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[4096];
        int n;
        while ((n = fis.read(buffer)) != -1) {
            digest.update(buffer, 0, n);
        }
        byte[] hash = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    } catch (Exception e) {
        throw new IOException("Could not compute SHA-256: " + e.getMessage(), e);
    }
}

}
