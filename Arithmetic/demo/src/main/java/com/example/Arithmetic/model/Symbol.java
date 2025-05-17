package com.example.Arithmetic.model;

public class Symbol {
    public int value;             // byte value (0–255)
    public long low;             // cumulative low range (integer)
    public long high;            // cumulative high range (integer)
    public int frequency;        // raw frequency (for modeling)

    public Symbol(int value, long low, long high, int frequency) {
        this.value = value;
        this.low = low;
        this.high = high;
        this.frequency = frequency;
    }
}
