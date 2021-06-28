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
package org.apache.camel.quarkus.component.redis.deployment;

import java.lang.reflect.Proxy;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;
import io.quarkus.deployment.builditem.nativeimage.UnsafeAccessedFieldBuildItem;
import org.jboss.logging.Logger;
import org.jboss.marshalling.ProviderDescriptor;
import org.jboss.marshalling.river.RiverProviderDescriptor;

/**
 * To go beyond a prototype, we would need:
 * + quarkus support for regular serialization use cases https://github.com/quarkusio/quarkus/issues/14530
 * + quarkus support for "customTargetConstructorClass" in case the DefaultExchangeHolder design changes ?
 *
 * The redis extensions needs an extra serialization-config.json file in order to serialize
 * <code>org.apache.camel.support.DefaultExchangeHolder</code> instances.
 * Under the hood, jboss-marshaller may generate some calls to
 * <code>sun.reflect.ReflectionFactory.newConstructorForSerialization(Constructor, Constructor)</code>.
 * Such non regular serialization uses cases are not detected by the agent. So, each time the DefaultExchangeHolder
 * structure would evolve, we would need to manually
 * register the needed serialization configs, including maybe use 'customTargetConstructorClass'.
 * More details here: https://github.com/oracle/graal/issues/3156
 */
class RedisProcessor {

    private static final Logger LOG = Logger.getLogger(RedisProcessor.class);
    private static final String FEATURE = "camel-redis";

    private static final String[] RUNTIME_INITIALIZED_CLASSES = new String[] {
            "io.netty.channel.DefaultChannelId",
            "io.netty.channel.socket.nio.ProtocolFamilyConverter$1",
            "io.netty.util.NetUtil",
            "io.netty.channel.socket.InternetProtocolFamily$1",
            "io.netty.channel.socket.InternetProtocolFamily",
            "io.netty.resolver.HostsFileEntriesResolver",
            "io.netty.resolver.dns.DnsNameResolver",
            "io.netty.resolver.dns.DnsServerAddressStreamProviders",
            "io.netty.resolver.dns.PreferredAddressTypeComparator$1",
            "io.netty.resolver.dns.DefaultDnsServerAddressStreamProvider",
            "io.netty.resolver.dns.DnsServerAddressStreamProviders$DefaultProviderHolder",
            "org.jboss.marshalling.river.RiverUnmarshaller"
    };

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    ServiceProviderBuildItem registerJbossMarshallingProvider() {
        return new ServiceProviderBuildItem(ProviderDescriptor.class.getName(), RiverProviderDescriptor.class.getName());
    }

    @BuildStep
    UnsafeAccessedFieldBuildItem registerProxyHFieldUnsafeAccess() {
        // RiverUnmarshaller static initializer uses unsafe access to the Proxy.h field
        return new UnsafeAccessedFieldBuildItem(Proxy.class.getName(), "h");
    }

    @BuildStep
    void registerReflectiveClasses(BuildProducer<ReflectiveClassBuildItem> producer) {
        producer.produce(new ReflectiveClassBuildItem(false, false, "org.jboss.marshalling.river.RiverProviderDescriptor"));
        producer.produce(new ReflectiveClassBuildItem(false, false, "io.netty.channel.socket.nio.NioDatagramChannel"));

        // Needed by the RiverUnmarshaller to deserialize DefaultExchangeHolder.inHeaders
        producer.produce(new ReflectiveClassBuildItem(false, true, "org.apache.camel.support.DefaultExchangeHolder"));
        producer.produce(new ReflectiveClassBuildItem(true, false, "java.util.LinkedHashMap"));

        // The snippet below tries to register fields from DefaultExchangeHolder
        // however, we would need to register the concrete type (that may be determined at runtime only ?)
        /*for (Field field : DefaultExchangeHolder.class.getDeclaredFields()) {
            if (!field.getType().isPrimitive()) {
                System.out.println("Registering serializable class as reflective: " + field.getType().getName());
                producer.produce(new ReflectiveClassBuildItem(true, false, field.getType().getName()));
            }
        }*/

        // RiverUnmarshaller L.108
        // Looks to be used in RiverUnmarshaller.doReadNewObject(...)
        // when unmarshalling a proxy class ? Is this covered by tests ? Looks not... maybe cut this functionality with a substitution ?
        // Seems we still have a native build issue with this. Let's have a look.
        // Really needed ? as we register Proxy.h as an unsafe accessed field above ?
        producer.produce(new ReflectiveClassBuildItem(false, true, Proxy.class));
    }

    @BuildStep
    void registerRuntimeInitializedClasses(BuildProducer<RuntimeInitializedClassBuildItem> producer) {
        for (String className : RUNTIME_INITIALIZED_CLASSES) {
            producer.produce(new RuntimeInitializedClassBuildItem(className));
        }
    }

}
