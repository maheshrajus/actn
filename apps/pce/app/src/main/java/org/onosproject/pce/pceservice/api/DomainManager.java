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

import org.onosproject.net.Path;

import java.util.Map;
import java.util.Set;

/**
 * Domain manager interface to provide set of paths for a path based on domain.
 */
public interface DomainManager {

    /**
     * Operation state.
     */
    enum Oper {
        /**
         * Signifies that the path is add.
         */
        ADD,

        /**
         * Signifies that the path is updated.
         */
        UPDATE,

        /**
         * Signifies that the path is delete.
         */
        DELETE
    }

    /**
     * Returns domain specific paths.
     *
     */
    Set<Path> getDomainSpecificPaths(Path path);

    /**
     * Returns domain specific paths to be process based on Oper type.
     *
     */
    Map<Oper, Set<Path>> compareDomainSpecificPaths(Set<Path> oldPaths, Set<Path> newPaths);
}
