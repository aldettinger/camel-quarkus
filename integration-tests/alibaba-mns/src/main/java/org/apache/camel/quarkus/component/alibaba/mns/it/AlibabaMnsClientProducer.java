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

import com.aliyun.mns.client.MNSClient;
import com.aliyun.mns.client.MNSClientBuilder;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.ConfigProvider;

public class AlibabaMnsClientProducer {

    public static final String MNS_ENDPOINT_URL = "alibaba.mns.endpoint.url";

    @Produces
    @Singleton
    @Named("mnsClient")
    public MNSClient produceMnsClient() {
        String endpoint = ConfigProvider.getConfig().getValue(MNS_ENDPOINT_URL, String.class);
        return MNSClientBuilder.create()
                .accessKeyId("test-access-key")
                .accessKeySecret("test-secret-key")
                .accountEndpoint(endpoint)
                .region("us-east-1")
                .build();
    }
}
