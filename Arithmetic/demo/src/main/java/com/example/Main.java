package com.example;

public class Main {
    public static void main(String[] args) {
        String input = "I'm Ahmed. I'm testing this coding";

        ArithmeticCoding encoderDecoder = new ArithmeticCoding();

        double encoded = encoderDecoder.encode(input);
        System.out.println("Encoded value: " + encoded);

        String decoded = encoderDecoder.decode(encoded, input.length());
        System.out.println("Decoded message: " + decoded);
    }
}