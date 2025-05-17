package com.example.Arithmetic.io;

import com.example.Arithmetic.core.ArithmeticDecoder;
import com.example.Arithmetic.util.FrequencyTable;

import java.io.*;

public class FileDecompressor {

    public void decompress(File inputFile, File outputFile) throws IOException {
        try (DataInputStream dataIn = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
             BitInputStream bitIn = new BitInputStream(dataIn);
             OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {

            int symbolCount = dataIn.readInt();
            System.out.println("[DEBUG] Symbol count: " + symbolCount);

            int[] frequencies = new int[symbolCount];
            for (int i = 0; i < symbolCount; i++) {
                frequencies[i] = dataIn.readInt();
            }

            FrequencyTable freqTable = new FrequencyTable(symbolCount);
            freqTable.setFrequencies(frequencies);

            System.out.println("\n== Frequency Table from Compressed File ==");
            printFrequencyTable(freqTable);
            System.out.println("===========================================\n");

            if (freqTable.getTotal() == 0) {
                throw new IllegalStateException("Decoded frequency table has total frequency = 0");
            }

            ArithmeticDecoder decoder = new ArithmeticDecoder(bitIn);

            int symbolIndex = 0;
            while (true) {
                int symbol = decoder.read(freqTable);
                System.out.printf("[DEBUG] Decoded symbol #%d: %d (%c)%n", symbolIndex, symbol, (char) symbol);
                if (symbol == 256) { // EOF
                    System.out.println("[DEBUG] Reached EOF symbol.");
                    break;
                }
                out.write(symbol);
                symbolIndex++;
            }
        }
    }

    public static void printFrequencyTable(FrequencyTable freqTable) {
        int symbolCount = freqTable.getSymbolCount();
        long total = 0;
        for (int i = 0; i < symbolCount; i++) {
            int freq = freqTable.getFrequency(i);
            if (freq > 0) {
                System.out.printf("Symbol %3d (%c): Frequency = %d%n", i, (i >= 32 && i <= 126) ? (char) i : '?', freq);
                total += freq;
            }
        }
        System.out.println("Total frequency: " + total);
    }
}
