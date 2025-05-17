package com.example.Arithmetic.core;

import com.example.Arithmetic.io.BitInputStream;
import com.example.Arithmetic.util.FrequencyTable;

public class ArithmeticDecoder {

    private static final long MAX_RANGE = 0xFFFFFFFFL;

    private long low;
    private long high;
    private long code;
    private final BitInputStream input;
    private FrequencyTable freq = null;

    public static final int SYMBOL_EOF = 256; // New EOF symbol
    public static final int SYMBOL_COUNT = 257; // 256 real + 1 EOF


    public ArithmeticDecoder(BitInputStream input) {
        this.input = input;
        this.freq = null;
        this.low = 0x00000000L;
        this.high = 0xFFFFFFFFL;
        this.code = 0;
        for (int i = 0; i < 32; i++) {
            code = (code << 1) | readBit();
        }
        System.out.printf("[INIT] Initial code: 0x%08X%n", code);
    }

    public int read(FrequencyTable freqTable) {
        long range = (high - low + 1) & MAX_RANGE;
        if (range == 0) range = MAX_RANGE + 1L;

        int total = freqTable.getTotal();
        long offset = (code - low) & MAX_RANGE;

        long value = ((offset + 1) * total - 1) / range;
        System.out.printf("[DEBUG] Offset=%d, Code=%d, Low=%d, High=%d, Range=%d%n", offset, code, low, high, range);
        System.out.printf("[DEBUG] Total=%d, Value=%d%n", total, value);


        if (value < 0 || value >= total) {
            System.err.printf("[ERROR] value=%d out of bounds [0,%d) (offset=%d, range=%d)%n",
                    value, total, offset, range);
            throw new ArithmeticException("Value out of bounds: " + value + " / total=" + total);
        }

        int symbol = findSymbol(value, freqTable);
        long symLow = freqTable.getLow(symbol);
        long symHigh = freqTable.getHigh(symbol);

        long newLow = low + (range * symLow) / total;
        long newHigh = low + (range * symHigh) / total - 1;

        System.out.printf("[DECODE] Symbol: %d | Value=%d, symLow=%d, symHigh=%d | Range: %d | New Low=%d, High=%d%n",
                symbol, value, symLow, symHigh, range, newLow, newHigh);

        low = newLow & MAX_RANGE;
        high = newHigh & MAX_RANGE;

        while (true) {
            if ((high & 0x80000000L) == (low & 0x80000000L)) {
                low = ((low << 1) & MAX_RANGE);
                high = ((high << 1) & MAX_RANGE) | 1;
                code = ((code << 1) & MAX_RANGE) | readBit();
                System.out.printf("[SHIFT] Code=0x%08X, Low=%d, High=%d%n", code, low, high);
            } else if ((low & 0x40000000L) != 0 && (high & 0x40000000L) == 0) {
                low = ((low << 1) ^ 0x80000000L) & MAX_RANGE;
                high = (((high ^ 0x80000000L) << 1) | 1) & MAX_RANGE;
                code = ((code ^ 0x80000000L) << 1 | readBit()) & MAX_RANGE;
                System.out.printf("[UNDERFLOW] Code=0x%08X, Low=%d, High=%d%n", code, low, high);
            } else {
                break;
            }
        }

        return symbol;
    }


    private int findSymbol(long value, FrequencyTable freqTable) {
        for (int i = 0; i < freqTable.getSymbolLimit(); i++) {
            if (value >= freqTable.getLow(i) && value < freqTable.getHigh(i)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Symbol not found for value: " + value);
    }

    private int readBit() {
        try {
            return input.readBit();
        } catch (Exception e) {
            throw new RuntimeException("Error reading bit", e);
        }
    }
}
