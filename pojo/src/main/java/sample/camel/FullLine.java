package sample.camel;

import lombok.Getter;

@Getter
public class FullLine extends IncompleteLine {
    private final String line;

    public FullLine(String line, Double value) {
        super(value);
        this.line = line;
    }

    @Override
    public String toString() {
        return "FullLine{" +
                "line='" + line + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
