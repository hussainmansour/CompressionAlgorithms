import java.io.*;
import java.util.HashMap;

public class DictionaryEmbedder {
    // not including MAGIC_NUMBER can save 8 bytes, but it acts as a good indication
    // of working code
    private static final int MAGIC_NUMBER = 0xABCDEF; // to mark the start and the end of the dictionary
    private HashMap<Byte, Data> dictionary;
    private HashMap<Data, Byte> extracted;
    long fileBytes;

    public DictionaryEmbedder() {
    }

    public DictionaryEmbedder(HashMap<Byte, Data> dictionary, long fileBytes) {
        this.dictionary = dictionary;
        this.fileBytes = fileBytes;
    }

    void embedDictionary(DataOutputStream out) throws IOException {
        out.writeInt(MAGIC_NUMBER);
        out.writeInt(dictionary.size());
        for (Byte chunk : dictionary.keySet()) {
            out.writeByte(chunk);
            Data p = dictionary.get(chunk);
            out.writeByte(p.len);
            out.writeLong(p.code);
        }
        out.writeLong(fileBytes);
        out.writeInt(MAGIC_NUMBER);
    }

    HashMap<Data, Byte> extractDictionary(DataInputStream in) throws IOException {
        int magicNumber = in.readInt();
        if (magicNumber != MAGIC_NUMBER) {
            throw new RuntimeException("unsupported format");
        }
        int mapSize = in.readInt();
        extracted = new HashMap<>(mapSize);
        for (int i = 0; i < mapSize; i++) {
            byte[] word = in.readNBytes(1);
            Byte chunk = word[0];
            int len = in.readByte();
            long code = in.readLong();
            Data p = new Data(code, len);
            extracted.put(p, chunk);
        }
        fileBytes = in.readLong();
        magicNumber = in.readInt();
        if (magicNumber != MAGIC_NUMBER) {
            throw new RuntimeException("error extracting the dictionary");
        }
        return this.extracted;
    }

    // public static void main(String[] args) throws IOException {
    // FrequencyCounter fc = new
    // FrequencyCounter("huffman compression/src/kimokono.txt");
    // HashMap<Byte,Data> freqs = fc.countFrequency();
    // HuffmanCodeGenerator gem = new HuffmanCodeGenerator(freqs);
    // HashMap<Byte, Data> dic = gem.getDictionary();
    // System.out.println("original : "+dic);
    // DictionaryEmbedder emb = new DictionaryEmbedder(dic,fc.bytesNumber);
    // DataOutputStream out = new DataOutputStream(new FileOutputStream("huffman compression/src/abc.dic"));
    // emb.embedDictionary(out);
    // DataInputStream in = new DataInputStream(new FileInputStream("huffman compression/src/abc.dic"));
    // HashMap<Data,Byte> dicx = emb.extractDictionary(in);
    // System.out.println("extracted : "+dicx);
    // // System.out.println(dic.equals(dicx));
    // }

}
