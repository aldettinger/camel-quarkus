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
package org.apache.camel.quarkus.component.alibaba.oss.it;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@QuarkusTestResource(AlibabaOssTestResource.class)
class AlibabaOssTest {

    private static final String BUCKET = "test-bucket";

    @Test
    void listBucketsShouldReturnBucketNames() {
        RestAssured.given()
                .get("/alibaba-oss/buckets")
                .then()
                .statusCode(200)
                .body(containsString("list-bucket-alpha"))
                .body(containsString("list-bucket-beta"));
    }

    @Test
    void putObjectShouldAcceptContent() {
        RestAssured.given()
                .contentType(ContentType.TEXT)
                .body("Put test content")
                .put("/alibaba-oss/object/" + BUCKET + "/put-test.txt")
                .then()
                .statusCode(200);
    }

    @Test
    void getObjectShouldReturnCorrectBody() {
        RestAssured.given()
                .get("/alibaba-oss/object/" + BUCKET + "/get-test.txt")
                .then()
                .statusCode(200)
                .body(is("Get test content"));
    }

    @Test
    void deleteObjectShouldReturn204() {
        RestAssured.given()
                .delete("/alibaba-oss/object/" + BUCKET + "/delete-test.txt")
                .then()
                .statusCode(204);
    }

    @Test
    void listObjectsShouldReturnObjectKeys() {
        RestAssured.given()
                .get("/alibaba-oss/objects/" + BUCKET)
                .then()
                .statusCode(200)
                .body(containsString("list-file-alpha.txt"))
                .body(containsString("list-file-beta.txt"));
    }

    @Test
    void copyObjectShouldSucceed() {
        RestAssured.given()
                .post("/alibaba-oss/object/copy/" + BUCKET + "/copy-source.txt/" + BUCKET + "/copy-dest.txt")
                .then()
                .statusCode(200);
    }

    @Test
    void headObjectShouldReturnContentType() {
        RestAssured.given()
                .get("/alibaba-oss/object/head/" + BUCKET + "/head-test.txt")
                .then()
                .statusCode(200)
                .body(is("text/plain"));
    }

    @Test
    void getObjectOnMissingKeyShouldReturnErrorDetails() {
        RestAssured.given()
                .get("/alibaba-oss/object/error-bucket/no-such-key.txt")
                .then()
                .statusCode(500)
                .body(containsString("NoSuchKey"));
    }

    @Test
    void consumerShouldReceiveObjectContent() {
        RestAssured.given()
                .get("/alibaba-oss/consumer")
                .then()
                .statusCode(200)
                .body(is("Consumer test content!"));
    }
}
