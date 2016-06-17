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
 * Abstraction of an entity providing PCEP ASSOCIATION Object.
 */
public interface PcepAssociationObject {

    /**
     * Returns ASSOCIATION TYPE of ASSOCIATION Object.
     *
     * @return ASSOCIATION TYPE of ASSOCIATION Object
     */
    short getAssociationType();

    /**
     * Sets ASSOCIATION TYPE with specified value.
     *
     * @param associationType ASSOCIATION TYPE of ASSOCIATION Object
     */
    void setAssociationType(short associationType);

    /**
     * Returns ASSOCIATION ID of ASSOCIATION Object.
     *
     * @return ASSOCIATION ID of ASSOCIATION Object
     */
    short getAssociationID();

    /**
     * Sets ASSOCIATION ID with specified value.
     *
     * @param associationId ASSOCIATION ID of ASSOCIATION Object
     */
    void setAssociationID(short associationId);

    /**
     * Returns IPV4 ASSOCIATION SOURCE of ASSOCIATION Object.
     *
     * @return IPV4 ASSOCIATION SOURCE of ASSOCIATION Object
     */
    int getAssociationSource();

    /**
     * Sets IPV4 ASSOCIATION SOURCE with specified value.
     *
     * @param associationSource IPV4 ASSOCIATION SOURCE of ASSOCIATION Object
     */
    void setAssociationSource(int associationSource);

    /**
     * Returns R flag of ASSOCIATION Object.
     *
     * @return R flag of ASSOCIATION Object
     */
    boolean getRFlag();

    /**
     * Sets R flag with specified value.
     *
     * @param bRFlag R Flag of ASSOCIATION Object
     */
    void setRFlag(boolean bRFlag);

    /**
     * sets the optional TLvs.
     *
     * @param llOptionalTlv list of optional tlvs
     */
    void setOptionalTlv(LinkedList<PcepValueType> llOptionalTlv);

    /**
     * Returns list of optional tlvs.
     *
     * @return llOptionalTlv list of optional tlvs
     */
    LinkedList<PcepValueType> getOptionalTlv();

    /**
     * Writes the ASSOCIATION Object into channel buffer.
     *
     * @param bb channel buffer
     * @return Returns the writerIndex of this buffer
     * @throws PcepParseException when tlv is null
     */
    int write(ChannelBuffer bb) throws PcepParseException;

    /**
     * Builder interface with get and set functions to build ASSOCIATION object.
     */
    interface Builder {

        /**
         * Builds ASSOCIATION Object.
         *
         * @return ASSOCIATION Object
         * @throws PcepParseException when mandatory object is not set
         */
        PcepAssociationObject build() throws PcepParseException;

        /**
         * Returns ASSOCIATION object header.
         *
         * @return ASSOCIATION object header
         */
        PcepObjectHeader getAssociationObjHeader();

        /**
         * Sets ASSOCIATION object header and returns its builder.
         *
         * @param obj ASSOCIATION object header
         * @return Builder by setting ASSOCIATION object header
         */
        Builder setAssociationObjHeader(PcepObjectHeader obj);

        /**
         * Returns ASSOCIATION ID of ASSOCIATION Object.
         *
         * @return ASSOCIATION ID of ASSOCIATION Object
         */
        short getAssociationID();

        /**
         * Sets ASSOCIATION ID and returns its builder.
         *
         * @param associationID ASSOCIATION ID
         * @return Builder by setting ASSOCIATION ID
         */
        Builder setAssociationID(short associationID);

        /**
         * Sets ASSOCIATION TYPE and returns its builder.
         *
         * @param associationType ASSOCIATION TYPE
         * @return Builder by setting ASSOCIATION TYPE
         */
        Builder setAssociationType(short associationType);

        /**
         * Returns ASSOCIATION TYPE of ASSOCIATION Object.
         *
         * @return ASSOCIATION TYPE of ASSOCIATION Object
         */
        short getAssociationType();

        /**
         * Sets IPV4 ASSOCIATION SOURCE and returns its builder.
         *
         * @param associationSource IPV4 ASSOCIATION SOURCE
         * @return Builder by setting IPV4 ASSOCIATION SOURCE
         */
        Builder setAssociationSource(int associationSource);

        /**
         * Returns ASSOCIATION SOURCE of ASSOCIATION Object.
         *
         * @return ASSOCIATION SOURCE of ASSOCIATION Object
         */
        int getAssociationSource();

        /**
         * Returns R flag of ASSOCIATION Object.
         *
         * @return R flag of ASSOCIATION Object
         */
        boolean getRFlag();

        /**
         * Sets R flag and returns its builder.
         *
         * @param bRFlag R flag
         * @return Builder by setting R flag
         */
        Builder setRFlag(boolean bRFlag);


        /**
         * Returns list of optional tlvs.
         *
         * @return llOptionalTlv list of optional tlvs
         */
        LinkedList<PcepValueType> getOptionalTlv();

        /**
         * sets the optional TLvs.
         *
         * @param llOptionalTlv List of optional tlv
         * @return builder by setting list of optional tlv.
         */
        Builder setOptionalTlv(LinkedList<PcepValueType> llOptionalTlv);

        /**
         * Sets P flag in ASSOCIATION object header and returns its builder.
         *
         * @param value boolean value to set P flag
         * @return Builder by setting P flag
         */
        Builder setPFlag(boolean value);

        /**
         * Sets I flag in ASSOCIATION object header and returns its builder.
         *
         * @param value boolean value to set I flag
         * @return Builder by setting I flag
         */
        Builder setIFlag(boolean value);
    }
}
