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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.alibaba.oss.constants.OSSProperties;

@Path("/alibaba-oss")
@ApplicationScoped
public class AlibabaOssResource {

    @Inject
    ProducerTemplate producerTemplate;

    @Inject
    ConsumerTemplate consumerTemplate;

    @Path("/object/{bucketName}/{objectName}")
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response putObject(@PathParam("bucketName") String bucketName,
            @PathParam("objectName") String objectName,
            String body) {
        try {
            String uri = "alibaba-oss:" + bucketName + "?operation=putObject&ossClient=#ossClient";
            producerTemplate.requestBodyAndHeader(uri, body, OSSProperties.OBJECT_NAME, objectName);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @Path("/object/{bucketName}/{objectName}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getObject(@PathParam("bucketName") String bucketName,
            @PathParam("objectName") String objectName) {
        try {
            String uri = "alibaba-oss:" + bucketName + "?operation=getObject&ossClient=#ossClient";
            byte[] result = producerTemplate.requestBodyAndHeader(uri, null,
                    OSSProperties.OBJECT_NAME, objectName, byte[].class);
            return Response.ok().entity(new String(result)).build();
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            return Response.status(500).entity(cause.getMessage()).build();
        }
    }

    @Path("/object/{bucketName}/{objectName}")
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteObject(@PathParam("bucketName") String bucketName,
            @PathParam("objectName") String objectName) {
        try {
            String uri = "alibaba-oss:" + bucketName + "?operation=deleteObject&ossClient=#ossClient";
            producerTemplate.requestBodyAndHeader(uri, null, OSSProperties.OBJECT_NAME, objectName);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @SuppressWarnings("unchecked")
    @Path("/objects/{bucketName}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response listObjects(@PathParam("bucketName") String bucketName) {
        try {
            String uri = "alibaba-oss:" + bucketName + "?operation=listObjects&ossClient=#ossClient";
            List<Map<String, Object>> result = producerTemplate.requestBody(uri, null, List.class);
            String names = result.stream()
                    .map(e -> String.valueOf(e.get("objectKey")))
                    .collect(Collectors.joining(", "));
            return Response.ok().entity(names).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @SuppressWarnings("unchecked")
    @Path("/buckets")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response listBuckets() {
        try {
            String uri = "alibaba-oss:whatever-bucket?operation=listBuckets&ossClient=#ossClient";
            List<Map<String, Object>> result = producerTemplate.requestBody(uri, null, List.class);
            String names = result.stream()
                    .map(e -> String.valueOf(e.get("name")))
                    .collect(Collectors.joining(", "));
            return Response.ok().entity(names).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @SuppressWarnings("unchecked")
    @Path("/object/copy/{srcBucket}/{srcObject}/{destBucket}/{destObject}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response copyObject(@PathParam("srcBucket") String srcBucket,
            @PathParam("srcObject") String srcObject,
            @PathParam("destBucket") String destBucket,
            @PathParam("destObject") String destObject) {
        try {
            String uri = "alibaba-oss:" + destBucket + "?operation=copyObject&ossClient=#ossClient";
            Map<String, Object> headers = Map.of(
                    OSSProperties.OBJECT_NAME, destObject,
                    OSSProperties.SOURCE_BUCKET_NAME, srcBucket,
                    OSSProperties.SOURCE_OBJECT_NAME, srcObject);
            Map<String, Object> result = (Map<String, Object>) producerTemplate.requestBodyAndHeaders(uri, null, headers);
            return Response.ok().entity(String.valueOf(result.get("statusCode"))).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @SuppressWarnings("unchecked")
    @Path("/object/head/{bucketName}/{objectName}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response headObject(@PathParam("bucketName") String bucketName,
            @PathParam("objectName") String objectName) {
        try {
            String uri = "alibaba-oss:" + bucketName + "?operation=headObject&ossClient=#ossClient";
            Map<String, Object> result = (Map<String, Object>) producerTemplate.requestBodyAndHeader(
                    uri, null, OSSProperties.OBJECT_NAME, objectName);
            return Response.ok().entity(String.valueOf(result.get("contentType"))).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @Path("/consumer")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response consumeMessage() {
        byte[] body = consumerTemplate.receiveBody("seda:ossConsumer", 10000L, byte[].class);
        if (body != null) {
            return Response.ok().entity(new String(body)).build();
        }
        return Response.noContent().build();
    }
}
