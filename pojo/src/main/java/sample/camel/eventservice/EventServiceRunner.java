package sample.camel.eventservice;

import lombok.RequiredArgsConstructor;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventServiceRunner implements ApplicationRunner {
	@Autowired
	private final ProducerTemplate template;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		template.sendBody("direct:event-service", new Event("gigi"));
		template.sendBodyAndHeader("direct:event-service",
				new Event("gigi"), "class", EventServiceClientAdapter.class);
		template.sendBodyAndHeader("direct:event-service",
				new Event("gigi"), "class", EventServiceRunner.class.getName());
	}
}
