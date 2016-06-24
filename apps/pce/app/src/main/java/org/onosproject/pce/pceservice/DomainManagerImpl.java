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

import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.topology.PathService;
import org.onosproject.pce.pceservice.api.DomainManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Domain manager implementation.
 */
public class DomainManagerImpl implements DomainManager {
    private static final Logger log = LoggerFactory.getLogger(DomainManagerImpl.class);

    protected DeviceService deviceService;

    protected PathService pathService;
    /**
     * Creates new instance of domain manager.
     */
    public DomainManagerImpl(DeviceService deviceService, PathService pathService) {
        this.deviceService = deviceService;
        this.pathService = pathService;
    }

    @Override
    public Set<Path> getDomainSpecificPaths(Path path) {
        checkNotNull(path, "Path cannot be null");
        Set<Path> paths = new HashSet<>();
        Device currentDevice;
        Device lastDevice = null;
        Device firstDevice = null;
        int prevAsNumber = 0;

        for (Link link : path.links()) {
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
    public Map<Oper, Set<Path>> compareDomainSpecificPaths(Set<Path> oldPaths, Set<Path> newPaths) {

        Map<Oper, Set<Path>> map = new HashMap<>();
        Set<Path> updatePaths = new HashSet<>();

        for (Path oldpath : oldPaths) {
            for (Path newPath : newPaths) {
                if (getSrcDeviceId(oldpath).equals(getSrcDeviceId(newPath))
                        && getDstDeviceId(oldpath).equals(getDstDeviceId(newPath))) {
                    updatePaths.add(newPath);
                    oldPaths.remove(oldpath);
                    newPaths.remove(newPath);
                }
            }
        }

        map.put(Oper.ADD, newPaths);
        map.put(Oper.UPDATE, updatePaths);
        map.put(Oper.DELETE, oldPaths);

        return map;
    }

    private DeviceId getDstDeviceId(Path path) {
        int size = path.links().size();
        return path.links().get(size - 1).dst().deviceId();
    }

    private DeviceId getSrcDeviceId(Path path) {
        return path.links().get(0).src().deviceId();
    }

    /*@Override
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
    }*/

    /*@Override
    public String toString() {
        return toStringHelper(this)
                .add("links", links)
                .toString();
    }*/
}
