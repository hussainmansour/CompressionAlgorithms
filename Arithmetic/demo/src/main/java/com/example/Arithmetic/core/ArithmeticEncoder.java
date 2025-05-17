package com.example.Arithmetic.core;

import com.example.Arithmetic.io.BitOutputStream;
import com.example.Arithmetic.util.FrequencyTable;

import java.io.IOException;

public class ArithmeticEncoder {
    private static final int STATE_SIZE = 32; // bits in arithmetic state
    private static final long MAX_RANGE = (1L << STATE_SIZE) - 1;
    private static final long HALF_RANGE = 1L << (STATE_SIZE - 1);
    private static final long QUARTER_RANGE = HALF_RANGE >> 1;

    public static final int SYMBOL_EOF = 256; // New EOF symbol
    public static final int SYMBOL_COUNT = 257; // 256 real + 1 EOF


    private long low = 0;
    private long high = MAX_RANGE;

    private int underflowBits = 0;
    private final BitOutputStream output;

    public ArithmeticEncoder(BitOutputStream output) {
        this.output = output;
        System.out.println("[Encoder] Initialized.");
    }

    public void write(FrequencyTable freq, int symbol) throws IOException {
        System.out.printf("[Encoder] Writing symbol: %d%n", symbol);

        long range = high - low + 1;
        System.out.printf("[Encoder] Current range: low=%d high=%d range=%d%n", low, high, range);

        long total = freq.getTotal();
        long symLow = freq.getLow(symbol);
        long symHigh = freq.getHigh(symbol);

        System.out.printf("[Encoder] Symbol freq range: low=%d high=%d total=%d%n", symLow, symHigh, total);

        high = low + (range * symHigh) / total - 1;
        low = low + (range * symLow) / total;

        System.out.printf("[Encoder] Updated range: low=%d high=%d%n", low, high);

        // Emit bits while high and low share the same MSB
        while (((high ^ low) & HALF_RANGE) == 0) {
            int msb = (int) (high >>> (STATE_SIZE - 1));
            writeBit(msb);
            System.out.printf("[Encoder] Writing bit: %d%n", msb);

            low = (low << 1) & MAX_RANGE;
            high = ((high << 1) & MAX_RANGE) | 1;

            System.out.printf("[Encoder] After shifting: low=%d high=%d%n", low, high);

            // Write underflow bits
            while (underflowBits > 0) {
                int underflowBit = (~msb) & 1;
                writeBit(underflowBit);
                System.out.printf("[Encoder] Writing underflow bit: %d%n", underflowBit);
                underflowBits--;
            }
        }

        // Handle underflow condition
        while ((low & ~high & QUARTER_RANGE) != 0) {
            underflowBits++;
            System.out.printf("[Encoder] Underflow detected, increment underflowBits to %d%n", underflowBits);

            low = (low << 1) ^ HALF_RANGE;
            high = ((high ^ HALF_RANGE) << 1) | HALF_RANGE | 1;

            System.out.printf("[Encoder] After underflow adjust: low=%d high=%d%n", low, high);
        }
    }

    public void finish() throws IOException {
        System.out.println("[Encoder] Finishing encoding...");

        int msb = (int) (low >>> (STATE_SIZE - 1));
        writeBit(msb);
        System.out.printf("[Encoder] Writing final bit: %d%n", msb);

        for (int i = 0; i <= underflowBits; i++) {
            int bit = (~msb) & 1;
            writeBit(bit);
            System.out.printf("[Encoder] Writing final underflow bit: %d%n", bit);
        }

        output.flush();
        System.out.println("[Encoder] Encoding finished and flushed.");
    }

    private void writeBit(int bit) throws IOException {
        output.write(bit);
    }

    public long getLow() {
        return low;
    }

    public long getHigh() {
        return high;
    }
}
