/*
 * Copyright 2015-present Open Networking Laboratory
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
package org.onosproject.pcc.pccmgr.ctl;

import com.google.common.collect.Sets;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.IpAddress;
import org.onlab.util.DataRateUnit;
import org.onosproject.incubator.net.tunnel.IpTunnelEndPoint;
import org.onosproject.incubator.net.tunnel.Tunnel;
import org.onosproject.incubator.net.tunnel.TunnelEndPoint;
import org.onosproject.net.intent.Constraint;
import org.onosproject.pcc.pccmgr.api.PceId;
import org.onosproject.pcc.pccmgr.api.PcepAgent;
import org.onosproject.pcc.pccmgr.api.PcepClient;
import org.onosproject.pcc.pccmgr.api.PcepClientController;
import org.onosproject.pcc.pccmgr.api.PcepClientDriver;
import org.onosproject.pcc.pccmgr.api.PcepClientListener;
import org.onosproject.pcc.pccmgr.api.PcepEventListener;
import org.onosproject.pcc.pccmgr.api.PcepNodeListener;
import org.onosproject.pcc.pccmgr.api.PcepPacketListener;
import org.onosproject.pce.pceservice.LspType;
import org.onosproject.pce.pceservice.api.PcePathUpdateListener;
import org.onosproject.pce.pceservice.api.PceService;
import org.onosproject.pce.pceservice.constraint.CostConstraint;
import org.onosproject.pcep.api.PcepSrpStore;
import org.onosproject.pcep.api.SrpIdMapping;
import org.onosproject.pce.pceservice.constraint.PceBandwidthConstraint;
import org.onosproject.pcep.pcepio.exceptions.PcepParseException;
import org.onosproject.pcep.pcepio.protocol.PcInitiatedLspRequest;
import org.onosproject.pcep.pcepio.protocol.PcepAssociationObject;
import org.onosproject.pcep.pcepio.protocol.PcepAttribute;
import org.onosproject.pcep.pcepio.protocol.PcepBandwidthObject;
import org.onosproject.pcep.pcepio.protocol.PcepEndPointsObject;
import org.onosproject.pcep.pcepio.protocol.PcepError;
import org.onosproject.pcep.pcepio.protocol.PcepErrorInfo;
import org.onosproject.pcep.pcepio.protocol.PcepErrorMsg;
import org.onosproject.pcep.pcepio.protocol.PcepErrorObject;
import org.onosproject.pcep.pcepio.protocol.PcepFactory;
import org.onosproject.pcep.pcepio.protocol.PcepInitiateMsg;
import org.onosproject.pcep.pcepio.protocol.PcepLspObject;
import org.onosproject.pcep.pcepio.protocol.PcepMessage;
import org.onosproject.pcep.pcepio.protocol.PcepMetricObject;
import org.onosproject.pcep.pcepio.protocol.PcepSrpObject;
import org.onosproject.pcep.pcepio.protocol.PcepUpdateMsg;
import org.onosproject.pcep.pcepio.protocol.PcepUpdateRequest;
import org.onosproject.pcep.pcepio.types.PathSetupTypeTlv;
import org.onosproject.pcep.pcepio.types.PcepValueType;
import org.onosproject.pcep.pcepio.types.StatefulIPv4LspIdentifiersTlv;
import org.onosproject.pcep.pcepio.types.SymbolicPathNameTlv;
import org.onosproject.pcep.pcepio.types.VirtualNetworkTlv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.onosproject.pcc.pccmgr.api.PcepLspSyncAction.*;
import static org.onosproject.pce.pceservice.api.PceService.PathErr;
import static org.onosproject.pce.pceservice.constraint.CostConstraint.Type.TE_COST;
import static org.onosproject.pce.pceservice.constraint.CostConstraint.Type.COST;
import static org.onosproject.pcep.pcepio.protocol.ver1.PcepMetricObjectVer1.IGP_METRIC;
import static org.onosproject.pcep.pcepio.protocol.ver1.PcepMetricObjectVer1.TE_METRIC;
import static org.onosproject.pce.pceservice.LspType.WITH_SIGNALLING;
import static org.onosproject.pcep.pcepio.types.PcepErrorDetailInfo.ERROR_TYPE_24;
import static org.onosproject.pcep.pcepio.types.PcepErrorDetailInfo.ERROR_TYPE_19;
import static org.onosproject.pcep.pcepio.types.PcepErrorDetailInfo.ERROR_TYPE_10;
import static org.onosproject.pcep.pcepio.types.PcepErrorDetailInfo.ERROR_TYPE_6;
import static org.onosproject.pcep.pcepio.types.PcepErrorDetailInfo.ERROR_VALUE_8;
import static org.onosproject.pcep.pcepio.types.PcepErrorDetailInfo.ERROR_VALUE_3;
import static org.onosproject.pcep.pcepio.types.PcepErrorDetailInfo.ERROR_VALUE_5;
import static org.onosproject.pcep.pcepio.types.PcepErrorDetailInfo.ERROR_VALUE_2;
import static org.onosproject.pcep.pcepio.types.PcepErrorDetailInfo.ERROR_VALUE_11;

/**
 * Implementation of PCEP client controller.
 */
