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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.incubator.net.tunnel.Tunnel;
import org.onosproject.net.DeviceId;
import org.onosproject.net.intent.Constraint;
import org.onosproject.pce.pceservice.DefaultPcePath;
import org.onosproject.pce.pceservice.PcePath;
import org.onosproject.pce.pceservice.constraint.CostConstraint;
import org.onosproject.rest.AbstractWebResource;
import org.onosproject.vn.vnservice.VirtualNetwork;
import org.onosproject.vn.vnservice.api.VnService;
import org.onosproject.vn.store.EndPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.OK;
import static org.onlab.util.Tools.nullIsNotFound;

/**
 * Query and program virtual network.
 */
@Path("vn")
public class VnWebResource extends AbstractWebResource {

    private final Logger log = LoggerFactory.getLogger(VnWebResource.class);
    public static final String VIRTUAL_NETWORK_NOT_FOUND = "virtual network not found";
    public static final String VIRTUAL_NETWORK_SETUP_FAILED = "Vitrual network setup has failed.";

    /**
     * Retrieve details of all paths created.
     *
     * @return 200 OK
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryAllVn() {
        log.debug("Query all VN.");
        Iterable<Tunnel> vnTunnels = get(VnService.class).queryAllVnTunnels();
        ObjectNode result = mapper().createObjectNode();
        ArrayNode pathEntry = result.putArray("vn");
        if (vnTunnels != null) {
            for (final Tunnel t : vnTunnels) {
                //VirtualNetwork vn = DefaultVirtualNetwork.builder().of(virtualNetwork).build();
                //pathEntry.add(codec(VirtualNetwork.class).encode(vn, this));
                PcePath path = DefaultPcePath.builder().of(t).build();
                pathEntry.add(codec(PcePath.class).encode(path, this));
            }
        }
        return ok(result.toString()).build();
    }

    /**
     * Retrieve details of a specified virtual network.
     *
     * @param vnName virtual network name
     * @return 200 OK, 404 if given identifier does not exist
     */
    @GET
    @Path("{VnName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryVn(@PathParam("VnName") String vnName) {
        Iterable<Tunnel> vnTunnels = get(VnService.class).queryVnTunnels(vnName);
        ObjectNode result = mapper().createObjectNode();
        ArrayNode pathEntry = result.putArray("vn");
        if (vnTunnels != null) {
            for (final Tunnel t : vnTunnels) {
                //VirtualNetwork vn = DefaultVirtualNetwork.builder().of(virtualNetwork).build();
                //pathEntry.add(codec(VirtualNetwork.class).encode(vn, this));
                PcePath path = DefaultPcePath.builder().of(t).build();
                pathEntry.add(codec(PcePath.class).encode(path, this));

            }
        }
        return ok(result.toString()).build();
    }

    /**
     * Creates a new virtual network.
     *
     * @param stream virtual network from json
     * @return status of the request
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setupVn(InputStream stream) {
        log.debug("Setup virtual network.");
        try {
            ObjectNode jsonTree = (ObjectNode) mapper().readTree(stream);
            JsonNode vnNode = jsonTree.get("vn");

            VirtualNetwork vn = codec(VirtualNetwork.class).decode((ObjectNode) vnNode, this);

            List<DeviceId> src = new LinkedList<>();
            List<DeviceId> dst = new LinkedList<>();
            List<Constraint> constraints = new LinkedList<>();

            src.addAll(vn.endPoint().src().stream().collect(Collectors.toList()));

            dst.addAll(vn.endPoint().dst().stream().collect(Collectors.toList()));
            EndPoint endPoint = new EndPoint(src, dst);

            // Add bandwidth
            constraints.add(vn.bandwidth());
            CostConstraint cost = (CostConstraint) vn.cost();
            if ((cost.type().type() != 1) && (cost.type().type() != 2)) {
                throw new IOException("Invalid cost type");
            }

            // Add cost
            constraints.add(vn.cost());

            Boolean isSuccess = nullIsNotFound(get(VnService.class)
                                               .setupVn(vn.vnName(), endPoint, constraints),
                                               VIRTUAL_NETWORK_SETUP_FAILED);
            return Response.status(OK).entity(isSuccess.toString()).build();
        } catch (IOException e) {
            log.error("Exception while creating virtual network {}.", e.toString());
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Update details of a specified virtual network.
     *
     * @param vnName virtual network name
     * @param stream pce path from json
     * @return 200 OK, 404 if given identifier does not exist
     */
    @PUT
    @Path("{vnName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePath(@PathParam("vnName") String vnName,
            final InputStream stream) {
        log.debug("Update virtual network by VN name {}.", vnName);
        try {
            ObjectNode jsonTree = (ObjectNode) mapper().readTree(stream);
            JsonNode vn = jsonTree.get("vn");
            VirtualNetwork virtualNetwork = codec(VirtualNetwork.class).decode((ObjectNode) vn, this);
            List<Constraint> constraints = new LinkedList<>();
            boolean updateEndPoints = false;

            if ((virtualNetwork.endPoint().src().size() != 0)
                    || (virtualNetwork.endPoint().dst().size() != 0)) {
                updateEndPoints = true;
            }

            if (updateEndPoints && ((virtualNetwork.bandwidth() != null)
                    || (virtualNetwork.cost() != null))) {
                throw new IOException("Update virtual network either by Constraints or EndPoints not using both at " +
                                              "the same time");
            }

            if (updateEndPoints) {
                Boolean result = nullIsNotFound(get(VnService.class).updateVn(vnName, virtualNetwork.endPoint()),
                                                VIRTUAL_NETWORK_NOT_FOUND);
                return Response.status(OK).entity(result.toString()).build();
            }
            // Assign bandwidth
            if (virtualNetwork.bandwidth() != null) {
                constraints.add(virtualNetwork.bandwidth());
            }

            // Assign cost
            if (virtualNetwork.cost() != null) {
                CostConstraint vnCost = (CostConstraint) virtualNetwork.cost();
                if ((vnCost.type().type() != 1) && (vnCost.type().type() != 2)) {
                    throw new IOException("Invalid cost type");
                }
                constraints.add(virtualNetwork.cost());
            }

            Boolean result = nullIsNotFound(get(VnService.class).updateVn(vnName, constraints),
                                            VIRTUAL_NETWORK_NOT_FOUND);
            return Response.status(OK).entity(result.toString()).build();
        } catch (IOException e) {
            log.error("Update virtual network failed because of exception {}.", e.toString());
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Delete a specified virtual network.
     *
     * @param vnName virtual network name
     * @return 200 OK, 404 if given virtual network does not exist
     */
    @Path("{vnName}")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteVn(@PathParam("vnName") String vnName) {
        log.debug("Deletes virtual network by name {}.", vnName);

        Boolean isSuccess = nullIsNotFound(get(VnService.class).deleteVn(vnName),
                                           VIRTUAL_NETWORK_NOT_FOUND);
        if (!isSuccess) {
            log.debug("Virtual network {} does not exist", vnName);
        }

        return Response.status(OK).entity(isSuccess.toString()).build();
    }
}
