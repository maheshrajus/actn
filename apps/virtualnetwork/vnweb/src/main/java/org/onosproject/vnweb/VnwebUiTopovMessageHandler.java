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
package org.onosproject.vnweb;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import org.onlab.osgi.ServiceDirectory;
import org.onlab.util.Bandwidth;
import org.onosproject.incubator.net.tunnel.Tunnel;
import org.onosproject.net.Annotations;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.ElementId;
import org.onosproject.net.HostId;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.link.LinkService;
import org.onosproject.pce.pceservice.constraint.CostConstraint;
import org.onosproject.pce.pceservice.constraint.PceBandwidthConstraint;
import org.onosproject.ui.RequestHandler;
import org.onosproject.ui.UiConnection;
import org.onosproject.ui.UiMessageHandler;
import org.onosproject.ui.topo.DeviceHighlight;
import org.onosproject.ui.topo.Highlights;
import org.onosproject.ui.topo.LinkHighlight;
import org.onosproject.ui.topo.Mod;
import org.onosproject.ui.topo.NodeBadge;
import org.onosproject.ui.topo.TopoJson;
import org.onosproject.ui.topo.TopoUtils;
import org.onosproject.vn.api.VnEndPoints;
import org.onosproject.vn.store.VirtualNetworkInfo;
import org.onosproject.vn.vnservice.api.VnService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.onosproject.ui.topo.LinkHighlight.Flavor.PRIMARY_HIGHLIGHT;

/**
 * Skeletal ONOS UI Topology-Overlay message handler.
 */
public class VnwebUiTopovMessageHandler extends UiMessageHandler {

    private static final String VNWEB_QUERY_MSG = "vnQuerymsg";
    private static final String VNWEB_QUERY_MSG_HANDLE = "vnQuerymsgHandle";
    private static final String VNWEB_CLEAR = "vnClear";
    private static final String VNWEB_VNID_DISPLAY_MSG = "showVnInfoMsg";
    private static final String VNWEB_VNID_REMOVE_MSG = "showVnInfoMsgRem";
    private static final String VNWEB_VNID_UPDATE_MSG = "showVnInfoMsgUpdate";
    private static final String VNWEB_REMOVE_MSG_HANDLE = "vnRemovemsgHandle";
    private static final String VNWEB_UPDATE_MSG_HANDLE = "vnUpdatemsgHandle";
    private static final String VNWEB_SET_SOURCE = "pceTopovSetSrc";
    private static final String VNWEB_SET_DESTINATION = "pceTopovSetDst";
    private static final String VNWEB_SETUP_PATH = "vnSetup";
    private static final String VNWEB_UPDATE_MSG_HANDLE_REPLY = "showVnInfoMsgUpdateCnstrs";
    private static final String VNWEB_UPDATE_MSG_HANDLE_CONSTRAINTS = "vnUpdatemsgHandleConstr";
    private static final String VNWEB_DEVICE_HIGHLIGHT = "vnDeviceHighlight";

    private static final String VN_ID = "vnid";
    private static final String VN_ID_QUERY = "query";
    private static final String BUFFER_ARRAY = "a";
    private static final String VNWEB_QUERY_SHOW = "show";
    private static final String VNWEB_QUERY_REMOVE = "remove";
    private static final String ID = "id";
    private static final String BANDWIDTH = "bw";
    private static final String BANDWIDTHTYPE = "bwtype";
    private static final String COSTTYPE = "ctype";
    private static final String VN_NAME = "vnName";
    public static final String AS_NUMBER = "asNumber";
    private static final String CUSTOM_RED = "customRed";
    private static final String COST_TYPE_IGP = "igp";
    private static final String COST_TYPE_TE = "te";
    private static final String BANDWIDTH_TYPE_KBPS = "kbps";
    private static final String STRING_NULL = "null";
    private static final double BANDWIDTH_KBPS = 1_000;
    private static final double BANDWIDTH_MBPS = 1_000_000;
    private static final String DST = "DST";
    private static final String SRC = "SRC";
    private static String[] linkColor = {"pCol1", "pCol2", "pCol3", "pCol4",
            "pCol5", "pCol6", "pCol7", "pCol8", "pCol9", "pCol10", "pCol11",
            "pCol12", "pCol13", "pCol14", "pCol15"};
    private static final int LINK_COLOR_MAX = 15;
    private static final int TYPE_BW = 2;
    private static final int TYPE_COST = 1;
    private static final String FILL_BW = "bwtype";
    private static final String FILL_COST = "CostType";
    private static final String FILL_VN = "VnName";
    private static final String FILL_SRC = "SRC";
    private static final String FILL_DST = "SRC";

    private final Logger log = LoggerFactory.getLogger(getClass());
    // Delay for showHighlights event processing on GUI client side to
    // account for addLink animation.
    private DeviceService deviceService;
    private LinkService linkService;
    private VnService vnService;

