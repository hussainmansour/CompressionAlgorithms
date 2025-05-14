package com.example;

import java.util.*;

public class ArithmeticCoding {

    private String input;
    private Map<String, Double> probabilities;
    private Map<String, Symbol> symbolTable;

    private void buildSymbolTable() {
        this.symbolTable = new HashMap<>();
        double accum = 0.0;
        for (Map.Entry<String, Double> letterProb : this.probabilities.entrySet()) {
            String letter = letterProb.getKey();
            double prob = letterProb.getValue();
            symbolTable.put(letter, new Symbol(letter, accum, accum + prob, prob));
            accum += prob;
        }
    }

    private void calcProbs() {
        this.probabilities = new HashMap<>();

        for (int i = 0; i < this.input.length(); i++) {
            if (probabilities.get(this.input.charAt(i) + "") != null)
                probabilities.put(this.input.charAt(i) + "", probabilities.get(this.input.charAt(i) + "") + 1);
            else
                probabilities.put(this.input.charAt(i) + "", 1.0);
        }

        for (Map.Entry<String, Double> prob : probabilities.entrySet()) {
            probabilities.put(prob.getKey(), prob.getValue() / Double.valueOf(this.input.length()));
        }
    }

    public double encode(String input) {
        this.input = input;
        calcProbs();
        buildSymbolTable();
        double low = 0.0;
        double high = 1.0;

        for (int i = 0; i < this.input.length(); i++) {
            Symbol s = symbolTable.get(String.valueOf(this.input.charAt(i)));
            double range = high - low;
            double newLow = low + range * s.low;
            double newHigh = low + range * s.high;
            low = newLow;
            high = newHigh;
        }

        return (low + high) / 2.0;
    }

    public String decode(double code, int len) {
        StringBuilder result = new StringBuilder();
        double low = 0.0;
        double high = 1.0;

        for (int i = 0; i < len; i++) {
            double value = (code - low) / (high - low);

            for (Symbol s : symbolTable.values()) {
                if (value >= s.low && value < s.high) {
                    result.append(s.ch);
                    double range = high - low;
                    high = low + range * s.high;
                    low = low + range * s.low;
                    break;
                }
            }
        }

        return result.toString();
    }
}
