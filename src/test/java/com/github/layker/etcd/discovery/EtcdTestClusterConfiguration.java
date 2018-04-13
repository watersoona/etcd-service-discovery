package com.github.layker.etcd.discovery;

import com.github.layker.etcd.discovery.etcd.EtcdClient;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.testcontainers.containers.GenericContainer;

public class EtcdTestClusterConfiguration {
    @ClassRule
    public static GenericContainer etcdContainer =
            new GenericContainer("quay.io/coreos/etcd:v3.2.5")
                    .withExposedPorts(2379, 2380)
                    .withCommand(
                            "/usr/local/bin/etcd " +
                                    "--name node1 " +
                                    "--listen-peer-urls http://0.0.0.0:2380 " +
                                    "--listen-client-urls http://0.0.0.0:2379 " +
                                    "--advertise-client-urls http://0.0.0.0:2379 " +
                                    "--initial-cluster node1=http://0.0.0.0:2380 " +
                                    "--initial-advertise-peer-urls http://0.0.0.0:2380"
                    );

    protected static EtcdClient etcdClient;

    @BeforeClass
    public static void initClient() {
//
//        etcdContainer.getContainerIpAddress(),
//                etcdContainer.getMappedPort(2379)
        etcdClient = EtcdClient.newBuilder()
                .ttlSeconds(5)
                .endpoint(
                        "192.168.20.202", 2379
                ).build();
    }
}
