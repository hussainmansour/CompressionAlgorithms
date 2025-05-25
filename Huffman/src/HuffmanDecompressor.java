import java.io.*;
import java.util.HashMap;

public class HuffmanDecompressor {
    private String filePath;

    public HuffmanDecompressor(String filePath) {
        this.filePath = filePath;
    }

    // read compressed file
    // get dictionary
    // read bitcount
    // if bitcount is 0 stop reading
    // read data bit by bit and check for equivalence in extracted dictionary
    // if found write in decompressed file
    // else wait till accumulation of a write code

    void decompress() throws IOException {
        long then = System.currentTimeMillis();
        DataInputStream in = new DataInputStream(new FileInputStream(filePath));
        FileNameManipulator fm = new FileNameManipulator();
        DataOutputStream out = new DataOutputStream(new FileOutputStream(fm.extractedFilePath(filePath)));
        DictionaryEmbedder embedder = new DictionaryEmbedder();
        HashMap<Data, Byte> dictionary = embedder.extractDictionary(in);
        long fileBytesCount = embedder.fileBytes;
        long bitCount = in.readLong();
        Data sequence = new Data(0, 0);
        int z;// for debugging;
        byte[] outBuffer = new byte[10000000];
        int indx = 0;
        while ((bitCount > 0) && (z = in.available()) > 0) {
            byte[] buffer = in.readNBytes(100*1024*1024);
            for (byte b : buffer) {
                for (int i = 7; i >= 0; i--) {
                    if (bitCount == 0)
                        break;
                    sequence.code = sequence.code << 1;
                    int x = b & (1 << i);
                    if (x != 0) {
                        sequence.code |= 1;
                    }
                    bitCount--;
                    sequence.len++;
                    if (dictionary.containsKey(sequence)) {
                        Byte chunk = dictionary.get(sequence);
                        if (indx == (outBuffer.length)) {
                            out.write(outBuffer, 0, outBuffer.length);
                            indx = 0;
                        }
                        outBuffer[indx++] = chunk;
                        fileBytesCount -= 1;
                        sequence.code = 0;
                        sequence.len = 0;
                    }
                }
            }
        }
        if (indx != 0) {
            out.write(outBuffer, 0, indx);
        }
        if (fileBytesCount != 0) {
            System.err.println("error in decompression");
            System.err.println("fileBytesCount = " + fileBytesCount);
            System.err.println("indx = " + indx);
            System.err.println("outBuffer.length = " + outBuffer.length);
            System.err.println("outBuffer = " + outBuffer);
        }
        long now = System.currentTimeMillis();
        in.close();
        out.close();
        System.out.println("decompressing time : " + (now - then) + " ms");
    }

    public static void main(String[] args) {
        HuffmanDecompressor decomp = new HuffmanDecompressor("./kimokono.txt.huffmancompressed");
        try {
            decomp.decompress();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
