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
package org.onosproject.pce.pceservice.api;

import java.util.List;
import java.util.Set;

import org.onlab.packet.IpAddress;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.intent.Constraint;
import org.onosproject.pce.pceservice.LspType;
import org.onosproject.incubator.net.tunnel.Tunnel;
import org.onosproject.incubator.net.tunnel.TunnelId;

/**
 * Service to compute path based on constraints, release path,
 * update path with new constraints and query existing tunnels.
 */
public interface PceService {

    /**
     * Compute new path based on constraints and LSP type.
     *
     * @param src source device
     * @param dst destination device
     * @param constraints list of constraints to be applied on path
     * @return set of path on successful computation or empty set if no path
     */
    Set<Path> computePath(DeviceId src, DeviceId dst, List<Constraint> constraints);

    /**
     * Creates new path based on constraints and LSP type.
     *
     * @param src source device
     * @param dst destination device
     * @param tunnelName name of the tunnel
     * @param constraints list of constraints to be applied on path
     * @param lspType type of path to be setup
     * @param vnName virtual network
     * @return false on failure and true on successful path creation
     */
    boolean setupPath(DeviceId src, DeviceId dst, String tunnelName, List<Constraint> constraints, LspType lspType,
                      String vnName);

    /**
     * Creates new path based on constraints and LSP type.
     *
     * @param vnName virtual network name
     * @param srcLsrId source device LSRId
     * @param dstLsrId destination device LSRId
     * @param tunnelName name of the tunnel
     * @param constraints list of constraints to be applied on path
     * @param lspType type of path to be setup
     * @return false on failure and true on successful path creation
     */
    boolean setupPath(String vnName, IpAddress srcLsrId, IpAddress dstLsrId, String tunnelName,
                      List<Constraint> constraints, LspType lspType);

    /**
     * Updates an existing path.
     *
     * @param tunnelId tunnel identifier
     * @param constraints list of constraints to be applied on path
     * @return false on failure and true on successful path update
     */
    boolean updatePath(TunnelId tunnelId, List<Constraint> constraints);

    /**
     * Updates an existing path.
     *
     * @param plspId plspId of the path
     * @param constraints list of constraints to be applied on path
     * @return false on failure and true on successful path update
     */
    boolean updatePath(String plspId, List<Constraint> constraints);

    /**
     * Releases an existing path.
     *
     * @param tunnelId tunnel identifier
     * @return false on failure and true on successful path removal
     */
    boolean releasePath(TunnelId tunnelId);

    /**
     * Releases an existing path.
     *
     * @param plspId plspId of the path
     * @return false on failure and true on successful path removal
     */
    boolean releasePath(String plspId);

    /**
     * Queries all paths.
     *
     * @return iterable of existing tunnels
     */
    Iterable<Tunnel> queryAllPath();

    /**
     * Queries particular path based on tunnel identifier.
     *
     * @param tunnelId tunnel identifier
     * @return tunnel if path exists, otherwise null
     */
    Tunnel queryPath(TunnelId tunnelId);

    /**
     * Queries particular path based on virtual network.
     *
     * @param vnName virtual network
     * @return iterable of existing tunnels associated with virtual network, otherwise null
     */
    Iterable<Tunnel> queryPath(String vnName);

    /**
     * Register a listener for PCE path update events.
     *
     * @param listener the listener to notify
     */
    void addListener(PcePathUpdateListener listener);

    /**
     * Unregister a listener for PCE path update events.
     *
     * @param listener the listener to unregister
     */
    void removeListener(PcePathUpdateListener listener);

    /**
     * Returns default LSP type.
     *
     * @return default lsp type
     */
    LspType defaultLspType();

    /**
     * Sets default LSP type.
     *
     * @param lspType lspType lsp type
     */
    void setdefaultLspType(LspType lspType);

    /**
     * Returns if the link has the available bandwidth or not.
     *
     * @return true if bandwidth is available else false
     */
    boolean pceBandwidthAvailable(Link link, Double bandwidth);

    /**
     * Returns the tunnels which are initiated.
     * @return list of PcePathReport if tunnels exist otherwise empty list.
     */
    List<PcePathReport> queryAllInitiateTunnels();
}
