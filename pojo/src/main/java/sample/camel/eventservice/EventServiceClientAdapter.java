package sample.camel.eventservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Body;
import org.apache.camel.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventServiceClientAdapter {
	private final EventServiceClient eventServiceClient;

	public void process(@Header("class") Class<?> clazz, @Body Event event) {
		log.debug("\n{}", clazz == null ? "received NULL class" : clazz.toString());
		eventServiceClient.sendEvent(event);
	}
}