@Component(immediate = true)
@Service
public class PcepClientControllerImpl implements PcepClientController {

    private static final Logger log = LoggerFactory.getLogger(PcepClientControllerImpl.class);



    protected ConcurrentHashMap<PceId, PcepClient> connectedClients =
            new ConcurrentHashMap<>();

    protected PcepClientAgent agent = new PcepClientAgent();
    protected Set<PcepClientListener> pcepClientListener = new HashSet<>();

    protected Set<PcepEventListener> pcepEventListener = Sets.newHashSet();
    protected Set<PcepNodeListener> pcepNodeListener = Sets.newHashSet();
    protected Set<PcepPacketListener> pcepPacketListener = Sets.newHashSet();

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PceService pceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PcepSrpStore pceSrpStore;

    private PcePathUpdateListener pcePathListener = new InnerPcePathUpdateListener();

    private final Controller ctrl = new Controller();

    public static final String BANDWIDTH = "bandwidth";
    public static final String LSP_SIG_TYPE = "lspSigType";
    public static final String PCC_TUNNEL_ID = "PccTunnelId";
    public static final String PLSP_ID = "PLspId";
    public static final String LOCAL_LSP_ID = "localLspId";
    public static final String PCE_INIT = "pceInit";
    public static final String COST_TYPE = "costType";
    public static final String DELEGATE = "delegation";


    @Activate
    public void activate() {
        ctrl.start(agent);
        //TODO : currently pceservice is set at PcepConfig singleton, will be removed later
        PcepConfig.getInstance().setPceService(pceService);

        pceService.addListener(pcePathListener);

        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        // Close all connected clients
        closeConnectedClients();
        pceService.removeListener(pcePathListener);
        ctrl.stop();
        log.info("Stopped");
    }

    @Override
    public Collection<PcepClient> getClients() {
        return connectedClients.values();
    }

    @Override
    public PcepClient getClient(PceId pceId) {
        return connectedClients.get(pceId);
    }

    @Override
    public void addListener(PcepClientListener listener) {
        if (!pcepClientListener.contains(listener)) {
            this.pcepClientListener.add(listener);
        }
    }


    @Override
    public void removeListener(PcepClientListener listener) {
        this.pcepClientListener.remove(listener);
    }

    @Override
    public void addEventListener(PcepEventListener listener) {
        pcepEventListener.add(listener);
    }

    @Override
    public void removeEventListener(PcepEventListener listener) {
        pcepEventListener.remove(listener);
    }

    @Override
    public void addPacketListener(PcepPacketListener listener) {
        pcepPacketListener.add(listener);
    }

    @Override
    public void removePacketListener(PcepPacketListener listener) {
        pcepPacketListener.remove(listener);
    }

    @Override
    public void writeMessage(PceId pceId, PcepMessage msg) {
        this.getClient(pceId).sendMessage(msg);
    }

    @Override
    public void addNodeListener(PcepNodeListener listener) {
        pcepNodeListener.add(listener);
    }

