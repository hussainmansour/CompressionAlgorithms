package com.example.Arithmetic.io;

import java.io.IOException;
import java.io.InputStream;

public class BitInputStream implements AutoCloseable {
    private InputStream input;
    private int currentByte;
    private int numBitsRemaining;

    public BitInputStream(InputStream input) {
        this.input = input;
        this.numBitsRemaining = 0;
    }

    public int read() throws IOException {
        if (currentByte == -1) return -1;

        if (numBitsRemaining == 0) {
            currentByte = input.read();
            if (currentByte == -1) return -1;
            numBitsRemaining = 8;
        }

        numBitsRemaining--;
        return (currentByte >>> numBitsRemaining) & 1;
    }

    public void close() throws IOException {
        input.close();
    }

    public int readBit() throws IOException {
        if (numBitsRemaining == 0) {
            currentByte = input.read();
            if (currentByte == -1) {
                return 0; // End of stream: pad with 0s
            }
            numBitsRemaining = 8;
        }

        numBitsRemaining--;
        return (currentByte >>> numBitsRemaining) & 1;
    }

}
