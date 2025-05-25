import java.io.Serializable;
import java.util.Objects;

public class Data implements Serializable {
    long freq;

    public Data(long freq) {
        this.freq = freq;
    }

    Data increment(){
        this.freq++;
        return this;
    }

    long code;
    int len;

    public Data(long code, int len) {
        this.code = code;
        this.len = len;
    }

    @Override
    public String toString() {
        return "Data{" +
                "freq=" + freq +
                ", code=" + code +
                ", len=" + len +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data data = (Data) o;
        return code == data.code && len == data.len;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, len);
    }
}