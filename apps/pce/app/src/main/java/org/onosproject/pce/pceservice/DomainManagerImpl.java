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
package org.onosproject.pce.pceservice;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.net.Device;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.topology.PathService;
import org.onosproject.pce.pceservice.api.DomainManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Domain manager implementation.
 */
public class DomainManagerImpl implements DomainManager {
    private static final Logger log = LoggerFactory.getLogger(DomainManagerImpl.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PathService pathService;

    private List<Link> links;
    /**
     * Creates new instance of domain manager.
     */
    public DomainManagerImpl(Path path) {
        checkNotNull(path, "Path cannot be null");
        this.links = path.links();
    }

    @Override
    public Set<Path> getDomainSpecificPaths() {
        Set<Path> paths = new HashSet<>();
        Device currentDevice;
        Device lastDevice = null;
        Device firstDevice = null;
        int prevAsNumber = 0;

        for (Link link : links) {
            currentDevice = deviceService.getDevice(link.src().deviceId());
            String asNumber = currentDevice.annotations().value("asNumber");
            if (asNumber != null) {
                if (firstDevice == null) {
                    firstDevice = currentDevice;
                    prevAsNumber = Integer.valueOf(asNumber);
                }

                if (prevAsNumber == Integer.valueOf(asNumber)) {
                    lastDevice = currentDevice;
                    continue;
                }
                // If AS Number is different then add to set of path
                //TODO: check with satish
                paths.addAll(pathService.getPaths(firstDevice.id(), lastDevice.id()));

                prevAsNumber = Integer.valueOf(asNumber);
                firstDevice = currentDevice;
            }
        }

        if ((firstDevice != null) && (lastDevice != null)) {
            paths.addAll(pathService.getPaths(firstDevice.id(), lastDevice.id()));
        }
        return paths;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DomainManagerImpl) {
            DomainManagerImpl that = (DomainManagerImpl) obj;
            return Objects.equals(links, that.links);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(links);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("links", links)
                .toString();
    }
}