    private List<DeviceId> srcList = new ArrayList<DeviceId>();
    private List<DeviceId> dstList = new ArrayList<DeviceId>();
    private List<Path> paths = new LinkedList<>();

    @Override
    public void init(UiConnection connection, ServiceDirectory directory) {
        super.init(connection, directory);
        deviceService = directory.get(DeviceService.class);
        linkService = directory.get(LinkService.class);
        vnService = directory.get(VnService.class);
    }

    @Override
    protected Collection<RequestHandler> createRequestHandlers() {
        return ImmutableSet.of(new VnIdQuery(), new VnIdQueryHandle(),
                new VnIdRemoveHandle(), new ClearHandler(),
                new SetSrcHandler(), new SetDstHandler(), new SetPathHandler(),
                new VnIdUpdateHandle(), new VnIdUpdateHandleConstr(),
                new VnDeviceHighlight());
    }

    // === Handler classes: Begin
    /**
     * Handles the 'set source' event received from the client.
     */
    private final class SetSrcHandler extends RequestHandler {

        public SetSrcHandler() {
            super(VNWEB_SET_SOURCE);
        }

        @Override
        public void process(long sid, ObjectNode payload) {
            String id = string(payload, ID);
            ElementId src = elementId(id);
            srcList.add((DeviceId) src);
        }
    }

    /**
     * Handles the 'set destination' event received from the client.
     */
    private final class SetDstHandler extends RequestHandler {

        public SetDstHandler() {
            super(VNWEB_SET_DESTINATION);
        }

        @Override
        public void process(long sid, ObjectNode payload) {
            String id = string(payload, ID);
            ElementId dst = elementId(id);
            dstList.add((DeviceId) dst);
        }
    }

    private final class VnIdQuery extends RequestHandler {
        public VnIdQuery() {
            super(VNWEB_QUERY_MSG);
        }

        @Override
        public void process(long sid, ObjectNode payload) {
            log.debug("query received for VN IDs");
            String type = string(payload, VN_ID_QUERY);
            ObjectNode result = objectNode();
            ArrayNode arrayNode = arrayNode();
            List<VirtualNetworkInfo> listVn = vnService.queryAllVn();

            for (VirtualNetworkInfo vn : listVn) {
                arrayNode.add(vn.vnName().toString());
            }

            result.putArray(BUFFER_ARRAY).addAll(arrayNode);

            if (type.equals(VNWEB_QUERY_SHOW)) {
                sendMessage(VNWEB_VNID_DISPLAY_MSG, sid, result);
            } else if (type.equals(VNWEB_QUERY_REMOVE)) {
                sendMessage(VNWEB_VNID_REMOVE_MSG, sid, result);
            } else {
                sendMessage(VNWEB_VNID_UPDATE_MSG, sid, result);
            }
        }
    }

    private final class VnIdQueryHandle extends RequestHandler {
        public VnIdQueryHandle() {
            super(VNWEB_QUERY_MSG_HANDLE);
        }

        @Override
        public void process(long sid, ObjectNode payload) {
            String vnName = string(payload, VN_ID);
            log.debug("query received for VN ID", vnName);
            Iterable<Tunnel> tunnels = vnService.queryVnTunnels(vnName);
            clearForMode();
            findTunnelAndHighlights(tunnels);
        }
    }

    private final class VnIdRemoveHandle extends RequestHandler {
        public VnIdRemoveHandle() {
            super(VNWEB_REMOVE_MSG_HANDLE);
        }

        @Override
        public void process(long sid, ObjectNode payload) {
            String vnName = string(payload, VN_ID);
            log.debug("remove event received for VN ID", vnName);
            if (!vnService.deleteVn(vnName)) {
                log.debug("Virtual network creation failed.");
            }
        }
    }

    private final class VnIdUpdateHandle extends RequestHandler {
        public VnIdUpdateHandle() {
            super(VNWEB_UPDATE_MSG_HANDLE);
        }

        @Override
        public void process(long sid, ObjectNode payload) {
            String vnName = string(payload, VN_ID);
            log.debug("update event received for VN ID", vnName);
            ObjectNode result = objectNode();
            ArrayNode arrayNode = arrayNode();

            // filling the VN name
            VirtualNetworkInfo vnInfo = vnService.queryVn(vnName);
            arrayNode.add(FILL_VN);
            arrayNode.add(vnInfo.vnName().toString());

            // filling the bandwidth and cost related constraints.
            List<Constraint> listConstraints = vnInfo.constraints();
            for (Constraint constrn : listConstraints) {
                if (constrn instanceof PceBandwidthConstraint) {
                    arrayNode.add(FILL_BW);
                    arrayNode.add("200"); // TODO:
                }
                if (constrn instanceof CostConstraint) {
                    arrayNode.add(FILL_COST);
                    arrayNode.add("1"); // TODO:
                }
            }
            // filling the SRC device IDs
            arrayNode.add(FILL_SRC);
            for (DeviceId device : vnInfo.endPoint().src()) {
                arrayNode.add(device.toString());
            }
            // filling the SRC device IDs
            arrayNode.add(FILL_DST);
            for (DeviceId device : vnInfo.endPoint().dst()) {
                arrayNode.add(device.toString());
            }

            result.putArray(BUFFER_ARRAY).addAll(arrayNode);
            sendMessage(VNWEB_UPDATE_MSG_HANDLE_REPLY, sid, result);
        }
    }

