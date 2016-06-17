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

package org.onosproject.pcep.pcepio.protocol;

import java.util.LinkedList;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.pcep.pcepio.exceptions.PcepParseException;
import org.onosproject.pcep.pcepio.types.PcepObjectHeader;
import org.onosproject.pcep.pcepio.types.PcepValueType;

/**
 * Abstraction of an entity providing PCEP XRO Object.
 */
public interface PcepXroObject {

    /**
     * Return LinkedList of SubObjects of XRO Object.
     *
     * @return list of subobjects
     */
    LinkedList<PcepValueType> getSubObjects();

    /**
     * Sets LinkedList of SubObjects in XRO Object.
     *
     * @param llSubObjects list of subobjects
     */
    void setSubObjects(LinkedList<PcepValueType> llSubObjects);

    /**
     * Returns F flag of XRO Object.
     *
     * @return F flag of XRO Object
     */
    boolean getFflag();

    /**
     * Sets F flag and returns its builder.
     *
     * @param bFflag F flag
     * @return Builder by setting F flag
     */
    void setFflag(boolean bFflag);

    /**
     * Writes the XRO Object into channel buffer.
     *
     * @param cb channel buffer
     * @return Returns the writerIndex of this buffer
     * @throws PcepParseException while writing XRO Object into ChannelBuffer
     */
    int write(ChannelBuffer cb) throws PcepParseException;

    /**
     * Builder interface with get and set functions to build XRO object.
     */
    interface Builder {

        /**
         * Builds XRO Object.
         *
         * @return XRO Object
         */
        PcepXroObject build();

        /**
         * Returns XRO Object Header.
         *
         * @return XRO Object Header
         */
        PcepObjectHeader getXroObjHeader();

        /**
         * Sets XRO Object header and returns its builder.
         *
         * @param obj XRO Object header
         * @return Builder by setting XRO Object header
         */
        Builder setXroObjHeader(PcepObjectHeader obj);

        /**
         * Returns F flag of XRO Object.
         *
         * @return F flag of XRO Object
         */
        boolean getFflag();

        /**
         * Sets F flag and returns its builder.
         *
         * @param bFflag F flag
         * @return Builder by setting F flag
         */
        Builder setFflag(boolean bFflag);

        /**
         * Returns LinkedList of SubObjects in XRO Objects.
         *
         * @return list of subobjects
         */
        LinkedList<PcepValueType> getSubObjects();

        /**
         * Sets LinkedList of SubObjects and returns its builder.
         *
         * @param llSubObjects list of SubObjects
         * @return Builder by setting list of SubObjects
         */
        Builder setSubObjects(LinkedList<PcepValueType> llSubObjects);

        /**
         * Sets P flag in XRO object header and returns its builder.
         *
         * @param value boolean value to set P flag
         * @return Builder by setting P flag
         */
        Builder setPFlag(boolean value);

        /**
         * Sets I flag in XRO object header and returns its builder.
         *
         * @param value boolean value to set I flag
         * @return Builder by setting I flag
         */
        Builder setIFlag(boolean value);
    }
}
