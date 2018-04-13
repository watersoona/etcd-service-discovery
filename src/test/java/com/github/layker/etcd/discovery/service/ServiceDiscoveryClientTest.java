package com.github.layker.etcd.discovery.service;

import com.github.layker.etcd.discovery.EtcdTestClusterConfiguration;
import com.github.layker.etcd.discovery.domain.AddressPair;
import com.github.layker.etcd.discovery.domain.ServiceDiscoveryRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ServiceDiscoveryClientTest extends EtcdTestClusterConfiguration {
    private final ServiceDiscoveryClient serviceDiscoveryClient = new ServiceDiscoveryClient(etcdClient);

    private final String serviceName = "test-service";
    private final String anotherServiceName = "another-test-service";

    @Before
    public void init() throws Exception {
        ServiceDiscoveryRecord record1 = ServiceDiscoveryRecord.newBuilder()
                .serviceName(serviceName)
                .labels("env=dev")
                .port(1)
                .build();
        ServiceDiscoveryRecord record2 = ServiceDiscoveryRecord.newBuilder()
                .serviceName(serviceName)
                .labels("env=uat")
                .port(2)
                .build();
        ServiceDiscoveryRecord record3 = ServiceDiscoveryRecord.newBuilder()
                .serviceName(serviceName)
                .port(3)
                .build();
        ServiceDiscoveryRecord record4 = ServiceDiscoveryRecord.newBuilder()
                .serviceName(anotherServiceName)
                .port(4)
                .build();
        ServiceDiscoveryRecord record5 = ServiceDiscoveryRecord.newBuilder()
                .serviceName(anotherServiceName)
                .port(5)
                .build();
//
        CompletableFuture.allOf(
                serviceDiscoveryClient.register(record1),
                serviceDiscoveryClient.register(record2),
                serviceDiscoveryClient.register(record3),
                serviceDiscoveryClient.register(record4),
                serviceDiscoveryClient.register(record5)




        ).get();

//        serviceDiscoveryClient.register(record1).get();
//                serviceDiscoveryClient.register(record2).get();
//                serviceDiscoveryClient.register(record3).get();
//                serviceDiscoveryClient.register(record4).get();
//                serviceDiscoveryClient.register(record5).get();
    }

    @Test
    public void shouldCorrectlyReturnAnyDcAddresses() throws Exception {
        List<AddressPair> addresses = serviceDiscoveryClient.getAddressesAnyLabel(serviceName).get();
        Assert.assertEquals(3, addresses.size());

        Thread.sleep(5000);
        etcdClient.keepAlive();
        while (true) {
            Thread.sleep(3000);
            System.out.println("sleeping");
//            etcdClient.keepAlive();
        }

    }

    @Test
    public void shouldCorrectlyReturnConcreteDcAddresses() throws Exception {
        List<AddressPair> addresses = serviceDiscoveryClient.getAddresses(serviceName, "env=dev").get();
        Assert.assertEquals(1, addresses.size());
    }
}
