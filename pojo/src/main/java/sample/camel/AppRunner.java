package sample.camel;

import lombok.RequiredArgsConstructor;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import static sample.camel.AppUtils.lines;

@Component
@RequiredArgsConstructor
public class AppRunner implements ApplicationRunner {
    @Autowired
    private final ProducerTemplate template;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        template.sendBody("direct:start", lines(100));
    }
}
