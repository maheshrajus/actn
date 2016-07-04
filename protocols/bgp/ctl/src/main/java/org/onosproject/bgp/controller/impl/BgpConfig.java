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
package org.onosproject.bgp.controller.impl;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.onlab.packet.Ip4Address;
import org.onlab.packet.IpAddress;
import org.onosproject.bgp.controller.BgpCfg;
import org.onosproject.bgp.controller.BgpConnectPeer;
import org.onosproject.bgp.controller.BgpController;
import org.onosproject.bgp.controller.BgpId;
import org.onosproject.bgp.controller.BgpLinkCfg;
import org.onosproject.bgp.controller.BgpLinkListener;
import org.onosproject.bgp.controller.BgpPeer;
import org.onosproject.bgp.controller.BgpPeerCfg;
import org.onosproject.bgp.controller.impl.BgpControllerImpl.BgpPeerManagerImpl;
import org.onosproject.bgpio.exceptions.BgpParseException;
import org.onosproject.bgpio.protocol.linkstate.BgpLinkLSIdentifier;
import org.onosproject.bgpio.protocol.linkstate.BgpLinkLsNlriVer4;
import org.onosproject.bgpio.protocol.linkstate.BgpNodeLSNlriVer4;
import org.onosproject.bgpio.protocol.linkstate.NodeDescriptors;
import org.onosproject.bgpio.protocol.linkstate.PathAttrNlriDetails;
import org.onosproject.bgpio.types.AreaIDTlv;
import org.onosproject.bgpio.types.AutonomousSystemTlv;
import org.onosproject.bgpio.types.BgpLSIdentifierTlv;
import org.onosproject.bgpio.types.BgpValueType;
import org.onosproject.bgpio.types.IPv4AddressTlv;
import org.onosproject.bgpio.types.IsIsNonPseudonode;
import org.onosproject.bgpio.types.OspfNonPseudonode;
import org.onosproject.bgpio.types.attr.BgpLinkAttrMaxLinkBandwidth;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
/**
 * Provides BGP configuration of this BGP speaker.
 */
public class BgpConfig implements BgpCfg {

    protected static final Logger log = LoggerFactory.getLogger(BgpConfig.class);

    private static final short DEFAULT_HOLD_TIMER = 120;
    private static final short DEFAULT_CONN_RETRY_TIME = 120;
    private static final short DEFAULT_CONN_RETRY_COUNT = 5;

    private State state = State.INIT;
    private int localAs;
    private int maxSession;
    private boolean lsCapability;
    private short holdTime;
    private boolean largeAs = false;
    private int maxConnRetryTime;
    private int maxConnRetryCount;
    private FlowSpec flowSpec = FlowSpec.NONE;
    private Ip4Address routerId = null;
    private TreeMap<String, BgpPeerCfg> bgpPeerTree = new TreeMap<>();
    private TreeMap<String, BgpLinkCfg> bgpLinks = new TreeMap<>();
    private BgpConnectPeer connectPeer;
    private BgpPeerManagerImpl peerManager;
    private BgpController bgpController;
    private boolean rpdCapability;
    private DeviceService deviceService;

