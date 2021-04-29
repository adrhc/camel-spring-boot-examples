package sample.camel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Body;

import java.util.function.Consumer;

import static sample.camel.AppUtils.sleepSafe;

@Slf4j
@RequiredArgsConstructor
public class SlowService implements Consumer<Object> {
    private final long millis;

    static SlowService createSlowService(long millis) {
        return new SlowService(millis);
    }

    @Override
    public void accept(@Body Object body) {
        sleepSafe(millis);
        log.debug("\nmillis = {}, thread = {}, processing:\n{}",
                millis, Thread.currentThread().getName(), body);
    }
}
