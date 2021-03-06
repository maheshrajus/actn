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
package org.onosproject.vn.store.api;

import org.onosproject.net.intent.Constraint;
import org.onosproject.vn.api.VnEndPoints;
import org.onosproject.vn.store.VirtualNetworkInfo;

import java.util.List;
import java.util.Map;

/**
 * Abstraction of an entity providing pool of available labels to devices, links and tunnels.
 */
public interface VnStore {

    /**
     * Setup virtual network map with bandwidth, cost, endpoint.
     *
     * @param vnName virtual network name
     * @param constraints virtual network constraints
     * @param endPoint virtual network endpoint
     * @return success or failure
     */
    boolean add(String vnName, VnEndPoints endPoint, List<Constraint> constraints);


    /**
     * Updates virtual network map with cost.
     *
     * @param vnName virtual network name
     * @param constraints virtual network constraints
     * @return success or failure
     */
    boolean update(String vnName, List<Constraint> constraints);

    /**
     * Updates virtual network map with endpoint.
     *
     * @param vnName virtual network name
     * @param endPoint virtual network endpoint
     * @return success or failure
     */
    boolean update(String vnName, VnEndPoints endPoint);

    /**
     * Removes virtual network.
     *
     * @param vnName virtual network
     * @return success or failure
     */
    boolean delete(String vnName);

    /**
     * Returns virtual network.
     *
     * @param vnName virtual network
     * @return virtual network data
     */
    VirtualNetworkInfo query(String vnName);

    /**
     * Returns all virtual network.
     *
     * @return virtual networks
     */
    Map<String, VirtualNetworkInfo> queryAll();
}