    private final class VnIdUpdateHandleConstr extends RequestHandler {
        public VnIdUpdateHandleConstr() {
            super(VNWEB_UPDATE_MSG_HANDLE_CONSTRAINTS);
        }

        @Override
        public void process(long sid, ObjectNode payload) {
            String vnName = string(payload, VN_ID);
            log.debug("update event received for VN ID", vnName);

            String bandWidth = string(payload, BANDWIDTH);
            String bandWidthType = string(payload, BANDWIDTHTYPE);
            String costType = string(payload, COSTTYPE);
            List<Constraint> constraints;

            constraints = buildCostAndBandWidthConstraints(bandWidth,
                    bandWidthType, costType);
            VnEndPoints endPoint = new VnEndPoints(srcList, dstList);
            if (!vnService.updateVn(vnName, endPoint)) {
                log.error("Virtual network creation failed.");
            }
            if (!vnService.updateVn(vnName, constraints)) {
                log.error("Virtual network creation failed.");
            }
            // clear the src and dst list after setup.
            dstList.removeAll(dstList);
            srcList.removeAll(srcList);

        }
    }

    private final class ClearHandler extends RequestHandler {
        public ClearHandler() {
            super(VNWEB_CLEAR);
        }

        @Override
        public void process(long sid, ObjectNode payload) {
            log.debug("Stop Display");
            dstList.removeAll(dstList);
            srcList.removeAll(srcList);
            clearForMode();
        }
    }

    /**
     * Handles the 'path calculation' event received from the client.
     */
    private final class SetPathHandler extends RequestHandler {

        public SetPathHandler() {
            super(VNWEB_SETUP_PATH);
        }

        @Override
        public void process(long sid, ObjectNode payload) {
            String bandWidth = string(payload, BANDWIDTH);
            String bandWidthType = string(payload, BANDWIDTHTYPE);
            String costType = string(payload, COSTTYPE);
            String vnName = string(payload, VN_NAME);

            if (vnName == null) {
                log.error("VN Name is NULL");
            }
            setupVnHandle(bandWidth, bandWidthType, costType, vnName);
        }
    }

    /**
     * Handles the 'path calculation' event received from the client.
     */
    private final class VnDeviceHighlight extends RequestHandler {

        public VnDeviceHighlight() {
            super(VNWEB_DEVICE_HIGHLIGHT);
        }

        @Override
        public void process(long sid, ObjectNode payload) {
            Iterable<Device> devices = deviceService.getAvailableDevices();
            Highlights highlights = new Highlights();
            LinkHighlight lh;
            for (Device dev : devices) {
                if (dev.type() == Device.Type.ROUTER) {
                    Annotations annots = dev.annotations();
                    String asNumber = annots.value(AS_NUMBER);
                    highlights = addDeviceBadge(highlights,
                            dev.id().toString(), asNumber);
                    Set<Link> links = linkService.getDeviceLinks(dev.id());
                    for (Link link : links) {
                        lh = new LinkHighlight(
                                TopoUtils.compactLinkString(link),
                                PRIMARY_HIGHLIGHT).addMod(new Mod(CUSTOM_RED));

                        highlights.add(lh);
                    }
                }
            }

            sendMessage(TopoJson.highlightsMessage(highlights));
        }
    }

    // === Handler classes:End
    /**
     * provides the element id.
     */
    private ElementId elementId(String id) {
        try {
            return DeviceId.deviceId(id);
        } catch (IllegalArgumentException e) {
            return HostId.hostId(id);
        }
    }

    /**
     * Handles the badge add and highlights.
     *
     * @param h
     *            highlights
     * @param devId
     *            device to be add badge
     * @param asNum
     *            device type
     * @return highlights
     */
    private Highlights addDeviceBadge(Highlights h, String devId, String asNum) {
        DeviceHighlight dh = new DeviceHighlight(devId);
        dh.setBadge(NodeBadge.text(asNum));
        h.add(dh);
        return h;
    }

    private void clearForMode() {
        sendHighlights(new Highlights());
    }

