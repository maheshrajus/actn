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

import org.onosproject.net.DefaultPath;
import org.onosproject.net.Device;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.device.DeviceService;
import org.onosproject.pce.pceservice.api.DomainManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Domain manager implementation.
 */
public class DomainManagerImpl implements DomainManager {
    private static final Logger log = LoggerFactory.getLogger(DomainManagerImpl.class);

    protected DeviceService deviceService;

    /**
     * Creates new instance of domain manager.
     */
    public DomainManagerImpl(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public Set<Path> getDomainSpecificPaths(Path path) {
        checkNotNull(path, "Path cannot be null");
        Set<Path> paths = new HashSet<>();
        List<Link> links = new LinkedList<>();
        Device srcDevice;
        Device dstDevice;

        for (Link link : path.links()) {
            srcDevice = deviceService.getDevice(link.src().deviceId());
            dstDevice = deviceService.getDevice(link.dst().deviceId());

            if (srcDevice.annotations().value("asNumber").equals(dstDevice.annotations().value("asNumber"))) {
                links.add(link);
            } else {
                paths.add(new DefaultPath(null, links, 0.0));
                links = new LinkedList<>();
            }
        }
        if (!links.isEmpty()) {
            paths.add(new DefaultPath(null, links, 0.0));
        }
        return paths;
    }

    @Override
    public Map<Oper, Set<Path>> compareDomainSpecificPaths(Set<Path> oldPaths, Set<Path> newPaths) {

        Map<Oper, Set<Path>> pathMap = new HashMap<>();
        Set<Path> updatePaths = new HashSet<>();
        Set<Path> oldPathsTemp= new HashSet<>(oldPaths);
        Set<Path> newPathsTemp = new HashSet<>(newPaths);

        for (Path oldpath : oldPaths) {
            for (Path newPath : newPaths) {
                if (oldpath.src().deviceId().equals(newPath.src().deviceId())
                        && oldpath.dst().deviceId().equals(newPath.dst().deviceId())) {
                    updatePaths.add(newPath);
                    oldPathsTemp.remove(oldpath);
                    newPathsTemp.remove(newPath);
                }
            }
        }

        pathMap.put(Oper.ADD, newPathsTemp);
        pathMap.put(Oper.UPDATE, updatePaths);
        pathMap.put(Oper.DELETE, oldPathsTemp);

        return pathMap;
    }
}
