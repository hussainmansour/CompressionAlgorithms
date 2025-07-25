import java.io.*;
import java.util.HashMap;

public class FrequencyCounter {
    private String filePath;
    private HashMap<Byte, Data> frequencyTable;
    long bytesNumber;

    public FrequencyCounter(String filePath) {
        this.filePath = filePath;
        this.frequencyTable = new HashMap<>(256);
        this.bytesNumber = 0;
    }

    HashMap<Byte, Data> countFrequency() throws IOException {

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(filePath));
        byte[] buffer = new byte[100*1024*1024];
        int bytesRead;
        while ((bytesRead= in.read(buffer)) != -1) {
            this.bytesNumber += bytesRead;
            for (int i = 0; i < buffer.length;i++) {
                frequencyTable.put(buffer[i], frequencyTable.getOrDefault(buffer[i], new Data(0)).increment());
            }
        }
        in.close();
        return this.frequencyTable;
    }

    public static void main(String[] args) throws IOException {
        FrequencyCounter fc = new FrequencyCounter("huffman compression/src/kimokono.txt");
        fc.countFrequency();
        System.out.println(fc.frequencyTable);
    }
}
