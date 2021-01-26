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
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.resolver.AddressResolverGroup;
import io.netty.resolver.DefaultAddressResolverGroup;
import io.netty.resolver.dns.DnsServerAddressStreamProviders;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.internal.PlatformDependent;
import org.redisson.ElementsSubscribeService;
import org.redisson.Version;
import org.redisson.api.NodeType;
import org.redisson.api.RFuture;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.command.CommandSyncService;
import org.redisson.config.Config;
import org.redisson.config.MasterSlaveServersConfig;
import org.redisson.connection.ConnectionEventsHub;
import org.redisson.connection.ConnectionManager;
import org.redisson.connection.IdleConnectionWatcher;
import org.redisson.connection.MasterSlaveConnectionManager;
import org.redisson.connection.MasterSlaveEntry;
import org.redisson.connection.NodeSource;
import org.redisson.misc.InfinitySemaphoreLatch;
import org.redisson.misc.RedisURI;
import org.redisson.pubsub.PublishSubscribeService;

@TargetClass(MasterSlaveConnectionManager.class)
// @TODO: Is there a cleaner way than IntrospectiorSubstitution/MasterSlaveConnectionManagerAlias to cut netty native transport code paths ?
final class MasterSlaveConnectionManagerAlias implements ConnectionManager {

    @Alias
    protected String id;
    @Alias
    protected Codec codec;
    @Alias
    private HashedWheelTimer timer;
    @Alias
    protected MasterSlaveServersConfig config;
    @Alias
    protected EventLoopGroup group;
    @Delete
    protected Class<? extends SocketChannel> socketChannelClass;
    @Alias
    private ExecutorService executor;
    @Alias
    private CommandSyncService commandExecutor;
    @Alias
    private Config cfg;
    @Alias
    protected AddressResolverGroup<InetSocketAddress> resolverGroup;

    @Substitute
    protected MasterSlaveConnectionManagerAlias(Config cfg, UUID id) {
        this.id = id.toString();
        Version.logVersion();

        if (cfg.getEventLoopGroup() == null) {
            this.group = new NioEventLoopGroup(cfg.getNettyThreads(), new DefaultThreadFactory("redisson-netty"));
        } else {
            this.group = cfg.getEventLoopGroup();
        }

        if (PlatformDependent.isAndroid()) {
            this.resolverGroup = DefaultAddressResolverGroup.INSTANCE;
        } else {
            this.resolverGroup = cfg.getAddressResolverGroupFactory().create(NioDatagramChannel.class,
                    DnsServerAddressStreamProviders.platformDefault());
        }

        if (cfg.getExecutor() == null) {
            int threads = Runtime.getRuntime().availableProcessors() * 2;
            if (cfg.getThreads() != 0) {
                threads = cfg.getThreads();
            }
            executor = Executors.newFixedThreadPool(threads, new DefaultThreadFactory("redisson"));
        } else {
            executor = cfg.getExecutor();
        }

        this.cfg = cfg;
        this.codec = cfg.getCodec();
        this.commandExecutor = new CommandSyncService(this);
    }

    @Substitute
    protected RedisClientConfig createRedisConfig(NodeType type, RedisURI address, int timeout, int commandTimeout,
            String sslHostname) {
        RedisClientConfig redisConfig = new RedisClientConfig();
        redisConfig.setAddress(address)
                .setTimer(timer)
                .setExecutor(executor)
                .setResolverGroup(resolverGroup)
                .setGroup(group)
                .setSocketChannelClass(NioSocketChannel.class)
                .setConnectTimeout(timeout)
                .setCommandTimeout(commandTimeout)
                .setSslHostname(sslHostname)
                .setSslEnableEndpointIdentification(config.isSslEnableEndpointIdentification())
                .setSslProvider(config.getSslProvider())
                .setSslTruststore(config.getSslTruststore())
                .setSslTruststorePassword(config.getSslTruststorePassword())
                .setSslKeystore(config.getSslKeystore())
                .setSslKeystorePassword(config.getSslKeystorePassword())
                .setClientName(config.getClientName())
                .setDecodeInExecutor(cfg.isDecodeInExecutor())
                .setKeepPubSubOrder(cfg.isKeepPubSubOrder())
                .setPingConnectionInterval(config.getPingConnectionInterval())
                .setKeepAlive(config.isKeepAlive())
                .setTcpNoDelay(config.isTcpNoDelay())
                .setUsername(config.getUsername())
                .setPassword(config.getPassword())
                .setNettyHook(cfg.getNettyHook());

        if (type != NodeType.SENTINEL) {
            redisConfig.setDatabase(config.getDatabase());
        }

        return redisConfig;
    }

