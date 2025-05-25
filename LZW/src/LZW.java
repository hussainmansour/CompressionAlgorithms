import java.io.*;
import java.util.*;

public class LZW {

    private static final int BIT_WIDTH     = 12;         
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
        if (!outDir.exists()) outDir.mkdirs();

        if (mode.equals("c"))
            compress(inputPath);
        else
            decompress(inputPath);
    }


    private static void compress(String inputPath) throws IOException {
        byte[] input = readAllBytes(inputPath);
        byte[] comp  = encode(input);

        byte[] out = new byte[comp.length + 1];
        out[0] = 1;                           // mark as LZW-compressed
        System.arraycopy(comp, 0, out, 1, comp.length);

        String name = new File(inputPath).getName() + ".lzw";
        writeAllBytes("LZW/" + name, out);

        System.out.println("Written: LZW/" + name);
        System.out.println("Dictionary size: " + dictSz);
    }


    private static void decompress(String inputPath) throws IOException {
        byte[] in  = readAllBytes(inputPath);
        if (in.length == 0) throw new IOException("Empty input");

        boolean isCompressed = in[0] == 1;
        byte[] body = Arrays.copyOfRange(in, 1, in.length);
        byte[] out  = isCompressed ? decode(body) : body;

        String fn = new File(inputPath).getName();
        String base = fn.endsWith(".lzw") ? fn.substring(0, fn.length() - 4)
                                          : fn + ".out";

        writeAllBytes("LZW/" + base, out);
        System.out.println("Written: LZW/" + base +
                           " (" + (isCompressed ? "decompressed" : "raw copy") + ")");
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
            char currChar = (char) (inputByte & 0xFF);   // Ensure unsigned byte conversion
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
        // Write the last matching string
        if (!currentStr.isEmpty()) {
            bitOut.writeBits(dictionary.get(currentStr), BIT_WIDTH);
        }
        // Write any remaining bits
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
    
        // Read the first code
        Integer firstCode = bitIn.readBits(BIT_WIDTH);
        if (firstCode == null) return new byte[0];
    
        String currentStr = dictionary.get(firstCode);
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        outputBuffer.write(currentStr.getBytes("ISO-8859-1"));  // write first decoded string
    
        Integer nextCodeValue;
        while ((nextCodeValue = bitIn.readBits(BIT_WIDTH)) != null) {
            String nextStr;
    
            if (dictionary.containsKey(nextCodeValue)) {
                nextStr = dictionary.get(nextCodeValue);
            } else if (nextCodeValue == nextCode) {
                // Special LZW case: code is not yet in dictionary
                nextStr = currentStr + currentStr.charAt(0);
            } else {
                throw new IOException("Invalid LZW code: " + nextCodeValue);
            }
    
            outputBuffer.write(nextStr.getBytes("ISO-8859-1"));
    
            if (nextCode < MAX_DICT_SIZE) {
                dictionary.put(nextCode++, currentStr + nextStr.charAt(0));
            }
    
            // Move to the next string
            currentStr = nextStr;
        }
    
        return outputBuffer.toByteArray();
    }
    

    private static byte[] readAllBytes(String path) throws IOException {
        try (InputStream in = new FileInputStream(path);
                ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            byte[] tmp = new byte[4096];
            int r;
            while ((r = in.read(tmp)) != -1) buf.write(tmp, 0, r);
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

        BitOutputStream(OutputStream os) { out = os; }

        void writeBits(int b, int len) throws IOException {
            for (int i = len - 1; i >= 0; i--) {
                currentByte = (currentByte << 1) | ((b >> i) & 1);
                if (++numBitsFilled == 8) {
                    out.write(currentByte);
                    numBitsFilled = 0;
                    currentByte   = 0;
                }
            }
        }

        void flush() throws IOException {
            if (numBitsFilled > 0) {
                currentByte <<= (8 - numBitsFilled);   // pad with zeros
                out.write(currentByte);
            }
            out.flush();
        }
    }

    private static class BitInputStream {
        private final InputStream in;
        private int currentByte = 0, numBitsRemaining = 0;

        BitInputStream(InputStream is) { in = is; }

        private int readBit() throws IOException {
            if (numBitsRemaining == 0) {
                currentByte = in.read();
                if (currentByte == -1) return -1; // EOF
                numBitsRemaining = 8;
            }
            numBitsRemaining--;
            return (currentByte >> numBitsRemaining) & 1;
        }

        Integer readBits(int len) throws IOException {
            int val = 0;
            for (int i = 0; i < len; i++) {
                int bit = readBit();
                if (bit < 0) return null;     // In case of EOF the readBit returns -1
                val = (val << 1) | bit;
            }
            return val;
        }
    }
}

