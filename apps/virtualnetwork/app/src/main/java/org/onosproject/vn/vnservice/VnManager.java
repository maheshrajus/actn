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
package org.onosproject.vn.vnservice;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.incubator.net.tunnel.Tunnel;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Path;
import org.onosproject.net.intent.Constraint;
import org.onosproject.pce.pceservice.LspType;
import org.onosproject.pce.pceservice.api.PceService;
import org.onosproject.vn.api.PathEndPoint;
import org.onosproject.vn.api.VnEndPoints;
import org.onosproject.vn.store.VirtualNetworkInfo;
import org.onosproject.vn.store.api.VnStore;
import org.onosproject.vn.vnservice.api.VnService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of virtual network service.
 */
@Component(immediate = true)
@Service
public class VnManager implements VnService {
    private static final Logger log = LoggerFactory.getLogger(VnManager.class);

    private static final String VN_TUNNEL_ID_GEN_TOPIC = "vn-tunnel-id";
    private ApplicationId appId;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected VnStore vnStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PceService service;
    private List<PathEndPoint> pathEndPoint;
    /**
     * Creates new instance of vnManager.
     */
    public VnManager() {
    }

    @Activate
    protected void activate() {
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        log.info("Stopped");
    }

    @Override
    public boolean setupVn(String vnName, VnEndPoints endPoint, List<Constraint> constraints) {
        //TODO:
        if (!vnStore.add(vnName, endPoint, constraints)) {
            return false;
        }

        List<PathEndPoint> endPoints = pathEndPoint(endPoint);
        for (PathEndPoint ep : endPoints) {
            String tunnelName = vnName.concat(Long.toString(service.generatePathId()));
            service.setupPath(ep.src(), ep.dst(), tunnelName, constraints, LspType.WITH_SIGNALLING, vnName);
        }
        return true;
    }

    private List<PathEndPoint> pathEndPoint(VnEndPoints endPoint) {
        List<DeviceId> src = endPoint.src();
        List<DeviceId> dst = endPoint.dst();
        this.pathEndPoint = new LinkedList<>();

        for (DeviceId source : src) {
            pathEndPoint.addAll(dst.stream().filter(destination -> !source.equals(destination))
                               .map(destination -> new PathEndPoint(source, destination)).collect(Collectors.toList()));
        }
        return pathEndPoint;
    }

