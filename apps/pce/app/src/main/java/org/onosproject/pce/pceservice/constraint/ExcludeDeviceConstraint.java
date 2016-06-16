/*
 * Copyright 2016-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.pce.pceservice.constraint;

import org.onosproject.net.Device;
import org.onosproject.net.Link;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.intent.ResourceContext;
import org.onosproject.net.intent.constraint.BooleanConstraint;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Constraint that evaluates whether links satisfies excluding the device request.
 */
public final class ExcludeDeviceConstraint extends BooleanConstraint {

    private final List<Device> devices;

    // Constructor for serialization
    private ExcludeDeviceConstraint() {
        devices = null;

    }

    /**
     * Creates a new exclude device constraint.
     *
     * @param devices exclude devices
     */
    public ExcludeDeviceConstraint(List<Device> devices) {
        this.devices = devices;
    }

    /**
     * Creates a new exclude device constraint.
     *
     * @param devices exclude devices
     */
    public static ExcludeDeviceConstraint of(List<Device> devices) {
        return new ExcludeDeviceConstraint(devices);
    }

    /**
     * Obtains exclude devices.
     *
     * @return exclude devices
     */
    public List<Device> devices() {
        return devices;
    }

    @Override
    public boolean isValid(Link link, ResourceContext context) {
        return false;
        //Do nothing instead using isValidLink needs device service to validate link
    }

    /**
     * Validates the link based on exclude device constraint.
     *
     * @param link to validate source and destination based on exclude device constraint
     * @param deviceService instance of DeviceService
     * @return true if link satisfies exclude device constraint otherwise false
     */
    public boolean isValidLink(Link link, DeviceService deviceService) {
        if (deviceService == null) {
            return false;
        }

        Device srcDevice = deviceService.getDevice(link.src().deviceId());
        Device dstDevice = deviceService.getDevice(link.dst().deviceId());

        if (srcDevice == null || dstDevice == null) {
            return false;
        }

        // If link source or destination is part of exclude list then not a valid link
        return !(devices.contains(srcDevice) || devices.contains(dstDevice));

    }

    @Override
    public int hashCode() {
        return Objects.hash(devices);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof ExcludeDeviceConstraint) {
            ExcludeDeviceConstraint other = (ExcludeDeviceConstraint) obj;
            return Objects.equals(this.devices, other.devices);
        }

        return false;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("devices", devices)
                .toString();
    }
}