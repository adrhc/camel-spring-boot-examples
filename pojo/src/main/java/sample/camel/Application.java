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

import org.apache.camel.component.seda.LinkedBlockingQueueFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.SynchronousQueue;

//CHECKSTYLE:OFF

/**
 * A sample Spring Boot application that starts the Camel routes.
 */
@SpringBootApplication
public class Application {

    /**
     * A main method to start this application.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /*@Bean
    CamelContextConfiguration camelContextConfiguration() {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext camelContext) {
                SedaComponent sedaComponent = new SedaComponent();
                sedaComponent.setQueueSize(1);
                sedaComponent.setDefaultBlockWhenFull(true);
                camelContext.addComponent("synchronousQueue", sedaComponent);
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
                // do nothing
            }
        };
    }*/

    @Bean
    SynchronousQueue<Object> synchronousQueue() {
        return new SynchronousQueue<>();
    }
}
//CHECKSTYLE:ON