    /*
     * Constructor to initialize the values.
     */
    public BgpConfig(BgpController bgpController, DeviceService deviceService) {
        this.bgpController = bgpController;
        this.deviceService = deviceService;
        this.peerManager = (BgpPeerManagerImpl) bgpController.peerManager();
        this.holdTime = DEFAULT_HOLD_TIMER;
        this.maxConnRetryTime = DEFAULT_CONN_RETRY_TIME;
        this.maxConnRetryCount = DEFAULT_CONN_RETRY_COUNT;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public int getAsNumber() {
        return this.localAs;
    }

    @Override
    public void setAsNumber(int localAs) {

        State localState = getState();
        this.localAs = localAs;

        /* Set configuration state */
        if (localState == State.IP_CONFIGURED) {
            setState(State.IP_AS_CONFIGURED);
        } else {
            setState(State.AS_CONFIGURED);
        }
    }

    @Override
    public int getMaxSession() {
        return this.maxSession;
    }

    @Override
    public void setMaxSession(int maxSession) {
        this.maxSession = maxSession;
    }

    @Override
    public boolean getLsCapability() {
        return this.lsCapability;
    }

    @Override
    public void setLsCapability(boolean lsCapability) {
        this.lsCapability = lsCapability;
    }

    @Override
    public FlowSpec flowSpecCapability() {
        return this.flowSpec;
    }

    @Override
    public void setFlowSpecCapability(FlowSpec flowSpec) {
        this.flowSpec = flowSpec;
    }

    @Override
    public boolean flowSpecRpdCapability() {
        return this.rpdCapability;
    }

    @Override
    public void setFlowSpecRpdCapability(boolean rpdCapability) {
        this.rpdCapability = rpdCapability;
    }

    @Override
    public String getRouterId() {
        if (this.routerId != null) {
            return this.routerId.toString();
        } else {
            return null;
        }
    }

    @Override
    public void setRouterId(String routerId) {
        State localState = getState();
        this.routerId = Ip4Address.valueOf(routerId);

        /* Set configuration state */
        if (localState == State.AS_CONFIGURED) {
            setState(State.IP_AS_CONFIGURED);
        } else {
            setState(State.IP_CONFIGURED);
        }
    }

    @Override
    public boolean addPeer(String routerid, int remoteAs) {
        return addPeer(routerid, remoteAs, DEFAULT_HOLD_TIMER, false);
    }

    @Override
    public boolean addPeer(String routerid, short holdTime) {
        return addPeer(routerid, this.getAsNumber(), holdTime, false);
    }

    @Override
    public boolean addPeer(String routerid, int remoteAs, short holdTime, boolean exportRoute) {
        BgpPeerConfig lspeer = new BgpPeerConfig();
        if (this.bgpPeerTree.get(routerid) == null) {

            lspeer.setPeerRouterId(routerid);
            lspeer.setAsNumber(remoteAs);
            lspeer.setHoldtime(holdTime);
            lspeer.setExportRoute(exportRoute);
            lspeer.setState(BgpPeerCfg.State.IDLE);
            lspeer.setSelfInnitConnection(false);

            if (this.getAsNumber() == remoteAs) {
                lspeer.setIsIBgp(true);
            } else {
                lspeer.setIsIBgp(false);
            }

            this.bgpPeerTree.put(routerid, lspeer);
            log.debug("added successfully");
            return true;
        } else {
            log.debug("already exists");
            return false;
        }
    }

    @Override
    public boolean connectPeer(String routerid) {
        BgpPeerCfg lspeer = this.bgpPeerTree.get(routerid);

        if (lspeer != null) {
            lspeer.setSelfInnitConnection(true);

            if (lspeer.connectPeer() == null) {
                connectPeer = new BgpConnectPeerImpl(bgpController, routerid, Controller.BGP_PORT_NUM);
                lspeer.setConnectPeer(connectPeer);
                connectPeer.connectPeer();
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean removePeer(String routerid) {
        BgpPeerCfg lspeer = this.bgpPeerTree.get(routerid);

        if (lspeer != null) {

            disconnectPeer(routerid);
            lspeer.setSelfInnitConnection(false);
            lspeer = this.bgpPeerTree.remove(routerid);
            log.debug("Deleted : " + routerid + " successfully");

            return true;
        } else {
            log.debug("Did not find : " + routerid);
            return false;
        }
    }

    @Override
    public boolean disconnectPeer(String routerid) {
        BgpPeerCfg lspeer = this.bgpPeerTree.get(routerid);

        if (lspeer != null) {

            BgpPeer disconnPeer = peerManager.getPeer(BgpId.bgpId(IpAddress.valueOf(routerid)));
            if (disconnPeer != null) {
                // TODO: send notification peer deconfigured
                disconnPeer.disconnectPeer();
            } else if (lspeer.connectPeer() != null) {
                lspeer.connectPeer().disconnectPeer();
            }
            lspeer.setState(BgpPeerCfg.State.IDLE);
            lspeer.setSelfInnitConnection(false);
            log.debug("Disconnected : " + routerid + " successfully");

            return true;
        } else {
            log.debug("Did not find : " + routerid);
            return false;
        }
    }

    @Override
    public void setPeerConnState(String routerid, BgpPeerCfg.State state) {
        BgpPeerCfg lspeer = this.bgpPeerTree.get(routerid);

        if (lspeer != null) {
            lspeer.setState(state);
            log.debug("Peer : " + routerid + " is not available");

            return;
        } else {
            log.debug("Did not find : " + routerid);
            return;
        }
    }

    @Override
    public BgpPeerCfg.State getPeerConnState(String routerid) {
        BgpPeerCfg lspeer = this.bgpPeerTree.get(routerid);

        if (lspeer != null) {
            return lspeer.getState();
        } else {
            return BgpPeerCfg.State.INVALID; //No instance
        }
    }

    @Override
    public boolean isPeerConnectable(String routerid) {
        BgpPeerCfg lspeer = this.bgpPeerTree.get(routerid);

        if ((lspeer != null) && lspeer.getState().equals(BgpPeerCfg.State.IDLE)) {
            return true;
        }

        return false;
    }

    @Override
    public TreeMap<String, BgpPeerCfg> getPeerTree() {
        return this.bgpPeerTree;
    }

    @Override
    public TreeMap<String, BgpPeerCfg> displayPeers() {
        if (this.bgpPeerTree.isEmpty()) {
            log.debug("There are no BGP peers");
        } else {
            Set<Entry<String, BgpPeerCfg>> set = this.bgpPeerTree.entrySet();
            Iterator<Entry<String, BgpPeerCfg>> list = set.iterator();
            BgpPeerCfg lspeer;

            while (list.hasNext()) {
                Entry<String, BgpPeerCfg> me = list.next();
                lspeer = me.getValue();
                log.debug("Peer neighbor IP :" + me.getKey());
                log.debug(", AS Number : " + lspeer.getAsNumber());
                log.debug(", Hold Timer : " + lspeer.getHoldtime());
                log.debug(", Is iBGP : " + lspeer.getIsIBgp());
            }
        }
        return null;
    }

    @Override
    public BgpPeerCfg displayPeers(String routerid) {

        if (this.bgpPeerTree.isEmpty()) {
            log.debug("There are no Bgp peers");
        } else {
            return this.bgpPeerTree.get(routerid);
        }
        return null;
    }

    @Override
    public void setHoldTime(short holdTime) {
        this.holdTime = holdTime;
    }

    @Override
    public short getHoldTime() {
        return this.holdTime;
    }

    @Override
    public boolean getLargeASCapability() {
        return this.largeAs;
    }

    @Override
    public void setLargeASCapability(boolean largeAs) {
        this.largeAs = largeAs;
    }

    @Override
    public boolean isPeerConfigured(String routerid) {
        BgpPeerCfg lspeer = this.bgpPeerTree.get(routerid);
        return (lspeer != null) ? true : false;
    }

    @Override
    public boolean isPeerConnected(String routerid) {
        // TODO: is peer connected
        return true;
    }

    @Override
    public int getMaxConnRetryCount() {
        return this.maxConnRetryCount;
    }

    @Override
    public void setMaxConnRetryCout(int retryCount) {
        this.maxConnRetryCount = retryCount;
    }

    @Override
    public int getMaxConnRetryTime() {
        return this.maxConnRetryTime;
    }

    @Override
    public void setMaxConnRetryTime(int retryTime) {
        this.maxConnRetryTime = retryTime;
    }

    public List<BgpValueType> getNodeDescriptor(Device device) {
        List<BgpValueType> subTlvsLocal = new LinkedList<>();
        if (device.annotations().value("asNumber") != null) {
            AutonomousSystemTlv asNum = new AutonomousSystemTlv(Integer.valueOf(device.annotations()
                                                                      .value("asNumber")));
            subTlvsLocal.add(asNum);
        }
        if (device.annotations().value("domainIdentifier") != null) {
            BgpLSIdentifierTlv identifier = new BgpLSIdentifierTlv(Integer.valueOf(device.annotations()
                                                                          .value("domainIdentifier")));
            subTlvsLocal.add(identifier);
        }
        if (device.annotations().value("areaIdentifier") != null) {
            AreaIDTlv areaId = new AreaIDTlv(Integer.valueOf(device.annotations().value("areaIdentifier")));
            subTlvsLocal.add(areaId);
        }

        if (device.annotations().value("protocol") != null) {
            int protocolId = Integer.valueOf(device.annotations().value("protocol"));

            if (device.annotations().value(AnnotationKeys.ROUTER_ID) != null) {
                if (protocolId == NodeDescriptors.IS_IS_LEVEL_1_PROTOCOL_ID || protocolId == NodeDescriptors
                        .IS_IS_LEVEL_2_PROTOCOL_ID) {
                    subTlvsLocal.add(new IsIsNonPseudonode(device.annotations()
                                                                 .value(AnnotationKeys.ROUTER_ID).getBytes()));
                } else if (protocolId == NodeDescriptors.OSPF_V2_PROTOCOL_ID || protocolId ==
                        NodeDescriptors.OSPF_V3_PROTOCOL_ID) {
                    subTlvsLocal.add(new OspfNonPseudonode(Integer.valueOf(device.annotations()
                                                                  .value(AnnotationKeys.ROUTER_ID))));
                }
            }
        }

        return subTlvsLocal;
    }

    public BgpLinkLsNlriVer4 getNlri(Device srcDevice, IpAddress srcInterface, Integer srcPort,
                                      Device dstDevice, IpAddress dstInterface, Integer dstPort) {





        List<BgpValueType> subTlvsLocal = getNodeDescriptor(srcDevice);
        NodeDescriptors localNodeDescriptors = new NodeDescriptors(subTlvsLocal,
                                                                   (short) subTlvsLocal.size(),
                                                                   NodeDescriptors.LOCAL_NODE_DES_TYPE);

        List<BgpValueType> subTlvsRemote = getNodeDescriptor(srcDevice);
        NodeDescriptors remoteNodeDescriptors = new NodeDescriptors(subTlvsRemote,
                                                                    (short) subTlvsRemote.size(),
                                                                    NodeDescriptors.REMOTE_NODE_DES_TYPE);

        LinkedList<BgpValueType> linkDescriptor = new LinkedList<>();
        linkDescriptor.add(IPv4AddressTlv.of(Ip4Address.valueOf(srcInterface.toString()), (short) 1));
        linkDescriptor.add(IPv4AddressTlv.of(Ip4Address.valueOf(dstInterface.toString()), (short) 1));

        BgpLinkLSIdentifier linkLSIdentifier = new BgpLinkLSIdentifier(localNodeDescriptors,
                                                                       remoteNodeDescriptors, linkDescriptor);

        BgpLinkLsNlriVer4 nlri = new BgpLinkLsNlriVer4(Byte.valueOf(srcDevice.annotations().value("protocol")),
                                                       (Integer.valueOf(srcDevice.annotations()
                                                       .value("domainIdentifier"))), linkLSIdentifier, null, false);
        return nlri;
    }

    @Override
    public void addLink(DeviceId srcDeviceId, IpAddress srcInterface, Integer srcPort, DeviceId dstDeviceId, IpAddress
            dstInterface, Integer dstPort, Double maxReservedBandwidth) {

        Device srcDevice = deviceService.getDevice(srcDeviceId);
        Device dstDevice = deviceService.getDevice(dstDeviceId);

        if ((srcDevice == null) || (dstDevice == null)) {
            return;
        }

        if (this.getLinks().containsKey(srcDeviceId.toString())) {
            this.bgpLinks.replace(srcDeviceId.toString(), new BgpLinkConfig(srcDeviceId, srcInterface, srcPort,
                                  dstDeviceId, dstInterface, dstPort, maxReservedBandwidth));
        } else {
            this.bgpLinks.put(srcDeviceId.toString(), new BgpLinkConfig(srcDeviceId, srcInterface, srcPort,
                                                                        dstDeviceId,
                                                            dstInterface,
                                                dstPort, maxReservedBandwidth));
        }
        PathAttrNlriDetails details = new PathAttrNlriDetails();

        List<BgpValueType> pathAttr = new LinkedList<>();
        pathAttr.add(BgpLinkAttrMaxLinkBandwidth.of(maxReservedBandwidth.floatValue(), (short) 1));
        details.setPathAttribute(pathAttr);
        details.setIdentifier(Integer.valueOf(srcDevice.annotations().value("domainIdentifier")));
        details.setProtocolID(BgpNodeLSNlriVer4.ProtocolType.valueOf(srcDevice.annotations().value("protocol")));

        for (BgpLinkListener l : bgpController.linkListener()) {
            try {
                l.addLink((BgpLinkLsNlriVer4) getNlri(srcDevice, srcInterface, srcPort, dstDevice, dstInterface,
                                                      dstPort), details);
            } catch (BgpParseException e) {
                e.printStackTrace();
            }
        }

        return;
    }

    @Override
    public void deleteLink(DeviceId srcDeviceId, IpAddress srcInterface, Integer srcPort, DeviceId dstDeviceId,
                           IpAddress dstInterface, Integer dstPort) {

        Device srcDevice = deviceService.getDevice(srcDeviceId);
        Device dstDevice = deviceService.getDevice(dstDeviceId);

        if ((srcDevice == null) || (dstDevice == null)) {
            return;
        }

        this.bgpLinks.remove(srcDeviceId.toString());

        for (BgpLinkListener l : bgpController.linkListener()) {
            try {
                l.deleteLink((BgpLinkLsNlriVer4) getNlri(srcDevice, srcInterface, srcPort, dstDevice, dstInterface,
                                                         dstPort));
            } catch (BgpParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public BgpLinkCfg link(DeviceId srcDeviceId) {
        return this.getLinks().get(srcDeviceId.toString());
    }

    @Override
    public TreeMap<String, BgpLinkCfg> getLinks() {
        return this.bgpLinks;
    }
}
