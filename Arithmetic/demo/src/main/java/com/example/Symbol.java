package com.example;

public class Symbol {
    String ch;
    double low;
    double high;
    double probability;

    public Symbol(String ch, double low, double high, double probability) {
        this.ch = ch;
        this.low = low;
        this.high = high;
        this.probability = probability;
    }
}
