package com.github.layker.etcd.discovery.service;

import com.coreos.jetcd.kv.PutResponse;
import com.github.layker.etcd.discovery.common.KeyGenerator;
import com.github.layker.etcd.discovery.domain.AddressPair;
import com.github.layker.etcd.discovery.domain.Label;
import com.github.layker.etcd.discovery.domain.ServiceDiscoveryRecord;
import com.github.layker.etcd.discovery.etcd.EtcdClient;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Beta
public class ServiceDiscoveryClient {

    private static final Logger log = LoggerFactory.getLogger(ServiceDiscoveryClient.class);
    private final EtcdClient etcdClient;

    public ServiceDiscoveryClient(EtcdClient etcdClient) {
        this.etcdClient = etcdClient;
    }

    public CompletableFuture<PutResponse> register(@Nonnull ServiceDiscoveryRecord record) {
        String serviceName = record.getServiceName();
        String dc = record.getLabels();
        AddressPair address = record.getAddress();

        String key = KeyGenerator.buildServiceKey(serviceName, dc, address, record.getInstanceId());
        log.info("key:{}", key);
        return etcdClient.put(key, address.toString());
    }

    public CompletableFuture<List<AddressPair>> getAddresses(@Nonnull String serviceName, @Nonnull String labels) {
        validateServiceName(serviceName);
        Preconditions.checkArgument(labels != null && !labels.isEmpty(), "labels should be not-empty");

        return etcdClient
                .getAllByPrefix(KeyGenerator.buildServiceKeyWithLabels(serviceName, labels))
                .thenApply(getResponse -> getResponse.getKvs()
                        .stream()
                        .map(keyValue -> AddressPair.fromString(keyValue.getValue().toStringUtf8()))
                        .collect(Collectors.toList()));
    }

    public CompletableFuture<List<AddressPair>> getAddresses(@Nonnull String serviceName) {
        validateServiceName(serviceName);

        return etcdClient
                .getAllByPrefix(KeyGenerator.buildServiceKeyWithoutLabels(serviceName))
                .thenApply(getResponse -> getResponse.getKvs()
                        .stream()
                        .map(keyValue -> AddressPair.fromString(keyValue.getValue().toStringUtf8()))
                        .collect(Collectors.toList()));
    }

    public List<CompletableFuture<List<AddressPair>>> getAddresses(@Nonnull String serviceName, @Nonnull List<Label> labels) {
        //todo list -> One
        return labels.stream().map(l -> getAddresses(serviceName, l)).collect(Collectors.toList());
    }

    public CompletableFuture<List<AddressPair>> getAddresses(@Nonnull String serviceName, @Nonnull Label label) {
        return getAddresses(serviceName, label.formatStr());
    }

    public CompletableFuture<List<AddressPair>> getAddressesAnyLabel(@Nonnull String serviceName) {
        validateServiceName(serviceName);

        return etcdClient.getAllByPrefix(KeyGenerator.buildServiceKeyWithoutLabels(serviceName))
                .thenApply(getResponse -> getResponse.getKvs()
                        .stream()
                        .map(keyValue -> AddressPair.fromString(keyValue.getValue().toStringUtf8()))
                        .collect(Collectors.toList()));
    }

    private void validateServiceName(String serviceName) throws IllegalArgumentException {
        Preconditions.checkArgument(serviceName != null && !serviceName.isEmpty(), "serviceName should be not-empty");
    }

}
