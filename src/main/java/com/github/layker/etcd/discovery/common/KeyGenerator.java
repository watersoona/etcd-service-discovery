package com.github.layker.etcd.discovery.common;

import com.github.layker.etcd.discovery.domain.AddressPair;

import static com.github.layker.etcd.discovery.common.Constants.DELIMITER;
import static com.github.layker.etcd.discovery.common.Constants.ETCD_NAMESPACE;

/**
 * @author layker
 * @Description: key 生成器
 * @date 2018/4/11 下午3:45
 */
public class KeyGenerator {

    public static String buildServiceKeyWithLabels(String serviceName, String labels) {
        return buildServiceKeyWithoutLabels(serviceName) + labels + DELIMITER;
    }

    public static String buildServiceKeyWithoutLabels(String serviceName) {
        return ETCD_NAMESPACE + DELIMITER + serviceName + DELIMITER;
    }

    public static String buildServiceKey(String serviceName, String labels, AddressPair address) {
        return buildServiceKeyWithLabels(serviceName, labels) + address.getAddress() + DELIMITER + address.getPort();
    }

    public static String buildServiceKey(String serviceName, String labels, AddressPair address, String instanceId) {
        return buildServiceKeyWithLabels(serviceName, labels) + address.getAddress() + DELIMITER + address.getPort() + DELIMITER + instanceId;
    }
}
