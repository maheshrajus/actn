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
package org.onosproject.vn.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.codec.CodecContext;
import org.onosproject.codec.JsonCodec;
import org.onosproject.vn.manager.DefaultVirtualNetwork;
import org.onosproject.vn.manager.VirtualNetwork;
import org.onosproject.vn.manager.constraint.VnBandwidth;
import org.onosproject.vn.manager.constraint.VnCost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Virtual network json codec.
 */
public final class VnCodec extends JsonCodec<VirtualNetwork> {
    private final Logger log = LoggerFactory.getLogger(VnCodec.class);
    private static final String VN_NAME = "vnName";
    private static final String CONSTRAINT = "constraint";
    private static final String COST_TYPE = "costType";
    private static final String COST = "cost";
    private static final String BANDWIDTH = "bandwidth";
    private static final String ENDPOINT = "endPoint";
    private static final String SRC_ENDPOINT = "source";
    private static final String DST_ENDPOINT = "destination";
    private static final String MISSING_MEMBER_MESSAGE = " member is required in virtual network";

    @Override
    public VirtualNetwork decode(ObjectNode json, CodecContext context) {
        if (json == null || !json.isObject()) {
            log.error("Empty json input");
            return null;
        }

        // build virtual network
        VirtualNetwork.Builder resultBuilder = new DefaultVirtualNetwork.Builder();

        // retrieve VN name
        JsonNode jNode = json.get(VN_NAME);
        if (jNode != null) {
            String vnName = jNode.asText();
            resultBuilder.vnName(vnName);
        }

        // retrieve constraint
        JsonNode constraintJNode = (JsonNode) json.path(CONSTRAINT);
        if ((constraintJNode != null) && (!constraintJNode.isMissingNode())) {
            // retrieve cost
            jNode = constraintJNode.get(COST_TYPE);
            if (jNode != null) {
                String costType = jNode.asText();
                // TODO: validation
                jNode = constraintJNode.get(COST);
                if (jNode != null) {
                    String cost = jNode.asText();
                    resultBuilder.cost(costType, cost);
                }
            }

            // retrieve bandwidth
            jNode = constraintJNode.get(BANDWIDTH);
            if (jNode != null) {
                String bandwidth = jNode.asText();
                resultBuilder.bandwidth(bandwidth);
            }
        }

        // retrieve constraint
        JsonNode enpointJNode = (JsonNode) json.path(ENDPOINT);
        if ((enpointJNode != null) && (!enpointJNode.isMissingNode())) {
            JsonNode array = enpointJNode.get(SRC_ENDPOINT);
            if (array != null) {
                List<String> srcEndPoint = new LinkedList<>();

                Iterator<JsonNode> itr =  array.iterator();
                while (itr.hasNext()) {
                    srcEndPoint.add(itr.next().asText());
                }
                resultBuilder.source(srcEndPoint);
            }

            array = enpointJNode.get(DST_ENDPOINT);
            if (array != null) {
                List<String> dstEndPoint = new LinkedList<>();

                Iterator<JsonNode> itr =  array.iterator();
                while (itr.hasNext()) {
                    dstEndPoint.add(itr.next().asText());
                }
                resultBuilder.destination(dstEndPoint);
            }
        }

        return resultBuilder.build();
    }

    @Override
    public ObjectNode encode(VirtualNetwork vn, CodecContext context) {
        checkNotNull(vn, "virtual network output cannot be null");
        VnCost vnCost = (VnCost) vn.cost();
        VnBandwidth vnbandwidth = (VnBandwidth) vn.bandwidth();

        ObjectNode result = context.mapper()
                .createObjectNode()
                .put(VN_NAME, vn.vnName());

        ObjectNode endPointNode = context.mapper()
                .createObjectNode()
                .put(SRC_ENDPOINT, vn.endPoint().src().toString())
                .put(DST_ENDPOINT, vn.endPoint().dst().toString());
        result.set(ENDPOINT, endPointNode);

        ObjectNode constraintNode = context.mapper()
                .createObjectNode()
                .put(COST_TYPE, vnCost.type().toString())
                .put(COST, vnCost.cost().toString())
                .put(BANDWIDTH, String.valueOf(vnbandwidth.bandWidthValue().bps()));

        result.set(CONSTRAINT, constraintNode);
        return result;
    }
}
