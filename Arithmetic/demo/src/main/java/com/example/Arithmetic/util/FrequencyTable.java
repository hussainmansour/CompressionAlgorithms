package com.example.Arithmetic.util;

import java.util.Arrays;

public class FrequencyTable {
    private final int[] frequencies;
    private final int[] cumulative;
    private int total;

    public FrequencyTable(int symbolCount) {
        frequencies = new int[symbolCount];
        cumulative = new int[symbolCount + 1];
        total = 0;

        // Initialize frequencies with 1 to avoid zero-frequency problem
        for (int i = 0; i < symbolCount; i++) {
            frequencies[i] = 1;
        }
    }


    public void buildFromData(byte[] data) {
        // Reset frequencies to 1 (or zero if you want to re-count cleanly)
        Arrays.fill(frequencies, 1);

        for (byte b : data) {
            int val = b & 0xFF;
            frequencies[val]++;
        }

        // Make sure EOF symbol frequency is at least 1
        // Assuming EOF symbol at last index (frequencies.length - 1)
        frequencies[frequencies.length - 1] = 1;

        buildCumulative();
    }

    private void buildCumulative() {
        cumulative[0] = 0;
        for (int i = 0; i < frequencies.length; i++) {
            cumulative[i + 1] = cumulative[i] + frequencies[i];
        }
        total = cumulative[frequencies.length];
    }

    public int getSymbolCount() {
        return frequencies.length;
    }

    public int getTotal() {
        return total;
    }

    public int getFrequency(int symbol) {
        return frequencies[symbol];
    }

    public int getLow(int symbol) {
        return cumulative[symbol];
    }

    public int getHigh(int symbol) {
        return cumulative[symbol + 1];
    }

    public void increment(int symbol) {
        frequencies[symbol]++;
        buildCumulative(); // Rebuild cumulative after update
    }

    public void setFrequencies(int[] freqs) {
        if (freqs.length != frequencies.length) {
            throw new IllegalArgumentException("Frequency array length mismatch");
        }
        System.arraycopy(freqs, 0, frequencies, 0, freqs.length);
        buildCumulative();
    }

    public int getSymbolLimit() {
        return cumulative.length - 1;
    }

}
