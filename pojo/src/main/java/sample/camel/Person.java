package sample.camel;

import lombok.*;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

@CsvRecord(separator = ",", crlf = "UNIX")
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Person {
    @DataField(pos = 1)
    private String line;

    @DataField(pos = 2, trim = true)
    private Double value;
}
