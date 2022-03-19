package sample.camel.eventservice;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class EventServiceRoute extends RouteBuilder {
	private static final String DEBUG_ALL = "log:DEBUG?showAll=true&multiline=true&skipBodyLineSeparator=false";

	@Override
	public void configure() {
		from("direct:event-service")
//				.to(DEBUG_ALL)
				.to("bean:eventServiceClientAdapter");
	}
}
