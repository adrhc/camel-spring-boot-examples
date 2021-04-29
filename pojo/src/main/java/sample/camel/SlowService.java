package sample.camel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Body;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class SlowService implements Consumer<Object> {
    private final long millis;

    static SlowService createSlowService(long millis) {
        return new SlowService(millis);
    }

    @Override
    public void accept(@Body Object body) {
        try {
            Thread.sleep(millis);
            log.debug("\nmillis = {}, thread = {}, {} consumer processed:\n{}",
                    millis,
                    Thread.currentThread().getName(),
                    millis,
                    body);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
