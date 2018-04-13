package com.github.layker.etcd.discovery.domain;

import com.github.layker.etcd.discovery.utils.InetUtils;
import com.github.layker.etcd.discovery.utils.InetUtilsProperties;

class HostDetectionUtils {
    private static final InetUtils inetUtils = new InetUtils(new InetUtilsProperties());

    static String getAddress(boolean preferIpAddress) {
        InetUtils.HostInfo hostInfo = inetUtils.findFirstNonLoopbackHostInfo();
        if (preferIpAddress) {
            return hostInfo.getIpAddress();
        } else {
            return hostInfo.getHostname();
        }
    }
}