    @Alias
    @Override
    public RedisURI applyNatMap(RedisURI address) {
        return null;
    }

    @Alias
    @Override
    public String getId() {
        return null;
    }

    @Alias
    @Override
    public CommandSyncService getCommandExecutor() {
        return null;
    }

    @Alias
    @Override
    public ElementsSubscribeService getElementsSubscribeService() {
        return null;
    }

    @Alias
    @Override
    public PublishSubscribeService getSubscribeService() {
        return null;
    }

    @Alias
    @Override
    public ExecutorService getExecutor() {
        return null;
    }

    @Alias
    @Override
    public RedisURI getLastClusterNode() {
        return null;
    }

    @Alias
    @Override
    public Config getCfg() {
        return null;
    }

    @Alias
    @Override
    public boolean isClusterMode() {
        return false;
    }

    @Alias
    @Override
    public ConnectionEventsHub getConnectionEventsHub() {
        return null;
    }

    @Alias
    @Override
    public boolean isShutdown() {
        return false;
    }

    @Alias
    @Override
    public boolean isShuttingDown() {
        return false;
    }

    @Alias
    @Override
    public IdleConnectionWatcher getConnectionWatcher() {
        return null;
    }

    @Alias
    @Override
    public int calcSlot(String key) {
        return 0;
    }

    @Alias
    @Override
    public int calcSlot(byte[] key) {
        return 0;
    }

    @Alias
    @Override
    public MasterSlaveServersConfig getConfig() {
        return null;
    }

    @Alias
    @Override
    public Codec getCodec() {
        return null;
    }

    @Alias
    @Override
    public Collection<MasterSlaveEntry> getEntrySet() {
        return null;
    }

    @Alias
    @Override
    public MasterSlaveEntry getEntry(String name) {
        return null;
    }

    @Alias
    @Override
    public MasterSlaveEntry getEntry(int slot) {
        return null;
    }

    @Alias
    @Override
    public MasterSlaveEntry getEntry(InetSocketAddress address) {
        return null;
    }

    @Alias
    @Override
    public void releaseRead(NodeSource source, RedisConnection connection) {
    }

    @Alias
    @Override
    public void releaseWrite(NodeSource source, RedisConnection connection) {
    }

    @Alias
    @Override
    public RFuture<RedisConnection> connectionReadOp(NodeSource source, RedisCommand<?> command) {
        return null;
    }

    @Alias
    @Override
    public RFuture<RedisConnection> connectionWriteOp(NodeSource source, RedisCommand<?> command) {
        return null;
    }

    @Alias
    @Override
    public RedisClient createClient(NodeType type, RedisURI address, int timeout, int commandTimeout, String sslHostname) {
        return null;
    }

    @Alias
    @Override
    public RedisClient createClient(NodeType type, InetSocketAddress address, RedisURI uri, String sslHostname) {
        return null;
    }

    @Alias
    @Override
    public RedisClient createClient(NodeType type, RedisURI address, String sslHostname) {
        return null;
    }

    @Alias
    @Override
    public MasterSlaveEntry getEntry(RedisClient redisClient) {
        return null;
    }

    @Alias
    @Override
    public void shutdown() {
    }

    @Alias
    @Override
    public void shutdown(long quietPeriod, long timeout, TimeUnit unit) {
    }

    @Alias
    @Override
    public EventLoopGroup getGroup() {
        return null;
    }

    @Alias
    @Override
    public Timeout newTimeout(TimerTask task, long delay, TimeUnit unit) {
        return null;
    }

    @Alias
    @Override
    public InfinitySemaphoreLatch getShutdownLatch() {
        return null;
    }

    @Alias
    @Override
    public Future<Void> getShutdownPromise() {
        return null;
    }

}
*/
