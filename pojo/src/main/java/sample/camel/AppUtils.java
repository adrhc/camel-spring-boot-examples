package sample.camel;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class AppUtils {
    //    private static final String LINE = "\nline${header.CamelTimerCounter},${random(0,200)}";
//    private static final Function<Integer, String> LINE = i -> "\nstep${header.CamelTimerCounter},line" + i;
    private static final Function<Integer, String> LINE = i -> "\nline" + i + ',' + Math.random();
    private static final Function<Integer, String> SHORT_LINE = i -> "\n" + Math.random();

    public static String lines(int count) {
        return IntStream.rangeClosed(1, count).boxed()
                .map(i -> (i % 2 == 1 ? LINE : SHORT_LINE).apply(i))
                .collect(Collectors.joining());
    }

    public static void sleepSafe(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
}
