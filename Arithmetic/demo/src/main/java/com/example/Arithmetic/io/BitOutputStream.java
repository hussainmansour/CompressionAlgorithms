package com.example.Arithmetic.io;

import java.io.IOException;
import java.io.OutputStream;

public class BitOutputStream implements AutoCloseable {
    private OutputStream output;
    private int currentByte;
    private int numBitsFilled;

    public BitOutputStream(OutputStream output) {
        this.output = output;
        this.currentByte = 0;
        this.numBitsFilled = 0;
    }

    public void write(int bit) throws IOException {
        if (bit != 0 && bit != 1)
            throw new IllegalArgumentException("Bit must be 0 or 1");

        currentByte = (currentByte << 1) | bit;
        numBitsFilled++;

        if (numBitsFilled == 8) {
            output.write(currentByte);
            numBitsFilled = 0;
            currentByte = 0;
        }
    }

    public void flush() throws IOException {
        while (numBitsFilled != 0)
            write(0); // Pad with zeros
        output.flush();
    }

    public void close() throws IOException {
        flush();
        output.close();
    }
}
