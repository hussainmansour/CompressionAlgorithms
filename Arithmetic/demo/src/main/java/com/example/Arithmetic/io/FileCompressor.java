package com.example.Arithmetic.io;

import com.example.Arithmetic.core.ArithmeticEncoder;
import com.example.Arithmetic.util.FrequencyTable;

import java.io.*;

import static com.example.Arithmetic.io.FileDecompressor.printFrequencyTable;

public class FileCompressor {

    public void compress(File inputFile, File outputFile) throws IOException {
        byte[] inputData = readFile(inputFile);
        FrequencyTable freqTable = new FrequencyTable(257);
        freqTable.buildFromData(inputData);
//        System.out.println("== Frequency Table Used in Encoding ==");
//        printFrequencyTable(freqTable);

        try (DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
             BitOutputStream bitOut = new BitOutputStream(dataOut)) {

            // Write frequency table size (symbol count)
            int symbolCount = freqTable.getSymbolCount();
            dataOut.writeInt(symbolCount);

            // Write frequency counts for all symbols
            for (int symbol = 0; symbol < symbolCount; symbol++) {
                dataOut.writeInt(freqTable.getFrequency(symbol));
            }

            // Encode all symbols
            ArithmeticEncoder encoder = new ArithmeticEncoder(bitOut);
            for (byte b : inputData) {
//                System.out.printf("[ENCODE] Symbol: %d (%c)%n", b, (char) b);
                encoder.write(freqTable, b & 0xFF);
            }
//            System.out.printf("[ENCODE] Symbol: %d (%c)%n", 256, (char) 256);
            encoder.write(freqTable, 256); // EOF symbol
            encoder.finish();
        }
    }

    private byte[] readFile(File file) throws IOException {
        try (InputStream input = new FileInputStream(file)) {
            return input.readAllBytes();
        }
    }
}
