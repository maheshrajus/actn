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
package org.onosproject.vn.manager;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.util.Bandwidth;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.core.IdGenerator;
import org.onosproject.incubator.net.tunnel.TunnelId;
import org.onosproject.net.intent.Constraint;
import org.onosproject.pce.pceservice.api.PceService;
import org.onosproject.pce.pceservice.constraint.CostConstraint;
import org.onosproject.pce.pceservice.constraint.SharedBandwidthConstraint;
import org.onosproject.vn.manager.api.VnService;
import org.onosproject.vn.manager.constraint.VnBandwidth;
import org.onosproject.vn.manager.constraint.VnConstraint;
import org.onosproject.vn.manager.constraint.VnCost;
import org.onosproject.vn.store.EndPoint;
import org.onosproject.vn.store.Lsp;
import org.onosproject.vn.store.VirtualNetworkInfo;
import org.onosproject.vn.store.api.VnStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of virtual network service.
 */
@Component(immediate = true)
@Service
public class VnManager implements VnService {
    private static final Logger log = LoggerFactory.getLogger(VnManager.class);

    private static final String VN_TUNNEL_ID_GEN_TOPIC = "vn-tunnel-id";
    public static final String VN_SERVICE_APP = "org.onosproject.vn";
    private ApplicationId appId;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected VnStore vnStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PceService service;

    private IdGenerator tunnelIdIdGen;
    /**
     * Creates new instance of vnManager.
     */
    public VnManager() {
    }

    @Activate
    protected void activate() {
        appId = coreService.registerApplication(VN_SERVICE_APP);
        tunnelIdIdGen = coreService.getIdGenerator(VN_TUNNEL_ID_GEN_TOPIC);
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        log.info("Stopped");
    }

    @Override
    public boolean setupVn(String vnName, List<VnConstraint> constraints, EndPoint endPoint) {
        //TODO:
        if (!vnStore.setupVn(vnName, constraints, endPoint)) {
            return false;
        }

        VirtualNetworkInfo virtualNetwork = vnStore.queryVn(vnName);
        for (Lsp lsp : virtualNetwork.lsp()) {
            String tunnelName = vnName.toString().concat(Long.toString(tunnelIdIdGen.getNewId()));
            //service.setupPath(lsp.src(), lsp.dst(), tunnelName, getConstraints(constraints), null);
        }
        return true;
    }

    @Override
    public boolean setupVn(String vnName, EndPoint endPoint) {
        if (!vnStore.setupVn(vnName, endPoint)) {
            return false;
        }
        VirtualNetworkInfo virtualNetwork = vnStore.queryVn(vnName);
        for (Lsp lsp : virtualNetwork.lsp()) {
            String tunnelName = vnName.toString().concat(Long.toString(tunnelIdIdGen.getNewId()));
            service.setupPath(lsp.src(), lsp.dst(), tunnelName, null, null);
        }
        return true;
    }

    @Override
    public boolean updateVn(String vnName, EndPoint endPoint) {
        if (!vnStore.updateVn(vnName, endPoint)) {
            return false;
        }
        VirtualNetworkInfo virtualNetwork = vnStore.queryVn(vnName);
        for (Lsp lsp : virtualNetwork.lsp()) {
            String tunnelName = vnName.toString().concat(Long.toString(tunnelIdIdGen.getNewId()));

            //TODO: currently no interface for updatePath in pceService
            service.setupPath(lsp.src(), lsp.dst(), tunnelName, null, null);
        }
        return true;
    }

    private List<Constraint> getConstraints(List<VnConstraint> constraint) {
        List<Constraint> pceConstraint = new LinkedList<>();
        for (VnConstraint c : constraint) {
            if (c.getType() == VnBandwidth.TYPE) {
                VnBandwidth vnBandwidth = (VnBandwidth) c;
                SharedBandwidthConstraint bandWidth = SharedBandwidthConstraint.of(null, Bandwidth.bps(100),
                                                                                   vnBandwidth.bandWidthValue());
                pceConstraint.add(bandWidth);
            } else if (c.getType() == VnCost.TYPE) {
                VnCost vnCost = (VnCost) c;
                int cost = vnCost.type().type();
                CostConstraint.Type type = CostConstraint.Type.COST;
                if (cost == 1) {
                    type = CostConstraint.Type.COST;
                } else if (cost == 2) {
                    type = CostConstraint.Type.TE_COST;
                }
                CostConstraint costConstraint = new CostConstraint(type);
                pceConstraint.add(costConstraint);
            }
        }
        return pceConstraint;
    }

    @Override
    public boolean updateVn(String vnName, List<VnConstraint> constraint) {
       if (!vnStore.updateVn(vnName, constraint)) {
           return false;
       }

       VirtualNetworkInfo virtualNetwork = vnStore.queryVn(vnName);
       for (Lsp lsp : virtualNetwork.lsp()) {
           String tunnelName = vnName.toString().concat(Long.toString(tunnelIdIdGen.getNewId()));
           // TODO:
           service.updatePath(TunnelId.valueOf(tunnelName), getConstraints(constraint));
       }
       return true;
    }

    @Override
    public boolean deleteVn(String vnName) {
        VirtualNetworkInfo virtualNetwork = vnStore.queryVn(vnName);
        if (virtualNetwork == null) {
            return true;
        }
        for (Lsp lsp : virtualNetwork.lsp()) {
            String tunnelName = vnName.toString().concat(Long.toString(tunnelIdIdGen.getNewId()));
            // TODO:
            service.releasePath(TunnelId.valueOf(tunnelName));
        }
        if (!vnStore.deleteVn(vnName)) {
            return false;
        }
        return true;
    }

    @Override
    public VirtualNetworkInfo queryVn(String vnName) {
        return vnStore.queryVn(vnName);
    }

    @Override
    public List<VirtualNetworkInfo> queryAllVn() {
        Map<String, VirtualNetworkInfo> vnMap = vnStore.queryAllVn();
        List<VirtualNetworkInfo> vn = new LinkedList<>();
        vn.addAll(vnMap.keySet().stream().map(vnName -> vnStore.queryVn(vnName)).collect(Collectors.toList()));
        return vn;
    }
}
