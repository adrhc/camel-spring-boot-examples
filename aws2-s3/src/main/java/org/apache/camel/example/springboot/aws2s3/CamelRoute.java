/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.example.springboot.aws2s3;

import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.springframework.stereotype.Component;

@Component
public class CamelRoute extends RouteBuilder {
	private static final String DEBUG_ALL = "log:DEBUG?showAll=true&multiline=true&skipBodyLineSeparator=false";
	private static final String ERROR_ALL = "log:ERROR?showAll=true&multiline=true&skipBodyLineSeparator=false";
	private static final String ERROR_KEY = "joke 1.txt";

	@Override
	public void configure() {
		errorHandler(deadLetterChannel("direct:bad-jokes").useOriginalMessage()
				.maximumRedeliveries(2).redeliveryDelay(5000));

		from("direct:bad-jokes")
//				.log("\nERROR on ${header.CamelAwsS3BucketName}/${header.CamelAwsS3Key}")
				.setHeader(AWS2S3Constants.BUCKET_DESTINATION_NAME, simple("${header.CamelAwsS3BucketName}"))
				.setHeader(AWS2S3Constants.DESTINATION_KEY, simple("bad/${header.CamelAwsS3Key}"))
				.to(ERROR_ALL)
				.to("aws2-s3://{{bucketName}}?operation=copyObject");

		from("aws2-s3://{{bucketName}}?delay=1000&prefix=jokes")
				.to(DEBUG_ALL)
//				.log("\n${header.CamelAwsS3BucketName}/${header.CamelAwsS3Key}:\n${body}")
				.process(ex -> {
					Message message = ex.getIn();
					String key = (String) message.getHeader(AWS2S3Constants.KEY);
					if (key.contains(ERROR_KEY)) {
						throw new RuntimeException("bad file");
					}
				});
//				.log("Received body:\n${body}");
	}
}