    private void sendHighlights(Highlights highlights) {
        sendMessage(TopoJson.highlightsMessage(highlights));
    }

    private void setupVnHandle(String bandWidth, String bandWidthType,
            String costType, String vnName) {
        List<Constraint> constraints;
        VnEndPoints endPoint = new VnEndPoints(srcList, dstList);

        if (bandWidth == null && costType == null) {
            if (!vnService.setupVn(vnName, endPoint, null)) {
                log.error("Virtual network creation failed.");
            }
            return;
        }

        constraints = buildCostAndBandWidthConstraints(bandWidth,
                bandWidthType, costType);
        if (!vnService.setupVn(vnName, endPoint, constraints)) {
            log.error("Virtual network creation failed.");
        }
        // clear the src and dst list after setup.
        dstList.removeAll(dstList);
        srcList.removeAll(srcList);

    }

    private List<Constraint> buildCostAndBandWidthConstraints(
            String bandWidth, String bandWidthType, String costType) {
        List<Constraint> constraints = new LinkedList<>();

        // bandwidth
        double bwValue = 0.0;
        if (!bandWidth.equals(STRING_NULL)) {
            bwValue = Double.parseDouble(bandWidth);
        }
        if (bandWidthType.equals(BANDWIDTH_TYPE_KBPS)) {
            bwValue = bwValue * BANDWIDTH_KBPS;
        } else {
            bwValue = bwValue * BANDWIDTH_MBPS;
        }

        // Cost type
        CostConstraint.Type costTypeVal = null;
        switch (costType) {
        case COST_TYPE_IGP:
            costTypeVal = CostConstraint.Type.COST;
            break;
        case COST_TYPE_TE:
            costTypeVal = CostConstraint.Type.TE_COST;
            break;
        default:
            break;
        }

        if (bwValue != 0.0) {
            PceBandwidthConstraint vnBandWidth = new PceBandwidthConstraint(Bandwidth.bps(bwValue));
            constraints.add(vnBandWidth);
        }

        if (costTypeVal != null) {
            constraints.add(CostConstraint.of(costTypeVal));
        }

        return constraints;
    }

    /**
     * Handles the event of topology listeners.
     */
    private void findTunnelAndHighlights(Iterable<Tunnel> tunnelSet) {
        Highlights highlights = new Highlights();
        paths.removeAll(paths);

        for (Tunnel tunnel : tunnelSet) {
            if (tunnel.path() == null) {
                log.error("path does not exist");
                sendMessage(TopoJson.highlightsMessage(highlights));
                return;
            }
            Link firstLink = tunnel.path().links().get(0);
            if (firstLink != null) {
                if (firstLink.src() != null) {
                    highlights = addBadge(highlights, firstLink.src()
                            .deviceId().toString(), SRC);
                }
            }
            Link lastLink = tunnel.path().links()
                    .get(tunnel.path().links().size() - 1);
            if (lastLink != null) {
                if (lastLink.dst() != null) {
                    highlights = addBadge(highlights, lastLink.dst().deviceId()
                            .toString(), DST);
                }
            }
            paths.add(tunnel.path());
        }

        hilightAndSendPaths(highlights);
    }

    /**
     * Handles the highlights of selected path.
     */
    private void hilightAndSendPaths(Highlights highlights) {
        LinkHighlight lh;
        int linkclr = 0;
        for (Path path : paths) {
            for (Link link : path.links()) {
                lh = new LinkHighlight(TopoUtils.compactLinkString(link),
                        PRIMARY_HIGHLIGHT).addMod(new Mod(linkColor[linkclr]));
                highlights.add(lh);
            }
            linkclr = linkclr + 1;
            if (linkclr == LINK_COLOR_MAX) {
                linkclr = 0;
            }
        }

        sendMessage(TopoJson.highlightsMessage(highlights));
    }

    /**
     * Handles the addition of badge and highlights.
     *
     * @param highlights
     *            highlights
     * @param elemId
     *            device to be add badge
     * @param src
     *            device to be add badge
     * @return
     */
    private Highlights addBadge(Highlights highlights, String elemId, String src) {
        highlights = deviceBadge(highlights, elemId, src);
        return highlights;
    }

    /**
     * Handles the badge add and highlights.
     *
     * @param h
     *            highlights
     * @param elemId
     *            device to be add badge
     * @param type
     *            device badge value
     * @return highlights
     */
    private Highlights deviceBadge(Highlights h, String elemId, String type) {
        DeviceHighlight dh = new DeviceHighlight(elemId);
        dh.setBadge(createBadge(type));
        h.add(dh);
        return h;
    }

    /**
     * Handles the node badge add and highlights.
     *
     * @param type
     *            device badge value
     * @return badge of given node
     */
    private NodeBadge createBadge(String type) {
        return NodeBadge.text(type);
    }
}
