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
package org.apache.camel.quarkus.component.alibaba.mns.it;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@QuarkusTest
@QuarkusTestResource(AlibabaMnsTestResource.class)
class AlibabaMnsTest {

    private static final String QUEUE = "test-queue";
    private static final String TOPIC = "test-topic";

    @Test
    void sendMessageShouldReturnMessageId() {
        RestAssured.given()
                .contentType(ContentType.TEXT)
                .body("Hello MNS")
                .put("/alibaba-mns/queue/" + QUEUE + "/send")
                .then()
                .statusCode(200)
                .body(not(emptyString()));
    }

    @Test
    void deleteMessageShouldReturn204() {
        RestAssured.given()
                .delete("/alibaba-mns/queue/" + QUEUE + "/delete/test-receipt-handle")
                .then()
                .statusCode(204);
    }

    @Test
    void publishMessageShouldReturnMessageId() {
        RestAssured.given()
                .contentType(ContentType.TEXT)
                .body("Hello Topic")
                .post("/alibaba-mns/topic/" + TOPIC + "/publish")
                .then()
                .statusCode(200)
                .body(not(emptyString()));
    }

    @Test
    void sendToNonExistentQueueShouldReturnServiceException() {
        RestAssured.given()
                .contentType(ContentType.TEXT)
                .body("Hello Error")
                .put("/alibaba-mns/queue/nonexistent-queue/send")
                .then()
                .statusCode(500)
                .body(is("QueueNotExist"));
    }

    @Test
    void consumerShouldReceiveMessage() {
        RestAssured.given()
                .get("/alibaba-mns/consumer")
                .then()
                .statusCode(200)
                .body(is("Consumer test content"));
    }
}
