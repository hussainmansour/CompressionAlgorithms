package org.example;

import java.util.Objects;


public final class SimpleFrequencyTable implements FrequencyTable {


    private int[] frequencies;

    private int[] cumulative;

    private int total;

    public SimpleFrequencyTable(int[] freqs) {
        Objects.requireNonNull(freqs);
        if (freqs.length < 1)
            throw new IllegalArgumentException("At least 1 symbol needed");
        if (freqs.length > Integer.MAX_VALUE - 1)
            throw new IllegalArgumentException("Too many symbols");

        frequencies = freqs.clone();  // Make copy
        total = 0;
        for (int x : frequencies) {
            if (x < 0)
                throw new IllegalArgumentException("Negative frequency");
            total = Math.addExact(x, total);
        }
        cumulative = null;
    }

    public SimpleFrequencyTable(FrequencyTable freqs) {
        Objects.requireNonNull(freqs);
        int numSym = freqs.getSymbolLimit();
        if (numSym < 1)
            throw new IllegalArgumentException("At least 1 symbol needed");

        frequencies = new int[numSym];
        total = 0;
        for (int i = 0; i < frequencies.length; i++) {
            int x = freqs.get(i);
            if (x < 0)
                throw new IllegalArgumentException("Negative frequency");
            frequencies[i] = x;
            total = Math.addExact(x, total);
        }
        cumulative = null;
    }




    public int getSymbolLimit() {
        return frequencies.length;
    }

    public int get(int symbol) {
        checkSymbol(symbol);
        return frequencies[symbol];
    }

    public void set(int symbol, int freq) {
        checkSymbol(symbol);
        if (freq < 0)
            throw new IllegalArgumentException("Negative frequency");

        int temp = total - frequencies[symbol];
        if (temp < 0)
            throw new AssertionError();
        total = Math.addExact(temp, freq);
        frequencies[symbol] = freq;
        cumulative = null;
    }

    public void increment(int symbol) {
        checkSymbol(symbol);
        if (frequencies[symbol] == Integer.MAX_VALUE)
            throw new ArithmeticException("Arithmetic overflow");
        total = Math.addExact(total, 1);
        frequencies[symbol]++;
        cumulative = null;
    }

    public int getTotal() {
        return total;
    }

    public int getLow(int symbol) {
        checkSymbol(symbol);
        if (cumulative == null)
            initCumulative();
        return cumulative[symbol];
    }

    public int getHigh(int symbol) {
        checkSymbol(symbol);
        if (cumulative == null)
            initCumulative();
        return cumulative[symbol + 1];
    }


    // Recomputes the array of cumulative symbol frequencies.
    private void initCumulative() {
        cumulative = new int[frequencies.length + 1];
        int sum = 0;
        for (int i = 0; i < frequencies.length; i++) {
            sum = Math.addExact(frequencies[i], sum);
            cumulative[i + 1] = sum;
        }
        if (sum != total)
            throw new AssertionError();
    }

    private void checkSymbol(int symbol) {
        if (!(0 <= symbol && symbol < frequencies.length))
            throw new IllegalArgumentException("Symbol out of range");
    }
}