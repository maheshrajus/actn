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
package org.onosproject.vn.manager.api;

import org.onosproject.incubator.net.tunnel.Tunnel;
import org.onosproject.vn.manager.constraint.VnConstraint;
import org.onosproject.vn.store.EndPoint;
import org.onosproject.vn.store.VirtualNetworkInfo;

import java.util.List;

/**
 * Service to compute path based on constraints, delate path,
 * update path with new constraints.
 */
public interface VnService {

    /**
     * Creates new path based on constraints and LSP type.
     *
     * @param vnName VN name
     * @param constraints VN constraints
     * @param endPoint end points
     * @return false on failure and true on successful path creation
     */
    boolean setupVn(String vnName, List<VnConstraint> constraints, EndPoint endPoint);

    /**
     * Creates new path based on constraints and LSP type.
     *
     * @param vnName VN name
     * @param endPoint end points
     * @return false on failure and true on successful path creation
     */
    boolean setupVn(String vnName, EndPoint endPoint);

    /**
     * Updates existing end points.
     *
     * @param vnName VN name
     * @param endpoints end points
     * @return false on failure and true on successful path update
     */
    boolean updateVn(String vnName, EndPoint endpoints);

    /**
     * Updates existing end points.
     *
     * @param vnName VN name
     * @param constraints constraints
     * @return false on failure and true on successful path update
     */
    boolean updateVn(String vnName, List<VnConstraint> constraints);

    /**
     * Removes an existing path.
     *
     * @param vnName VN name
     * @return false on failure and true on successful path removal
     */
    boolean deleteVn(String vnName);

    /**
     * Queries particular virtual network based on name.
     *
     * @param vnName virtual network name
     * @return virtual network if exists, otherwise null
     */
    VirtualNetworkInfo queryVn(String vnName);

    /**
     * Queries all virtual network.
     *
     * @return Virtual networks if exists, otherwise empty list
     */
    List<VirtualNetworkInfo> queryAllVn();

    /**
     * Queries all tunnels in all virtual network.
     *
     * @return tunnels if exists, otherwise empty list
     */
    Iterable<Tunnel> queryAllTunnels();

    /**
     * Queries all tunnels in a virtual network.
     *
     * @return tunnels if exists, otherwise null
     */
    Iterable<Tunnel> queryVnTunnels(String vnName);
}
