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
package org.onosproject.vn.store;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.util.KryoNamespace;
import org.onosproject.net.intent.Constraint;
import org.onosproject.pce.pceservice.constraint.CostConstraint;
import org.onosproject.pce.pceservice.constraint.PceBandwidthConstraint;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.ConsistentMap;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageService;
import org.onosproject.vn.api.PathEndPoint;
import org.onosproject.vn.api.VnEndPoints;
import org.onosproject.vn.store.api.VnStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manages the pool of available labels to devices, links and tunnels.
 */
@Component(immediate = true)
@Service
public class DistributedVnStore implements VnStore {

    private static final String VN_NAME_NULL = "VN name cannot be null";
    private static final String ENDPOINT_NULL = "End point cannot be null";
    private static final String CONSTRAINT_NULL = "Constraint cannot be null";
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;

    private ConsistentMap<String, VirtualNetworkInfo> vnDataMap;

    @Activate
    protected void activate() {
        vnDataMap = storageService.<String, VirtualNetworkInfo>consistentMapBuilder()
                .withName("onos-vn-vnDataMap")
                .withSerializer(Serializer.using(
                        new KryoNamespace.Builder()
                                .register(KryoNamespaces.API)
                                .register(VirtualNetworkInfo.class,
                                          CostConstraint.class,
                                          CostConstraint.Type.class,
                                          PceBandwidthConstraint.class,
                                          VnEndPoints.class,
                                          PathEndPoint.class)
                                .build()))
                .build();

        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        log.info("Stopped");
    }

    @Override
    public boolean add(String vnName, VnEndPoints endPoint, List<Constraint> constraints) {
        checkNotNull(vnName, VN_NAME_NULL);
        checkNotNull(constraints, CONSTRAINT_NULL);
        checkNotNull(endPoint, ENDPOINT_NULL);

        VirtualNetworkInfo vnData = new VirtualNetworkInfo(vnName, constraints, endPoint);
        vnDataMap.put(vnName, vnData);

        return true;
    }


    @Override
    public boolean update(String vnName, List<Constraint> constraint) {
        checkNotNull(vnName, VN_NAME_NULL);
        checkNotNull(constraint, CONSTRAINT_NULL);

        if (!vnDataMap.containsKey((vnName))) {
            log.debug("Virtual network does not exist whose name is {}.", vnName);
            return false;
        }

        VirtualNetworkInfo vnData = vnDataMap.get(vnName).value();
        vnData.setConstraint(constraint);
        vnDataMap.put(vnName, vnData);

        return true;
    }

    @Override
    public boolean update(String vnName, VnEndPoints endPoint) {
        checkNotNull(vnName, VN_NAME_NULL);
        checkNotNull(endPoint, ENDPOINT_NULL);

        if (!vnDataMap.containsKey((vnName))) {
            log.debug("Virtual network does not exist whose name is {}.", vnName);
            return false;
        }

        VirtualNetworkInfo vnData = vnDataMap.get(vnName).value();
        vnData.setEndPoint(endPoint);
        vnDataMap.put(vnName, vnData);

        return true;
    }

    @Override
    public boolean delete(String vnName) {
        checkNotNull(vnName, VN_NAME_NULL);

        if (!vnDataMap.containsKey((vnName))) {
            log.debug("Virtual network does not exist whose name is {}.", vnName);
            return false;
        }

        vnDataMap.remove(vnName);

        return true;
    }

    @Override
    public VirtualNetworkInfo query(String vnName) {
        checkNotNull(vnName, VN_NAME_NULL);

        if (!vnDataMap.containsKey((vnName))) {
            log.debug("Virtual network does not exist whose name is {}.", vnName);
            return null;
        }
        return vnDataMap.get(vnName).value();
    }

    @Override
    public Map<String, VirtualNetworkInfo> queryAll() {
        return vnDataMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (VirtualNetworkInfo) e.getValue().value()));
    }
}
