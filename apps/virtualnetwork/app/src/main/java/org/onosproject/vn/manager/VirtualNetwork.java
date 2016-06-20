/*
 * Copyright 2016 Open Networking Laboratory
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

import org.onosproject.vn.manager.constraint.VnConstraint;
import org.onosproject.vn.store.EndPoint;
import org.onosproject.vn.store.VirtualNetworkInfo;

import java.util.List;

/**
 * Abstraction of an entity which provides functionalities of virtual network.
 */
public interface VirtualNetwork {

    /**
     * Returns the attribute virtual network.
     *
     * @return virtualnetwork name
     */
    String vnName();

    /**
     * Returns the attribute source endpoint.
     *
     * @return source end point
     */
    List<String> source();

    /**
     * Sets the attribute source endpoint.
     *
     * @param src source endpoint
     */
    void source(List<String> src);

   /**
    * Returns the attribute destination endpoint.
    *
    * @return destination end point
    */
   List<String> destination();

   /**
    * Sets the attribute destination endpoint.
    *
    * @param dst destination end point.
    */
   void destination(List<String> dst);

    /**
     * Returns the attribute cost constraint.
     *
     * @return cost constraint
     */
    VnConstraint cost();

    /**
     * Returns the attribute bandwidth constraint.
     *
     * @return bandwidth constraint
     */
    VnConstraint bandwidth();

    /**
     * Copies only non-null or non-zero member variables.
     *
     * @param vn virtual network name
     * @return virtual network
     */
    VirtualNetwork copy(VirtualNetwork vn);

    /**
     * Returns the end point.
     *
     * @return end point
     */
    EndPoint endPoint();

    /**
     * Builder for pce path.
     */
    interface Builder {

        /**
         * Returns the builder object of virtual network name.
         *
         * @param vnName virtual network name
         * @return builder object of virtual network name
         */
        Builder vnName(String vnName);

        /**
         * Returns the builder object of ingress.
         *
         * @param source ingress
         * @return builder object of ingress
         */
        Builder source(List<String> source);

        /**
         * Returns the builder object of egress.
         *
         * @param destination egress
         * @return builder object of egress
         */
        Builder destination(List<String> destination);

        /**
         * Returns the builder object of cost constraint.
         *
         * @param costType constraint type
         * @return builder object of cost constraint
         */
        Builder cost(String costType);

        /**
         * Returns the builder object of bandwidth constraint.
         *
         * @param bandwidth constraint
         * @return builder object of bandwidth constraint
         */
        Builder bandwidth(String bandwidth);

        /**
         * Copies virtual network information to local.
         *
         * @param vn virtual network
         * @return object of virtual network
         */
        Builder of(VirtualNetworkInfo vn);

        /**
         * Builds object of virtual network.
         *
         * @return object of virtual network.
         */
        VirtualNetwork build();
    }
}
