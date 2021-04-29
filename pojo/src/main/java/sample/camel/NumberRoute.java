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
package sample.camel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.csv.CsvDataFormat;
import org.apache.camel.model.dataformat.BindyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static org.apache.camel.builder.AggregationStrategies.flexible;
import static sample.camel.AppUtils.lines;
import static sample.camel.SlowService.createSlowService;

@Component
@RequiredArgsConstructor
@Slf4j
public class NumberRoute extends RouteBuilder {
    private static final int THREADS = 4;
    private static CompletableFuture<Exchange> futureLines;
    @Autowired
    private final ProducerTemplate producer;

    /**
     * closest approach
     */
    @Override
    public void configure() throws Exception {
        from("direct:start")
                .log("\n[threadName = ${threadName}] from direct-start:\n${body.length}")
                .split().tokenize("\n", 10).streaming()
                .log("\n[threadName = ${threadName}] lines in chunk ${exchangeProperty.CamelSplitIndex}:\n${body}")
                .split(body().tokenize("\n"), flexible().accumulateInCollection(ArrayList.class))
                .executorService(() -> Executors.newFixedThreadPool(THREADS))
                .unmarshal().bindy(BindyType.Csv, Person.class)
//                .to("log:DEBUG?multiline=true")
                .bean(createSlowService(1000))
                .end()
                .to("log:DEBUG?multiline=true");
    }

    private static CsvDataFormat csv() {
        return new CsvDataFormat()
                .setDelimiter(',')
                .setRecordSeparator("\n")
                .setIgnoreEmptyLines(true);
    }

    public void configure5() throws Exception {
        from("direct:start")
                .log("\n[threadName = ${threadName}] from direct-start:\n${body.length}")
                .split().tokenize("\n", 8).streaming()
                .log("\n[threadName = ${threadName}] lines in chunk ${exchangeProperty.CamelSplitIndex}:\n${body}")
                .to("seda:csv?blockWhenFull=true");

        from("direct:lines")
                .split(body().tokenize("\n"), flexible().accumulateInCollection(ArrayList.class))
                .executorService(() -> Executors.newFixedThreadPool(THREADS))
                .bean(createSlowService(2000))
                .end()
                .to("log:DEBUG?multiline=true");

        from("seda:csv?queue=#synchronousQueue&pollTimeout=1000")
                .process((exchange) -> {
                    producer.send("direct:lines", exchange);
                });

//        from("seda:csv?pollTimeout=1000&waitForTaskToComplete=Always&timeout=0").to("direct:lines");
//        from("seda:csv?pollTimeout=1000").to("direct:lines");
    }

    /**
     * best approach
     */
    public void configure4() throws Exception {
        from("direct:start")
                .log("\n[threadName = ${threadName}] from direct-start:\n${body.length}")
                .split().tokenize("\n", 8).streaming()
                .log("\n[threadName = ${threadName}] lines in chunk ${exchangeProperty.CamelSplitIndex}:\n${body}")
                .process((exchange) -> {
                    if (futureLines != null) {
                        futureLines.join();
//                        final Exchange previousLines = futureLines.get();
//                        log.debug("\n{}", previousLines.getIn().getBody());
                    }
                    futureLines = producer.asyncSend("direct:lines", exchange);
                });

        from("direct:lines")
                .split(body().tokenize("\n"), flexible().accumulateInCollection(ArrayList.class))
                .executorService(() -> Executors.newFixedThreadPool(THREADS))
                .bean(createSlowService(2000))
                .end()
                .to("log:DEBUG?multiline=true");
    }

    public void configure3() throws Exception {
        from("direct:start")
                .startupOrder(1)
                .log("\n[threadName = ${threadName}] from direct-start:\n${body.length}")
                .split().tokenize("\n", THREADS * 2).streaming()
                .log("\n[threadName = ${threadName}] chunk ${exchangeProperty.CamelSplitIndex}:\n${body}")
                .to("seda:csv?size=1&blockWhenFull=true");

        from("seda:csv?pollTimeout=2000")
                .startupOrder(2)
                .split(body().tokenize("\n"), flexible().accumulateInCollection(ArrayList.class))
                .executorService(() -> Executors.newFixedThreadPool(THREADS))
                .bean(createSlowService(4000))
                .end()
                .to("log:DEBUG?multiline=true");
    }

    /**
     * closest approach
     */
    public void configure2() throws Exception {
        from("direct:start")
                .log("\n[threadName = ${threadName}] from direct-start:\n${body.length}")
                .split().tokenize("\n", 10).streaming()
                .log("\n[threadName = ${threadName}] lines in chunk ${exchangeProperty.CamelSplitIndex}:\n${body}")
                .split(body().tokenize("\n"), flexible().accumulateInCollection(ArrayList.class))
                .executorService(() -> Executors.newFixedThreadPool(THREADS))
                .unmarshal(csv())
//                .to("log:DEBUG?multiline=true&showHeaders=true&showProperties=true")
//                .setBody(spel("#{body[0]}"))
//                .transform().simple("${body[0]} - ${body[last]}}")
//                .setBody(simple("${body[0]} - ${body[last]}"))
                .setBody(simple("${body[0]}")) // csv sets the body as an Array of lines
                .setBody(simple("${body[0]} - ${body[last]}"))
//                .to("log:DEBUG?multiline=true&showHeaders=true&showProperties=true")
                .bean(createSlowService(1000))
                .end()
                .to("log:DEBUG?multiline=true");
    }

    public void configure1() throws Exception {
        // generate random number every second
        // which is send to this seda queue that the NumberPojo will consume
/*
        from("timer:number?period=1000")
                .transform().simple("${random(0,200)}")
                .log("[${threadName}] sending to direct:numbers ${body}")
                .to("direct:numbers");
*/
        from("timer:number?period=1000")
                .startupOrder(1)
                .transform().simple(lines(2))
//                .log("[threadName = ${threadName}] timer sending:\n${body}")
                .bean(createSlowService(3000))
//                .to("synchronousQueue:csv");
//                .to("seda:csv");
//                .to("seda:csv?size=1&blockWhenFull=true");
                .to("seda:csv?blockWhenFull=true");
//                .to("seda:csv?blockWhenFull=true&waitForTaskToComplete=Always&timeout=0");
//                .to("seda:csv?size=1&blockWhenFull=true&waitForTaskToComplete=Always&timeout=0");

//        from("synchronousQueue:csv?pollTimeout=2000")
//        from("seda:csv?pollTimeout=2000")
//        from("seda:csv?pollTimeout=2000")
        from("seda:csv?queue=#synchronousQueue&pollTimeout=2000")
//        from("seda:csv?queue=#synchronousQueue&pollTimeout=2000")
//        from("seda:csv?pollTimeout=2000")
                .startupOrder(2)
//                .log("[threadName = ${threadName}] split will receive:\n${body}")
//                .split(body(), "\n")
                .split(body().tokenize("\n"), flexible().accumulateInCollection(ArrayList.class))
                .executorService(() -> Executors.newFixedThreadPool(2))
//                .log("[threadName = ${threadName}] slow consumer will receive:\n${body}")
                .bean(createSlowService(8000))
                .end()
                .to("log:DEBUG?multiline=true");
    }
}
