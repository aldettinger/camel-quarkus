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

import com.aliyun.mns.common.ServiceException;
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
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.alibaba.mns.constants.MNSHeaders;

@Path("/alibaba-mns")
@ApplicationScoped
public class AlibabaMnsResource {

    private static final String MNS_COMMON_OPTS = "&mnsClient=#mnsClient&accessKey=test&secretKey=test"
            + "&accountEndpoint=http://placeholder&region=us-east-1";

    @Inject
    ProducerTemplate producerTemplate;

    @Inject
    ConsumerTemplate consumerTemplate;

    @Path("/queue/{queueName}/send")
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendMessage(@PathParam("queueName") String queueName, String body) {
        try {
            String uri = "alibaba-mns:test?operation=sendMessage&queueName=" + queueName + MNS_COMMON_OPTS;
            Exchange result = producerTemplate.request(uri, exchange -> exchange.getIn().setBody(body));
            if (result.getException() instanceof ServiceException) {
                return Response.status(500).entity(((ServiceException) result.getException()).getErrorCode()).build();
            } else if (result.getException() != null) {
                return Response.status(500).entity(result.getException().getMessage()).build();
            }
            String messageId = result.getProperty(MNSHeaders.MESSAGE_ID, String.class);
            return Response.ok().entity(messageId != null ? messageId : "").build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @Path("/queue/{queueName}/delete/{receiptHandle}")
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteMessage(@PathParam("queueName") String queueName,
            @PathParam("receiptHandle") String receiptHandle) {
        try {
            String uri = "alibaba-mns:test?operation=deleteMessage&queueName=" + queueName + MNS_COMMON_OPTS;
            producerTemplate.requestBodyAndHeader(uri, null, MNSHeaders.RECEIPT_HANDLE, receiptHandle);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @Path("/topic/{topicName}/publish")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response publishMessage(@PathParam("topicName") String topicName, String body) {
        try {
            String uri = "alibaba-mns:test?operation=publishMessage&topicName=" + topicName + MNS_COMMON_OPTS;
            Exchange result = producerTemplate.request(uri, exchange -> exchange.getIn().setBody(body));
            if (result.getException() != null) {
                return Response.status(500).entity(result.getException().getMessage()).build();
            }
            String messageId = result.getProperty(MNSHeaders.MESSAGE_ID, String.class);
            return Response.ok().entity(messageId != null ? messageId : "").build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @Path("/consumer")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response consumeMessage() {
        String body = consumerTemplate.receiveBody("seda:mnsConsumer", 10000L, String.class);
        if (body != null) {
            return Response.ok().entity(body).build();
        }
        return Response.noContent().build();
    }
}
