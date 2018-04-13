package com.github.layker.etcd.discovery.domain;

import com.google.common.annotations.Beta;

@Beta
public class ServiceDiscoveryRecord {
    private final String instanceId;
    private final String serviceName;
    private final String labels;
    private final AddressPair address;

    private ServiceDiscoveryRecord(String instanceId, String serviceName, String labels, AddressPair address) {
        this.instanceId = instanceId;
        this.serviceName = serviceName;
        this.labels = labels;
        this.address = address;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getLabels() {
        return labels;
    }

    public AddressPair getAddress() {
        return address;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public static class Builder {
        private String serviceName;
        private String labels = "";
        private String instanceId;

        private int port = 80;
        private boolean preferIpAddress = true;

        private Builder() {
        }

        public ServiceDiscoveryRecord build() {
            return new ServiceDiscoveryRecord(
                    instanceId,
                    serviceName,
                    labels,
                    AddressPair.of(HostDetectionUtils.getAddress(preferIpAddress), port)
            );
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder labels(String labels) {
            this.labels = labels;
            return this;
        }

        public Builder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }



        public Builder preferIpAddress(boolean preferIpAddress) {
            this.preferIpAddress = preferIpAddress;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }
    }
}
