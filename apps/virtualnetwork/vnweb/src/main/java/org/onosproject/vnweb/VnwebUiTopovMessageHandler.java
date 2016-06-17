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
import org.onosproject.net.Annotations;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.ElementId;
import org.onosproject.net.HostId;
import org.onosproject.net.Link;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.link.LinkService;
import org.onosproject.ui.RequestHandler;
import org.onosproject.ui.UiConnection;
import org.onosproject.ui.UiMessageHandler;
import org.onosproject.ui.topo.DeviceHighlight;
import org.onosproject.ui.topo.Highlights;
import org.onosproject.ui.topo.LinkHighlight;
import org.onosproject.ui.topo.Mod;
import org.onosproject.ui.topo.NodeBadge;
import org.onosproject.ui.topo.TopoUtils;
//import org.onosproject.ui.topo.LinkHighlight.Flavor;
import org.onosproject.ui.topo.NodeBadge.Status;
import org.onosproject.ui.topo.TopoJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.onosproject.ui.topo.LinkHighlight.Flavor.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private static final String VNWEB_QUERY_UPDATE = "update";
    private static final String ID = "id";
    private static final String BANDWIDTH = "bw";
    private static final String BANDWIDTHTYPE = "bwtype";
    private static final String COSTTYPE = "ctype";
    private static final String VN_NAME = "vnName";
    public static final String AS_NUMBER = "asNumber";
    private static final Link[] EMPTY_LINK_SET = new Link[0];
    private static final String CUSTOM_RED = "customRed";

    private final Logger log = LoggerFactory.getLogger(getClass());
    // Delay for showHighlights event processing on GUI client side to
    // account for addLink animation.
    private static final int DELAY_MS = 1100;
    private DeviceService deviceService;
    private LinkService linkService;
    private Link[] linkSet = EMPTY_LINK_SET;
    private int linkIndex;

    private List<ElementId> srcList = new ArrayList<ElementId>();
    private List<ElementId> dstList = new ArrayList<ElementId>();

    @Override
    public void init(UiConnection connection, ServiceDirectory directory) {
        super.init(connection, directory);
        deviceService = directory.get(DeviceService.class);
        linkService = directory.get(LinkService.class);
    }

    @Override
    protected Collection<RequestHandler> createRequestHandlers() {
        return ImmutableSet.of(
                new VnIdQuery(),
                new VnIdQueryHandle(),
                new VnIdRemoveHandle(),
                new ClearHandler(),
                new SetSrcHandler(),
                new SetDstHandler(),
                new SetPathHandler(),
                new VnIdUpdateHandle(),
                new VnIdUpdateHandleConstr(),
                new VnDeviceHighlight()
        );
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
            srcList.add(src);
            log.info("count" + srcList.size());
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
            dstList.add(dst);
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
            //TODO: Need to get all the VN ids and send message to the client.
            ObjectNode result = objectNode();
            ArrayNode arrayNode = arrayNode();

            arrayNode.add("123");
            arrayNode.add("456");
            arrayNode.add("789");
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
            String vnId = string(payload, VN_ID);
            log.debug("query received for VN ID", vnId);

            //clear the previous highlights if any
            clearForMode();
            //TODO: Get the all the tunnels based on VN ID and highlight them.
        }
    }

    private final class VnIdRemoveHandle extends RequestHandler {
        public VnIdRemoveHandle() {
            super(VNWEB_REMOVE_MSG_HANDLE);
        }

        @Override
        public void process(long sid, ObjectNode payload) {
            String vnId = string(payload, VN_ID);
            log.debug("remove event received for VN ID", vnId);

            //TODO: remove the VN based all tunnels and highlight rest of them.
        }
    }

    private final class VnIdUpdateHandle extends RequestHandler {
        public VnIdUpdateHandle() {
            super(VNWEB_UPDATE_MSG_HANDLE);
        }

        @Override
        public void process(long sid, ObjectNode payload) {
            String vnId = string(payload, VN_ID);
            log.debug("update event received for VN ID", vnId);
            //TODO:send the information about tunnel[src, dst and list of constrainsts.]
            //TODO: update the VN based all tunnels and highlight rest of them.

            ObjectNode result = objectNode();
            ArrayNode arrayNode = arrayNode();

            arrayNode.add("VnName");
            arrayNode.add("MaheshNetwork");
            arrayNode.add("BandWidth");
            arrayNode.add("200");
            arrayNode.add("CostType");
            arrayNode.add("TE");
            arrayNode.add("SRC");
            arrayNode.add("RT1");
            arrayNode.add("RT2");
            arrayNode.add("RT3");
            arrayNode.add("DST");
            arrayNode.add("RT1");
            arrayNode.add("RT2");
            arrayNode.add("RT3");

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
            String vnId = string(payload, VN_ID);
            log.debug("update event received for VN ID", vnId);

            String bandWidth = string(payload, BANDWIDTH);
            String bandWidthType = string(payload, BANDWIDTHTYPE);
            String costType = string(payload, COSTTYPE);

            //TODO: update the VN based on constrainsts received.[SRC, DST, BW and cost type]
            //TODO: clear the srcList and dstList after update the path.

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

            //TODO: pass the src, dst list and these constrainsts for setup path.
            //TODO: clear the srcList and dstList after update the path.
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
                    highlights = addDeviceBadge(highlights, dev.id().toString(), asNumber);
                    Set<Link> links = linkService.getDeviceLinks(dev.id());
                    for (Link link : links) {
                        lh = new LinkHighlight(TopoUtils.compactLinkString(link), PRIMARY_HIGHLIGHT)
                        .addMod(new Mod(CUSTOM_RED));

                        log.info("modes:", lh.flavor().toString());
                        log.info("modes:", +lh.mods().size());
                        log.info("mode name:", lh.mods().iterator().next().toString());
                        log.info("CSS class name:", lh.cssClasses());
                        //assertEquals("wrong css", "primary custom", lh.cssClasses());
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
     * @param h highlights
     * @param elemId device to be add badge
     * @param type device type
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

    private void addDeviceBadge(Highlights h, DeviceId devId, int n) {
        DeviceHighlight dh = new DeviceHighlight(devId.toString());
        dh.setBadge(createBadge(n));
        h.add(dh);
    }

    private NodeBadge createBadge(int n) {
        Status status = n > 3 ? Status.ERROR : Status.WARN;
        String noun = n > 3 ? "(critical)" : "(problematic)";
        String msg = "Egress links: " + n + " " + noun;
        return NodeBadge.number(status, n, msg);
    }

    private Highlights fromLinks(Set<Link> links, DeviceId devId) {
        VnwebLinkMap linkMap = new VnwebLinkMap();
        if (links != null) {
            log.debug("Processing {} links", links.size());
            links.forEach(linkMap::add);
        } else {
            log.debug("No egress links found for device {}", devId);
        }

        Highlights highlights = new Highlights();

        for (VnwebLink dlink : linkMap.biLinks()) {
            dlink.makeImportant().setLabel("Yo!");
            highlights.add(dlink.highlight(null));
        }
        return highlights;
    }

    private void initLinkSet() {
        Set<Link> links = new HashSet<>();
        for (Link link : linkService.getActiveLinks()) {
            links.add(link);
        }
        linkSet = links.toArray(new Link[links.size()]);
        linkIndex = 0;
        log.debug("initialized link set to {}", linkSet.length);
    }

    private void sendLinkData() {
        VnwebLinkMap linkMap = new VnwebLinkMap();
        for (Link link : linkSet) {
            linkMap.add(link);
        }
        VnwebLink dl = linkMap.add(linkSet[linkIndex]);
        dl.makeImportant().setLabel(Integer.toString(linkIndex));
        log.debug("sending link data (index {})", linkIndex);

        linkIndex += 1;
        if (linkIndex >= linkSet.length) {
            linkIndex = 0;
        }

        Highlights highlights = new Highlights();
        for (VnwebLink dlink : linkMap.biLinks()) {
            highlights.add(dlink.highlight(null));
        }

        sendHighlights(highlights);
    }
}
