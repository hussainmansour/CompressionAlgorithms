import java.util.Arrays;

public class Chunk {
    byte[] bytes;

    public Chunk(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chunk chunk = (Chunk) o;
        return Arrays.equals(bytes, chunk.bytes);
    }

    @Override
    public String toString() {
        return "Chunk{" +
                "bytes=" + Arrays.toString(bytes) +
                '}';
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }
}