    @Override
    public boolean updateVn(String vnName, VnEndPoints endPoint) {
        checkNotNull(endPoint, "End point cannot be null");
        VirtualNetworkInfo virtualNetwork = vnStore.query(vnName);
        if (virtualNetwork == null) {
            return false;
        }
        List<PathEndPoint> newPathEndPoints = pathEndPoint(endPoint);

        // Check if any tunnel not provided in update, if not delete existing
        Iterable<Tunnel> tunnels = service.queryPath(vnName);
        List<PathEndPoint> oldPathEndPoints = pathEndPoint(virtualNetwork.endPoint());
        for (PathEndPoint ep : oldPathEndPoints) {
            if (!newPathEndPoints.contains(ep)) {
                // not present in update request, so delete existing
                for (Tunnel t : tunnels) {
                    if (t.path().src().deviceId().equals(ep.src()) && t.path().dst().deviceId().equals(ep.dst())) {
                             service.releasePath(t.tunnelId());
                    }
                }
                oldPathEndPoints.remove(ep);
            }
        }

        for (PathEndPoint ep : newPathEndPoints) {
            if (!oldPathEndPoints.contains(ep)) {
              // new entry, setup path
                String tunnelName = vnName.concat(Long.toString(service.generatePathId()));
                service.setupPath(ep.src(), ep.dst(), tunnelName,
                        virtualNetwork.constraints(), LspType.WITH_SIGNALLING, vnName);
            }
        }

        if (!vnStore.update(vnName, endPoint)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean updateVn(String vnName, List<Constraint> constraint) {
       if (!vnStore.update(vnName, constraint)) {
           return false;
       }

       Iterable<Tunnel> tunnels = service.queryPath(vnName);
       for (Tunnel t : tunnels) {
           service.updatePath(t.tunnelId(), constraint);
       }
       return true;
    }

    @Override
    public boolean deleteVn(String vnName) {
        VirtualNetworkInfo virtualNetwork = vnStore.query(vnName);
        if (virtualNetwork == null) {
            return true;
        }
        Iterable<Tunnel> tunnels = service.queryPath(vnName);
        for (Tunnel t : tunnels) {
            service.releasePath(t.tunnelId());
        }

        if (!vnStore.delete(vnName)) {
            return false;
        }
        return true;
    }

    @Override
    public VirtualNetworkInfo queryVn(String vnName) {
        return vnStore.query(vnName);
    }

    @Override
    public List<VirtualNetworkInfo> queryAllVn() {
        Map<String, VirtualNetworkInfo> vnMap = vnStore.queryAll();
        List<VirtualNetworkInfo> vn = new LinkedList<>();
        vn.addAll(vnMap.keySet().stream().map(vnName -> vnStore.query(vnName)).collect(Collectors.toList()));
        return vn;
    }

    @Override
    public Iterable<Tunnel> queryAllVnTunnels() {
        Map<String, VirtualNetworkInfo> vnMap = vnStore.queryAll();
        List<Tunnel> allTunnels = new LinkedList<>();
        Iterable<Tunnel> tunnels;
        for (Map.Entry<String, VirtualNetworkInfo> entry : vnMap.entrySet()) {
            tunnels = service.queryPath(entry.getValue().vnName());
            if (tunnels != null) {
               for (Tunnel t : tunnels) {
                   allTunnels.add(t);
               }
            }
        }
        return allTunnels;
    }
    @Override
    public Iterable<Tunnel> queryVnTunnels(String vnName) {
        VirtualNetworkInfo vnInfo = vnStore.query(vnName);
        if (vnInfo != null) {
            return service.queryPath(vnName);
        }
        return null;
    }

    @Override
    public boolean computeOptimalPath(String vnName, List<Constraint> constraints,
                                      DeviceId src, List<DeviceId> dstPoints) {
        double leastCost = 0.0;
        boolean bcomputeFirst = true;
        DeviceId leastCostDst = null;

        for (DeviceId dst : dstPoints) {

            Set<Path> computedPathSet = service.computePath(src, dst, constraints);

            // NO-PATH
            if (computedPathSet.isEmpty()) {
                log.info("Path not found for given destination: " + dst);
                continue;
            }

            Path computedPath = computedPathSet.iterator().next();

            log.info("Path found for the destination: " + dst + ", cost: " + computedPath.cost()
                             + ", No of links: " + computedPath.links().size());

            if (bcomputeFirst) {
                leastCostDst = dst;
                leastCost = computedPath.cost();
                bcomputeFirst = false;
            } else if (computedPath.cost() < leastCost) {
                leastCostDst = dst;
                leastCost = computedPath.cost();
            }
        }

        if (leastCostDst == null) {
            log.error("No optimal path fund for given source to destinations!!");
            return false;
        }

        log.info("Optimal path destination: " + leastCostDst);

        String tunnelName = vnName.concat(Long.toString(service.generatePathId()));
        PceService.PathErr pathErr = service.setupPath(src, leastCostDst,
                                                       tunnelName, constraints, LspType.WITH_SIGNALLING, vnName);
        if (PceService.PathErr.SUCCESS != pathErr) {
            log.error("SetupPath failed!! pathErr: " + pathErr);
            return false;
        }

        List<DeviceId> srcId = new LinkedList<>();
        List<DeviceId> dstId = new LinkedList<>();
        srcId.add(src);
        dstId.add(leastCostDst);
        VnEndPoints endPoint = new VnEndPoints(srcId, dstId);
        if (!vnStore.add(vnName, endPoint, constraints)) {
            log.error("Create vnName failed!! : " + vnName);
            return false;
        }

        return true;
    }
}
