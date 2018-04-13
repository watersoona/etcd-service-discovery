package com.github.layker.etcd.discovery.service;

import com.github.layker.etcd.discovery.etcd.EtcdClient;

import java.util.function.Consumer;

/**
 * @author layker
 * @Description: 服务心跳
 * @date 2018/4/10 下午5:42
 */
public class ServiceHeartBeatClient {

    private final EtcdClient etcdClient;

    public ServiceHeartBeatClient(EtcdClient etcdClient) {
        this.etcdClient = etcdClient;
    }

    public void keepHeartBeat(String key) {
        etcdClient.keepAlive();
    }

    public void keepHeartBeat(String key, Consumer callback) {
        etcdClient.keepAlive();
    }

    public Boolean watch(String key) {
        etcdClient.watch(key, (events) -> {
            System.out.println(events);
        });
        return false;
    }

    public Boolean watch(String key, String prefix) {
        etcdClient.watch(key, prefix, false, false, (events -> {
            System.out.println(events);
        }));
        return false;
    }
}