    @Override
    public void removeNodeListener(PcepNodeListener listener) {
        pcepNodeListener.remove(listener);
    }


    public void processInitiateMsg(PceId pceId, PcepMessage msg) {
        VirtualNetworkTlv virtualNetworklv = null;
        PcepEndPointsObject endPointObj = null;
        SymbolicPathNameTlv pathNameTlv = null;
        List<Constraint> initConstrntList = null;
        LspType lspType = null;
        PathErr pathErr = null;
        PcepClient pc = getClient(pceId);
        ListIterator<PcInitiatedLspRequest> listIterator
                = ((PcepInitiateMsg) msg).getPcInitiatedLspRequestList().listIterator();
        while (listIterator.hasNext()) {
            PcInitiatedLspRequest initLsp = listIterator.next();
            PcepSrpObject srpObj = initLsp.getSrpObject();
            if (0 != srpObj.getSrpID()) {

                ListIterator<PcepValueType> listSrpTlvIterator = srpObj.getOptionalTlv().listIterator();
                PathSetupTypeTlv pathSetupTlv = null;
                lspType = WITH_SIGNALLING;

                while (listSrpTlvIterator.hasNext()) {
                    PcepValueType tlv = listSrpTlvIterator.next();
                    switch (tlv.getType()) {
                        case PathSetupTypeTlv.TYPE:
                            pathSetupTlv = (PathSetupTypeTlv) tlv;
                            lspType = LspType.values()[pathSetupTlv.getPst()];
                            break;

                        default:
                            break;
                    }
                }

                PcepLspObject lspObj = initLsp.getLspObject();
                ListIterator<PcepValueType> listTlvIterator = lspObj.getOptionalTlv().listIterator();
                pathNameTlv = null;
                StatefulIPv4LspIdentifiersTlv lspIdentifierTlv = null;

                while (listTlvIterator.hasNext()) {
                    PcepValueType tlv = listTlvIterator.next();
                    switch (tlv.getType()) {
                        case SymbolicPathNameTlv.TYPE:
                            pathNameTlv = (SymbolicPathNameTlv) tlv;
                            break;
                        case StatefulIPv4LspIdentifiersTlv.TYPE:
                            lspIdentifierTlv = (StatefulIPv4LspIdentifiersTlv) tlv;
                            break;
                        default:
                            break;
                    }
                }

                if (pathNameTlv != null) {
                    // PcepSrpIdMap.INSTANCE.add(pathNameTlv.getValue(), srpObj.getSrpID());
                    String pathName = new String(pathNameTlv.getValue());
                    SrpIdMapping srpIdMapping = new SrpIdMapping(srpObj.getSrpID(), 0, 0);
                    pceSrpStore.addSrpIdMapping(pathName, srpIdMapping);
                    log.info("Adding into SrpID map, symName: " + pathName + ", SrpId: " + srpObj.getSrpID());
                } else {
                    pc.sendMessage(Collections.singletonList(getErrMsg(pc.factory(), ERROR_TYPE_10,
                                                                       ERROR_VALUE_8, srpObj.getSrpID())));
                    continue;
                }

                endPointObj = initLsp.getEndPointsObject();

                if (srpObj.getRFlag()) {
                    log.info("Remove path with PLSPID " + lspObj.getPlspId());
                    assert lspIdentifierTlv != null;
                    pathErr = pceService.releasePath(IpAddress.valueOf(lspIdentifierTlv.getIpv4IngressAddress()),
                            IpAddress.valueOf(lspIdentifierTlv.getIpv4EgressAddress()),
                                    String.valueOf(lspObj.getPlspId()));
                } else {

                    if (initLsp.getAssociationObjectList() != null) {
                        ListIterator<PcepAssociationObject> iterator
                                = initLsp.getAssociationObjectList().listIterator();

                        while (iterator.hasNext()) {
                            PcepAssociationObject associationObj = iterator.next();

                            ListIterator<PcepValueType> listAssTlvIterator
                                    = associationObj.getOptionalTlv().listIterator();
                           virtualNetworklv = null;

                            while (listAssTlvIterator.hasNext()) {
                                PcepValueType tlv = listAssTlvIterator.next();
                                switch (tlv.getType()) {
                                    case VirtualNetworkTlv.TYPE:
                                        virtualNetworklv = (VirtualNetworkTlv) tlv;
                                        break;

                                    default:
                                        break;
                                }
                            }

                            if (virtualNetworklv != null) {

                                initConstrntList = new LinkedList<>();
                                PcepBandwidthObject initBandwidthObject
                                        = initLsp.getPcepAttribute().getBandwidthObject();

                                // Assign bandwidth
                                if ((initBandwidthObject != null) && (initBandwidthObject.getBandwidth() != 0.0)) {
                                    initConstrntList.add(PceBandwidthConstraint.of(
                                            (double) initBandwidthObject.getBandwidth(),
                                            DataRateUnit.valueOf("BPS")));
                                }

                                PcepAttribute initAttributes = initLsp.getPcepAttribute();
                                if (initAttributes != null && initAttributes.getMetricObjectList() != null) {
                                    ListIterator<PcepMetricObject> metricIterator
                                            = initAttributes.getMetricObjectList().listIterator();

                                    while (metricIterator.hasNext()) {
                                        PcepMetricObject initMetricObj = metricIterator.next();
                                        if (initMetricObj.getBType() == IGP_METRIC) {
                                            CostConstraint costConstraint = new CostConstraint(COST);
                                            initConstrntList.add(costConstraint);
                                        } else if (initMetricObj.getBType() == TE_METRIC) {
                                            CostConstraint costConstraint = new CostConstraint(TE_COST);
                                            initConstrntList.add(costConstraint);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    assert virtualNetworklv != null;
                    pathErr = pceService.setupPath(new String(virtualNetworklv.getValue()),
                                                           IpAddress.valueOf(endPointObj.getSourceIpAddress()),
                                                           IpAddress.valueOf(endPointObj.getDestIpAddress()),
                                                           new String(pathNameTlv.getValue()),
                                                           initConstrntList, lspType);
                }

                if (pathErr == PathErr.COMPUTATION_FAIL) {
                    PcepSrpIdMap.remove(pathNameTlv.getValue());
                    pc.sendMessage(Collections.singletonList(getErrMsg(pc.factory(), ERROR_TYPE_24,
                                                                       ERROR_VALUE_3, srpObj.getSrpID())));
                }  else if (pathErr != PathErr.SUCCESS) {
                    log.info("setupPath failed, ErrorValue: " + pathErr);
                    PcepSrpIdMap.remove(pathNameTlv.getValue());
                    pc.sendMessage(Collections.singletonList(getErrMsg(pc.factory(), ERROR_TYPE_24,
                                                                       ERROR_VALUE_2, srpObj.getSrpID())));
                }
            }
        }

        return;
    }

    private void processUpdateMsg(PceId pceId, PcepMessage msg) {

        PathErr pathErr;
        StatefulIPv4LspIdentifiersTlv lspIdentifier = null;
        SymbolicPathNameTlv pathNameTlv = null;
        PcepLspObject lspObj = null;
        List<Constraint> constrntList = null;

        PcepClient pc = getClient(pceId);

        ListIterator<PcepUpdateRequest> listIterator
                = ((PcepUpdateMsg) msg).getUpdateRequestList().listIterator();
        while (listIterator.hasNext()) {
            PcepUpdateRequest updReq = listIterator.next();
            PcepSrpObject srpObj = updReq.getSrpObject();
            if (0 != srpObj.getSrpID()) {
                lspObj = updReq.getLspObject();
                ListIterator<PcepValueType> listTlvIterator = lspObj.getOptionalTlv().listIterator();
                lspIdentifier = null;

                while (listTlvIterator.hasNext()) {
                    PcepValueType tlv = listTlvIterator.next();
                    switch (tlv.getType()) {
                        case SymbolicPathNameTlv.TYPE:
                            pathNameTlv = (SymbolicPathNameTlv) tlv;
                            break;
                        case StatefulIPv4LspIdentifiersTlv.TYPE:
                            lspIdentifier = (StatefulIPv4LspIdentifiersTlv) tlv;
                            break;
                        default:
                            break;
                    }
                }

                if (lspIdentifier == null) {
                    //Attempted LSP Update Request without stateful PCE capability being advertised
                    pc.sendMessage(Collections.singletonList(getErrMsg(pc.factory(), ERROR_TYPE_6,
                                                                       ERROR_VALUE_11, srpObj.getSrpID())));
                    return;
                }

                if (pathNameTlv != null) {
                    //PcepSrpIdMap.INSTANCE.add(pathNameTlv.getValue(), srpObj.getSrpID());
                    String pathName = new String(pathNameTlv.getValue());
                    SrpIdMapping srpIdMapping = new SrpIdMapping(srpObj.getSrpID(), 0, 0);
                    pceSrpStore.addSrpIdMapping(pathName, srpIdMapping);
                    log.info("Adding symbalicName: " + pathName + ", SrpId: " + srpObj.getSrpID());
                } else {
                    TunnelEndPoint src = IpTunnelEndPoint
                            .ipTunnelPoint(IpAddress.valueOf(lspIdentifier.getIpv4EgressAddress()));
                    TunnelEndPoint dst = IpTunnelEndPoint
                            .ipTunnelPoint(IpAddress.valueOf(lspIdentifier.getIpv4EgressAddress()));

                    Collection<Tunnel> existingTunnels = pceService.queryPath(src, dst);
                    String rptPlspId = String.valueOf(lspObj.getPlspId());
                    String pathName = null;

                    for (Tunnel tunnel : existingTunnels) {
                        if (rptPlspId.equals(tunnel.annotations().value(PLSP_ID))) {
                            pathName = tunnel.tunnelName().value();
                            break;
                        }
                    }

                    if (pathName == null) {
                        pc.sendMessage(Collections.singletonList(getErrMsg(pc.factory(), ERROR_TYPE_10, ERROR_VALUE_8,
                                                                           srpObj.getSrpID())));
                        continue;
                    }

                    SrpIdMapping srpIdMapping = new SrpIdMapping(srpObj.getSrpID(), 0, 0);
                    pceSrpStore.addSrpIdMapping(pathName, srpIdMapping);
                    log.info("Adding symName: " + pathName + ", SrpId: " + srpObj.getSrpID());
                }

                constrntList = new LinkedList<>();
                PcepBandwidthObject bandwidthObject
                        = updReq.getMsgPath().getPcepAttribute().getBandwidthObject();

                // Assign bandwidth
                if (bandwidthObject.getBandwidth() != 0.0) {
                    constrntList.add(PceBandwidthConstraint.of((double) bandwidthObject.getBandwidth(),
                                                            DataRateUnit.valueOf("BPS")));
                }

                PcepAttribute attributes = updReq.getMsgPath().getPcepAttribute();
                if (attributes != null && attributes.getMetricObjectList() != null) {
                    ListIterator<PcepMetricObject> iterator = attributes.getMetricObjectList().listIterator();

                    while (iterator.hasNext()) {
                        PcepMetricObject metricObj = iterator.next();
                        if (metricObj.getBType() == IGP_METRIC) {
                            CostConstraint costConstraint = new CostConstraint(COST);
                            constrntList.add(costConstraint);
                        } else if (metricObj.getBType() == TE_METRIC) {
                            CostConstraint costConstraint = new CostConstraint(TE_COST);
                            constrntList.add(costConstraint);
                        }

                    }
                }
            }

            assert lspIdentifier != null;
            pathErr = pceService.updatePath(IpAddress.valueOf(lspIdentifier.getIpv4IngressAddress()),
                                            IpAddress.valueOf(lspIdentifier.getIpv4EgressAddress()),
                                            String.valueOf(lspObj.getPlspId()), constrntList);

            if (pathErr == PathErr.COMPUTATION_FAIL) {
                PcepSrpIdMap.remove(pathNameTlv.getValue());
                pc.sendMessage(Collections.singletonList(getErrMsg(pc.factory(), ERROR_TYPE_24,
                                                                   ERROR_VALUE_3, srpObj.getSrpID())));
            } else if (pathErr != PathErr.SUCCESS) {
                log.info("setupPath failed, ErrorValue: " + pathErr);
                PcepSrpIdMap.remove(pathNameTlv.getValue());
                pc.sendMessage(Collections.singletonList(getErrMsg(pc.factory(), ERROR_TYPE_24,
                                                                   ERROR_VALUE_2, srpObj.getSrpID())));
            }
        }
    }

    @Override
    public void processClientMessage(PceId pceId, PcepMessage msg) {
        PcepClient pc = getClient(pceId);

        switch (msg.getType()) {
        case NONE:
            break;
        case OPEN:
            break;
        case KEEP_ALIVE:
            break;
        case PATH_COMPUTATION_REQUEST:
            break;
        case PATH_COMPUTATION_REPLY:
            break;
        case NOTIFICATION:
            break;
        case ERROR:
            break;
        case INITIATE:
            if (!pc.capability().pcInstantiationCapability()) {
                pc.sendMessage(Collections.singletonList(getErrMsg(pc.factory(), ERROR_TYPE_19,
                                                                   ERROR_VALUE_5)));
            } else {
                processInitiateMsg(pceId, msg);
            }
            break;
        case UPDATE:
            if (!pc.capability().statefulPceCapability()) {

                //Attempted LSP Update Request without stateful PCE capability being advertised
                pc.sendMessage(Collections.singletonList(getErrMsg(pc.factory(), ERROR_TYPE_19,
                        ERROR_VALUE_2)));
            } else {
                processUpdateMsg(pceId, msg);
            }

            break;
        case LABEL_UPDATE:
            if (!pc.capability().pceccCapability()) {
                pc.sendMessage(Collections.singletonList(getErrMsg(pc.factory(), ERROR_TYPE_19,
                        ERROR_VALUE_5)));
            }
            break;
        case CLOSE:
            log.info("Sending Close Message  to {" + pceId.toString() + "}");
            pc.sendMessage(Collections.singletonList(pc.factory().buildCloseMsg().build()));
            //now disconnect client
            pc.disconnectClient();
            break;
        case LABEL_RANGE_RESERV:
            break;
        case LS_REPORT: //TODO: need to handle LS report to add or remove node
            break;
        case MAX:
            break;
        case END:
            break;
        default:
            break;
        }
    }

    @Override
    public void closeConnectedClients() {
        PcepClient pc;
        for (PceId id : connectedClients.keySet()) {
            pc = getClient(id);
            pc.disconnectClient();
        }
    }

    /**
     * Returns pcep error message with specific error type and value.
     *
     * @param factory represents pcep factory
     * @param errorType pcep error type
     * @param errorValue pcep error value
     * @return pcep error message
     */
    public PcepErrorMsg getErrMsg(PcepFactory factory, byte errorType, byte errorValue) {
        LinkedList<PcepError> llPcepErr = new LinkedList<>();

        LinkedList<PcepErrorObject> llerrObj = new LinkedList<>();
        PcepErrorMsg errMsg;

        PcepErrorObject errObj = factory.buildPcepErrorObject().setErrorValue(errorValue).setErrorType(errorType)
                .build();

        llerrObj.add(errObj);
        PcepError pcepErr = factory.buildPcepError().setErrorObjList(llerrObj).build();

        llPcepErr.add(pcepErr);

        PcepErrorInfo errInfo = factory.buildPcepErrorInfo().setPcepErrorList(llPcepErr).build();

        errMsg = factory.buildPcepErrorMsg().setPcepErrorInfo(errInfo).build();
        return errMsg;
    }

    private PcepErrorMsg getErrMsg(PcepFactory factory, byte errorType,
                                   byte errorValue, int srpID) {
        LinkedList<PcepError> llPcepErr = new LinkedList<>();

        LinkedList<PcepErrorObject> llerrObj = new LinkedList<>();
        LinkedList<PcepSrpObject> llsrpObj = new LinkedList<>();
        PcepErrorMsg errMsg;

        PcepSrpObject srpObjTemp = null;
        try {
            srpObjTemp = factory.buildSrpObject()
                    .setRFlag(false)
                    .setSrpID(srpID)
                    .build();
        } catch (PcepParseException e) {
            e.printStackTrace();
        }

        llsrpObj.add(srpObjTemp);

        PcepErrorObject errObj = factory.buildPcepErrorObject().setErrorValue(errorValue).setErrorType(errorType)
                .build();

        llerrObj.add(errObj);
        PcepError pcepErr = factory.buildPcepError().setErrorObjList(llerrObj).setSrpObjList(llsrpObj).build();

        llPcepErr.add(pcepErr);

        PcepErrorInfo errInfo = factory.buildPcepErrorInfo().setPcepErrorList(llPcepErr).build();

        errMsg = factory.buildPcepErrorMsg().setPcepErrorInfo(errInfo).build();
        return errMsg;
    }

    /**
     * Implementation of an Pcep Agent which is responsible for
     * keeping track of connected clients and the state in which
     * they are.
     */
    public class PcepClientAgent implements PcepAgent {

        private final Logger log = LoggerFactory.getLogger(PcepClientAgent.class);

        @Override
        public boolean addConnectedClient(PceId pceId, PcepClient pc) {

            if (connectedClients.get(pceId) != null) {
                log.error("Trying to add connectedClient but found a previous "
                        + "value for pcc ip: {}", pceId.toString());
                return false;
            } else {
                log.debug("Added Client {}", pceId.toString());
                connectedClients.put(pceId, pc);
                for (PcepClientListener l : pcepClientListener) {
                    l.clientConnected(pceId);
                }
                return true;
            }
        }

        @Override
        public PcepClientDriver getConnectedClient(PceId pceId) {
            if (pceId == null) {
                Iterator<ConcurrentHashMap.Entry<PceId, PcepClient>> iterator = connectedClients.entrySet().iterator();
                if (iterator.hasNext()) {
                    return (PcepClientDriver) iterator.next().getValue();
                }
                return null;
            }
            return (PcepClientDriver) connectedClients.get(pceId);
        }

        @Override
        public boolean validActivation(PceId pceId) {
            if (connectedClients.get(pceId) == null) {
                log.error("Trying to activate client but is not in "
                        + "connected client: pccIp {}. Aborting ..", pceId.toString());
                return false;
            }

            return true;
        }

        @Override
        public void removeConnectedClient(PceId pceId) {

            connectedClients.remove(pceId);
            for (PcepClientListener l : pcepClientListener) {
                log.warn("removal for {}", pceId.toString());
                l.clientDisconnected(pceId);
            }
        }

        @Override
        public void processPcepMessage(PceId pceId, PcepMessage m) {
            processClientMessage(pceId, m);
        }

        @Override
        public void addNode(PcepClient pc) {
            for (PcepNodeListener l : pcepNodeListener) {
                l.addNode(pc);
            }
        }

        @Override
        public void deleteNode(PceId pceId) {
            for (PcepNodeListener l : pcepNodeListener) {
                l.deleteNode(pceId);
            }
        }

        /*
         * Go through the tunnels which are known by PCE but were not reported by PCC during LSP DB sync and take
         * appropriate actions.
         */
        private void handleResidualTunnels(Map<Object, Tunnel> preSyncLspDb) {
            for (Tunnel pceExtraTunnel : preSyncLspDb.values()) {
                if (pceExtraTunnel.annotations().value(PCE_INIT) == null
                        || "false".equalsIgnoreCase(pceExtraTunnel.annotations().value(PCE_INIT))) {
                    // PCC initiated tunnels should be removed from tunnel store.
                    for (PcepEventListener l : pcepEventListener) {
                        l.handleEndOfSyncAction(pceExtraTunnel, REMOVE);
                    }
                } else {
                    // PCE initiated tunnels should be initiated again.
                    for (PcepEventListener l : pcepEventListener) {
                        l.handleEndOfSyncAction(pceExtraTunnel, UNSTABLE);
                    }
                }
            }
        }

        @Override
        public PcepErrorMsg prepareErrMsg(PcepClientDriver pc, byte errorType, byte errorValue) {
            return getErrMsg(pc.factory(), errorType, errorValue);
        }
    }
}
