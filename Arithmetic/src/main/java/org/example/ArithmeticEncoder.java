package org.example;


import java.io.IOException;
import java.util.Objects;


/**
 * Encodes symbols and writes to an arithmetic-coded bit stream. Not thread-safe.
 */
public final class ArithmeticEncoder extends ArithmeticCodeBase {


    private BitOutputStream output;

    private int numUnderflow;

    public ArithmeticEncoder(int numBits, BitOutputStream out) {
        super(numBits);
        output = Objects.requireNonNull(out);
        numUnderflow = 0;
    }




    /**
     * Encodes the specified symbol based on the specified frequency table.
     * This updates this arithmetic coder's state and may write out some bits.
     */
    public void write(FrequencyTable freqs, int symbol) throws IOException {
        write(new SimpleFrequencyTable(freqs), symbol);
    }


    /**
     * Encodes the specified symbol based on the specified frequency table.
     * Also updates this arithmetic coder's state and may write out some bits.
     */
    public void write(SimpleFrequencyTable freqs, int symbol) throws IOException {
        update(freqs, symbol);
    }


    /**
     * Terminates the arithmetic coding by flushing any buffered bits, so that the output can be decoded properly.
     * It is important that this method must be called at the end of the each encoding process.
     */
    public void finish() throws IOException {
        output.write(1);
    }


    protected void shift() throws IOException {
        int bit = (int)(low >>> (numStateBits - 1));
        output.write(bit);

        // Write out the saved underflow bits
        for (; numUnderflow > 0; numUnderflow--)
            output.write(bit ^ 1);
    }


    protected void underflow() {
        if (numUnderflow == Integer.MAX_VALUE)
            throw new ArithmeticException("Maximum underflow reached");
        numUnderflow++;
    }

}