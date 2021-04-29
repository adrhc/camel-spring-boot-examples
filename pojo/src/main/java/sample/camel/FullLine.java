package sample.camel;

import lombok.Getter;
import lombok.ToString;

@Getter
public class FullLine extends IncompleteLine {
    private final String line;

    public FullLine(Double value, String line) {
        super(value);
        this.line = line;
    }

    @Override
    public String toString() {
        return "FullLine{" +
                "line='" + line + '\'' +
                "value='" + value + '\'' +
                '}';
    }
}
