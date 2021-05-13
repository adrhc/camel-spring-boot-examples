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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * http://localhost:9000/minio
 */
@Component
@Slf4j
@Setter
public class CamelRoute extends RouteBuilder {
	private static final String DEBUG_ALL = "log:DEBUG?showAll=true&multiline=true&skipBodyLineSeparator=false";
	private static final String DEBUG_NO_HEADERS = "log:DEBUG?multiline=true&skipBodyLineSeparator=false&showExchangePattern=false&showProperties=true";
	private static final String ERROR_ALL = "log:ERROR?showAll=true&multiline=true&skipBodyLineSeparator=false";
	private static final String ERROR_KEY = "joke 1.txt";
	@Value("${bucketName}")
	private String bucketName;

	/**
	 * reading (using request builder) the list of files (names) and splitting to process 1 by 1
	 * splitting each files lines (all at once per file but not streaming): processing line by line
	 */
	@Override
	public void configure() {
		from("timer:number?period=30000")
				.setBody(ex -> ListObjectsRequest.builder().bucket(bucketName).prefix("jokes").build())
				.to("aws2-s3://{{bucketName}}?operation=listObjects&pojoRequest=true")
				.to(DEBUG_NO_HEADERS)
				.split(body())
				.log("\n[timer:number] ${threadName}:\nkey = ${body.key}, size = ${body.size}")
				.to("direct:files");

		from("direct:files")
				.delay(5000)
				.setHeader(AWS2S3Constants.KEY, simple("${body.key}"))
				.to("aws2-s3://{{bucketName}}?operation=getObject")
				.setBody(simple("${bodyAs(String)}"))
//				.to(DEBUG_ALL)
				.log("\n[direct:files1] ${threadName}:\nkey = ${headers.CamelAwsS3Key}, body:\n${body}")
				.split().tokenize("\n")
				.log("\n[direct:files2] ${threadName}:\nkey = ${headers.CamelAwsS3Key}, line: ${body}");
	}

	/**
	 * reading (using request builder) the list of files (names) and splitting to process 1 by 1
	 */
	public void configure3() {
		from("timer:number?period=10000")
				.setBody(ex -> ListObjectsRequest.builder().bucket(bucketName).build())
				.to("aws2-s3://{{bucketName}}?operation=listObjects&pojoRequest=true")
				.to(DEBUG_NO_HEADERS)
				.split(body())
				.log("\n[${threadName}] key = ${body.key}, size = ${body.size}");
	}

	/**
	 * reading the list of files (names) and splitting to process 1 by 1
	 */
	public void configure2() {
		from("timer:number?period=10000")
				.to("aws2-s3://{{bucketName}}?operation=listObjects")
				.to(DEBUG_NO_HEADERS)
				.split(body())
				.to(DEBUG_ALL)
				.process(exchange -> {
					S3Object s3Object = exchange.getIn().getBody(S3Object.class);
					log.debug("\nkey = {}, size = {}", s3Object.key(), s3Object.size());
				});
	}

	/**
	 * reading all files (+ their content) then processing 1 by 1
	 */
	public void configure1() {
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
