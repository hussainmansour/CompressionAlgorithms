import java.io.*;
import java.util.HashMap;

public class HuffmanCompressor {

    private String filepath;
    // private int wordSize = 1;

    public HuffmanCompressor(String filepath) {
        this.filepath = filepath;
        // this.wordSize = Math.max(wordSize, 1);
    }

    // read file
    // collect frequencies
    // generate dictionary
    // embed dictionary
    // read file
    // convert each chunk to equivalent code
    void compress() throws IOException {
        long then = System.currentTimeMillis();
        FrequencyCounter fc = new FrequencyCounter(filepath);
        HashMap<Byte, Data> frequencies = fc.countFrequency();
        HuffmanCodeGenerator generator = new HuffmanCodeGenerator(frequencies);
        HashMap<Byte, Data> dictionary = generator.getDictionary();
        DictionaryEmbedder embedder = new DictionaryEmbedder(dictionary, fc.bytesNumber);
        FileNameManipulator fm = new FileNameManipulator();
        String compressedFilePath = fm.compressedFilePath(filepath);
        DataOutputStream out = new DataOutputStream(new FileOutputStream(compressedFilePath));
        embedder.embedDictionary(out);
        long expectedBitCount = getExpectedNumberOfBits(dictionary);
        out.writeLong(expectedBitCount);
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(filepath));
        int writingBuffer = 0;
        int limit = 8;
        int z;//for debugging
        byte[] outBuffer = new byte[10000000];
        byte[] buffer = new byte[100*1024*1024];
        int indx = 0;
        
        while ((z = in.read(buffer)) != -1) {
            for (int i=0; i < z; i++) {
                byte chunk = buffer[i];
                Data data = dictionary.get(chunk);
                if (data == null) {
                    System.err.println("chunk = " + chunk);
                }
                for (int k = data.len - 1; k >= 0; k--) {
                    if (limit == 0) {
                        if (indx == outBuffer.length) {
                            out.write(outBuffer, 0, indx);
                            indx = 0;
                        }

                        outBuffer[indx++] = (byte) writingBuffer;
                        writingBuffer = 0;
                        limit = 8;
                    }
                    writingBuffer = writingBuffer << 1;
                    if ((data.code & (1L << k)) != 0) {
                        writingBuffer |= 1;
                    }
                    limit--;
                }
            }
        }
        if (indx == outBuffer.length) {
            out.write(outBuffer, 0, indx);
            indx = 0;
        }

        if (limit < 8) {
            while (limit > 0) {
                writingBuffer = writingBuffer << 1;
                limit--;
            }
            outBuffer[indx++] = (byte) writingBuffer;
            // out.write(writingBuffer);
        }
        out.write(outBuffer, 0, indx);
        long now = System.currentTimeMillis();
        in.close();
        out.close();
        File original = new File(filepath);
        File compressed = new File(compressedFilePath);
        double compressionRatio = (compressed.length() * 1.0 / original.length()) * 100;
        System.out.println("compression ratio = " + compressionRatio + " %");
        System.out.println("Compression time : " + (now - then) + " ms");
    }

    private long getExpectedNumberOfBits(HashMap<Byte, Data> dictionary) {
        long bits = 0;
        for (Byte chunk : dictionary.keySet()) {
            bits += dictionary.get(chunk).freq * dictionary.get(chunk).len;
        }
        return bits;
    }

    public static void main(String[] args) {
        HuffmanCompressor compressor = new HuffmanCompressor(
                "huffman compression/src/kimokono.txt");
        try {
            compressor.compress();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
