package com.github.layker.etcd.discovery.service;

import com.coreos.jetcd.kv.PutResponse;
import com.github.layker.etcd.discovery.EtcdTestClusterConfiguration;
import com.github.layker.etcd.discovery.common.KeyGenerator;
import com.github.layker.etcd.discovery.domain.ServiceDiscoveryRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author layker
 * @Description: ${TODO}
 * @date 2018/4/10 下午7:34
 */
public class ServiceHeartBeatClientTest extends EtcdTestClusterConfiguration {

    private ServiceHeartBeatClient serviceHeartBeatClient = new ServiceHeartBeatClient(etcdClient);

    private ServiceDiscoveryClient serviceDiscoveryClient = new ServiceDiscoveryClient(etcdClient);

    private final String serviceName = "test_service_ng";
    private final String anotherServiceName = "test_service_java";


    private String prefix = "";

    private final List<String> putRes = new ArrayList<>();

    @Before
    public void init() throws ExecutionException, InterruptedException {

        ServiceDiscoveryRecord record1 = ServiceDiscoveryRecord.newBuilder()
                .serviceName(serviceName)
                .labels("env=dev")
                .port(1)
                .instanceId("0001")
                .build();
        ServiceDiscoveryRecord record2 = ServiceDiscoveryRecord.newBuilder()
                .serviceName(serviceName)
                .labels("env=uat")
                .port(2)
                .instanceId("00002")
                .build();
        ServiceDiscoveryRecord record3 = ServiceDiscoveryRecord.newBuilder()
                .serviceName(serviceName)
                .labels("env=uat,lang=java")
                .port(3)
                .instanceId("000003")
                .build();
        ServiceDiscoveryRecord record4 = ServiceDiscoveryRecord.newBuilder()
                .serviceName(anotherServiceName)
                .labels("env=dev,lang=java")
                .port(4)
                .instanceId("000004")
                .build();
        ServiceDiscoveryRecord record5 = ServiceDiscoveryRecord.newBuilder()
                .serviceName(anotherServiceName)
                .labels("env=uat,lang=php")
                .port(5)
                .instanceId("000005")
                .build();

        PutResponse response1 = serviceDiscoveryClient.register(record1).get();
        PutResponse response2 = serviceDiscoveryClient.register(record2).get();
        PutResponse response3 = serviceDiscoveryClient.register(record3).get();
        PutResponse response4 = serviceDiscoveryClient.register(record4).get();
        PutResponse response5 = serviceDiscoveryClient.register(record5).get();
        putRes.add(response1.getPrevKv().getKey().toStringUtf8());
        putRes.add(response2.getPrevKv().getKey().toStringUtf8());
        putRes.add(response3.getPrevKv().getKey().toStringUtf8());
        putRes.add(response4.getPrevKv().getKey().toStringUtf8());
        putRes.add(response5.getPrevKv().getKey().toStringUtf8());
        System.out.println(putRes);
        prefix = KeyGenerator.buildServiceKeyWithoutLabels(serviceName);
        Assert.assertEquals(5, putRes.size());
    }


    @Test
    public void watch() {
        putRes.forEach(s -> {
            serviceHeartBeatClient.watch(s);
        });
    }

    @Test
    public void watchPrefix() {
        serviceHeartBeatClient.watch(prefix);
    }
}