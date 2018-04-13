package com.github.layker.etcd.discovery.etcd;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.KV;
import com.coreos.jetcd.Lease;
import com.coreos.jetcd.Watch;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.kv.PutResponse;
import com.coreos.jetcd.lease.LeaseKeepAliveResponse;
import com.coreos.jetcd.options.GetOption;
import com.coreos.jetcd.options.PutOption;
import com.coreos.jetcd.options.WatchOption;
import com.coreos.jetcd.watch.WatchEvent;
import com.google.common.annotations.Beta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Beta
public class EtcdClient implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(EtcdClient.class);

    private final Client etcdClient;
    private final KV kvClient;
    private final Lease leaseClient;
    private final long ttl;
    private final Watch watchClient;

    private Long leaseId;
    public Lease.KeepAliveListener keepAlive;

    private EtcdClient(Collection<String> endpoints, long ttl) {
        etcdClient = Client.builder().endpoints(endpoints).build();
        kvClient = etcdClient.getKVClient();
        leaseClient = etcdClient.getLeaseClient();
        watchClient = etcdClient.getWatchClient();
        this.ttl = ttl;
    }


    public void keepAlive() {
        try {
            keepAlive = leaseClient.keepAlive(leaseId);
            LeaseKeepAliveResponse response = keepAlive.listen();
            log.info("LeaseKeepAliveResponse:id:{}-ttl:{}", response.getID(), response.getTTL());
        } catch (Exception e) {
            e.printStackTrace();
            keepAlive.close();
        }
    }


    public void closeKeepAlive() {
        try {
            keepAlive.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public CompletableFuture<PutResponse> put(String key, String value) {
        return leaseClient
                .grant(ttl)
                .thenCompose(leaseGrantResponse -> {
                    leaseId = leaseGrantResponse.getID();
                    keepAlive = leaseClient.keepAlive(leaseId);
                    ByteSequence keyByteSequence = ByteSequence.fromString(key);
                    ByteSequence valueByteSequence = ByteSequence.fromString(value);
                    CompletableFuture<PutResponse> putResponseCompletableFuture = kvClient.put(
                            keyByteSequence,
                            valueByteSequence,
                            PutOption.newBuilder()
                                    .withLeaseId(leaseId)
                                    .build()
                    );
                    return putResponseCompletableFuture;
                });
    }

    public CompletableFuture<GetResponse> getAllByPrefix(String prefix) {
        ByteSequence prefixByteSequence = ByteSequence.fromString(prefix);
        GetOption prefixOption = GetOption.newBuilder()
                .withPrefix(prefixByteSequence)
                .build();
        return kvClient.get(prefixByteSequence, prefixOption);
    }

    //region watch option
    public void watch(String key, Consumer<List<WatchEvent>> consumer) {
        Watch.Watcher watch = watchClient.watch(ByteSequence.fromString(key));
        WatchConsumer.build(watch, consumer).apply();
    }

    public void watch(String key, String prefix, Consumer<List<WatchEvent>> consumer) {
        Watch.Watcher watch = getWatcher(key, prefix, false, false);
        WatchConsumer.build(watch, consumer).apply();
    }

    public void watch(String key, String prefix, boolean noDelete, boolean noPut, Consumer<List<WatchEvent>> consumer) {
        Watch.Watcher watch = getWatcher(key, prefix, noDelete, noPut);
        WatchConsumer.build(watch, consumer).apply();
    }

    private Watch.Watcher getWatcher(String key, String prefix, boolean noDelete, boolean noPut) {
        WatchOption watchOption = WatchOption.newBuilder()
                .withPrefix(ByteSequence.fromString(prefix))
                .withNoDelete(noDelete)
                .withNoPut(noPut)
                .build();
        return watchClient.watch(ByteSequence.fromString(key), watchOption);
    }

    static class WatchConsumer {
        private Watch.Watcher watcher;
        private Consumer<List<WatchEvent>> consumer;

        public WatchConsumer(Watch.Watcher watcher, Consumer<List<WatchEvent>> consumer) {
            this.watcher = watcher;
            this.consumer = consumer;
        }

        public void apply() {
            try {
                com.coreos.jetcd.watch.WatchResponse watchResponse = watcher.listen();
                List<WatchEvent> events = watchResponse.getEvents();
                consumer.accept(events);
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error("watch exception : {}", e.getMessage());
                watcher.close();
            }
        }

        public static WatchConsumer build(Watch.Watcher watcher, Consumer<List<WatchEvent>> consumer) {
            return new WatchConsumer(watcher, consumer);
        }
    }
    //endregion

    @Override
    public void close() throws ExecutionException, InterruptedException {
        if (leaseId != null) {
            log.info("Revoking lease with id={}", leaseId);
            leaseClient.revoke(leaseId).get();
        }
        etcdClient.close();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String scheme = "http";
        private Set<String> endpoints = new HashSet<>();
        private long ttl = 5;

        private Builder() {
        }

        public EtcdClient build() throws IllegalStateException {
            if (endpoints.isEmpty()) {
                throw new IllegalStateException("Provide at least one endpoint!");
            }
            return new EtcdClient(
                    endpoints.stream()
                            .map(endpoint -> scheme + "://" + endpoint)
                            .collect(Collectors.toList()),
                    ttl
            );
        }

        public Builder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder endpoint(
                @Nonnull String address,
                int port
        ) {
            this.endpoints.add(address + ":" + port);
            return this;
        }

        public Builder ttlSeconds(long ttlSeconds) {
            this.ttl = ttlSeconds;
            return this;
        }
    }
}
