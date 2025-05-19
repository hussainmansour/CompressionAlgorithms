package org.example;

import java.io.IOException;


/**
 * Provides the state and behaviors that arithmetic coding encoders and decoders share.
 */
public abstract class ArithmeticCodeBase {

    protected final int numStateBits;

    /** Maximum range (high+1-low) during coding (trivial), which is 2^numStateBits = 1000...000. */
    protected final long fullRange;

    /** The top bit at width numStateBits, which is 0100...000. */
    protected final long halfRange;

    /** The second highest bit at width numStateBits, which is 0010...000. This is zero when numStateBits=1. */
    protected final long quarterRange;

    /** Minimum range (high+1-low) during coding (non-trivial), which is 0010...010. */
    protected final long minimumRange;

    /** Maximum allowed total from a frequency table at all times during coding. */
    protected final long maximumTotal;

    /** Bit mask of numStateBits ones, which is 0111...111. */
    protected final long stateMask;


    /**
     * Low end of this arithmetic coder's current range. Conceptually has an infinite number of trailing 0s.
     */
    protected long low;

    /**
     * High end of this arithmetic coder's current range. Conceptually has an infinite number of trailing 1s.
     */
    protected long high;


    public ArithmeticCodeBase(int numBits) {
        if (!(1 <= numBits && numBits <= 62))
            throw new IllegalArgumentException("State size out of range");
        numStateBits = numBits;
        fullRange = 1L << numStateBits;
        halfRange = fullRange >>> 1;  // Non-zero
        quarterRange = halfRange >>> 1;  // Can be zero
        minimumRange = quarterRange + 2;  // At least 2
        maximumTotal = Math.min(Long.MAX_VALUE / fullRange, minimumRange);
        stateMask = fullRange - 1;

        low = 0;
        high = stateMask;
    }




    /**
     * Updates the code range (low and high) of this arithmetic coder as a result
     * of processing the specified symbol with the specified frequency table.
     */
    protected void update(SimpleFrequencyTable freqs, int symbol) throws IOException {
        // State check
        if (low >= high || (low & stateMask) != low || (high & stateMask) != high)
            throw new AssertionError("Low or high out of range");
        long range = high - low + 1;
        if (!(minimumRange <= range && range <= fullRange))
            throw new AssertionError("Range out of range");

        // Frequency table values check
        long total = freqs.getTotal();
        long symLow = freqs.getLow(symbol);
        long symHigh = freqs.getHigh(symbol);
        if (symLow == symHigh)
            throw new IllegalArgumentException("Symbol has zero frequency");
        if (total > maximumTotal)
            throw new IllegalArgumentException("Cannot code symbol because total is too large");

        // Update range
        long newLow  = low + symLow  * range / total;
        long newHigh = low + symHigh * range / total - 1;
        low = newLow;
        high = newHigh;

        // While low and high have the same top bit value, shift them out
        while (((low ^ high) & halfRange) == 0) {
            shift();
            low  = ((low  << 1) & stateMask);
            high = ((high << 1) & stateMask) | 1;
        }
        // Now low's top bit must be 0 and high's top bit must be 1

        // While low's top two bits are 01 and high's are 10, delete the second highest bit of both
        while ((low & ~high & quarterRange) != 0) {
            underflow();
            low = (low << 1) ^ halfRange;
            high = ((high ^ halfRange) << 1) | halfRange | 1;
        }
    }


    /**
     * Called to handle the situation when the top bit of low and high are equal.
     */
    protected abstract void shift() throws IOException;


    /**
     * Called to handle the situation when low=01(...) and high=10(...).
     */
    protected abstract void underflow() throws IOException;

}