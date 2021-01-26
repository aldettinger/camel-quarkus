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
/*
package org.redisson.client;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.resolver.dns.DnsAddressResolverGroup;
import io.netty.resolver.dns.DnsServerAddressStreamProviders;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import org.redisson.api.RFuture;
import org.redisson.client.handler.RedisChannelInitializer.Type;
import org.redisson.misc.RedisURI;
import org.redisson.misc.RedissonPromise;

@TargetClass(RedisClient.class)
final class RedisClientAlias {

    @Alias
    private AtomicReference<RFuture<InetSocketAddress>> resolvedAddrFuture;

    @Alias
    private Bootstrap bootstrap;
    @Alias
    private Bootstrap pubSubBootstrap;
    @Alias
    private RedisURI uri;
    @Alias
    private InetSocketAddress resolvedAddr;
    @Alias
    private ChannelGroup channels;

    @Alias
    private ExecutorService executor;
    @Alias
    private long commandTimeout;
    @Alias
    private Timer timer;
    @Alias
    private RedisClientConfig config;

    @Alias
    private boolean hasOwnTimer;
    @Alias
    private boolean hasOwnExecutor;
    @Alias
    private boolean hasOwnGroup;
    @Alias
    private boolean hasOwnResolver;

    // @TODO: really needed ?
    @Substitute
    public static RedisClientAlias create(RedisClientConfig config) {
        return new RedisClientAlias(config);
    }

    @Substitute
    private RedisClientAlias(RedisClientConfig config) {
        RedisClientConfig copy = new RedisClientConfig(config);
        if (copy.getTimer() == null) {
            copy.setTimer(new HashedWheelTimer());
            hasOwnTimer = true;
        }
        if (copy.getGroup() == null) {
            copy.setGroup(new NioEventLoopGroup());
            hasOwnGroup = true;
        }
        if (copy.getExecutor() == null) {
            copy.setExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2));
            hasOwnExecutor = true;
        }
        if (copy.getResolverGroup() == null) {
            copy.setResolverGroup(
                    new DnsAddressResolverGroup(NioDatagramChannel.class, DnsServerAddressStreamProviders.platformDefault()));
            hasOwnResolver = true;
        }

        this.config = copy;
        this.executor = copy.getExecutor();
        this.timer = copy.getTimer();

        uri = copy.getAddress();
        resolvedAddr = copy.getAddr();

        // @TODO: This field should be initialized in target class RedisClient but it's actually not
        // Any wasy to do this more cleanly ?
        resolvedAddrFuture = new AtomicReference<RFuture<InetSocketAddress>>();

        if (resolvedAddr != null) {
            resolvedAddrFuture.set(RedissonPromise.newSucceededFuture(resolvedAddr));
        }

        channels = new DefaultChannelGroup(copy.getGroup().next());
        bootstrap = createBootstrap(copy, Type.PLAIN);
        pubSubBootstrap = createBootstrap(copy, Type.PUBSUB);

        this.commandTimeout = copy.getCommandTimeout();
    }

    @Alias
    private Bootstrap createBootstrap(RedisClientConfig config, Type type) {
        return null;
    }
}
*/
