package sample.camel;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AppUtils {
//    private static final String LINE = "\nline${header.CamelTimerCounter},${random(0,200)}";
//    private static final Function<Integer, String> LINE = i -> "\nstep${header.CamelTimerCounter},line" + i;
    private static final Function<Integer, String> LINE = i -> "\nline" + i + ',' + Math.random();

    public static String lines(int count) {
        return IntStream.rangeClosed(1, count).boxed().map(LINE).collect(Collectors.joining());
    }
}
