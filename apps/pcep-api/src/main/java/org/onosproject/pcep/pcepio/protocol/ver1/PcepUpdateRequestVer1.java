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

package org.onosproject.pcep.pcepio.protocol.ver1;

import org.onosproject.pcep.pcepio.exceptions.PcepParseException;
import org.onosproject.pcep.pcepio.protocol.PcepAssociationObject;
import org.onosproject.pcep.pcepio.protocol.PcepLspObject;
import org.onosproject.pcep.pcepio.protocol.PcepMsgPath;
import org.onosproject.pcep.pcepio.protocol.PcepSrpObject;
import org.onosproject.pcep.pcepio.protocol.PcepUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

import java.util.LinkedList;

/**
 * Provides PCEP Update Request List.
 */
public class PcepUpdateRequestVer1 implements PcepUpdateRequest {

    /*                     <update-request-list>
     * Where:
     *   <update-request-list>     ::= <update-request>[<update-request-list>]
     *   <update-request>          ::= <SRP>
     *                                 <LSP>
     *                                 <path>
     * Where:
     *   <path>                     ::= <ERO><attribute-list>
     * Where:
     * <attribute-list> is defined in [RFC5440] and extended by PCEP extensions.
     */

    protected static final Logger log = LoggerFactory.getLogger(PcepUpdateRequestVer1.class);

    //PCEP SRP Object
    private PcepSrpObject srpObject;
    //PCEP LSP Object
    private PcepLspObject lspObject;
    //PCEP Message path
    private PcepMsgPath msgPath;
    //PCEP Association list
    private LinkedList<PcepAssociationObject> llAssociationObjectList;

    /**
     * Default constructor.
     */
    public PcepUpdateRequestVer1() {
        srpObject = null;
        lspObject = null;
        msgPath = null;
        llAssociationObjectList = null;
    }

    /**
     * Constructor to initialize all member variables.
     *
     * @param srpObject srp object
     * @param lspObject lsp object
     * @param llAssociationObjectList PCEP association object list
     * @param msgPath message path object
     */
    public PcepUpdateRequestVer1(PcepSrpObject srpObject, PcepLspObject lspObject,
                                 LinkedList<PcepAssociationObject> llAssociationObjectList, PcepMsgPath msgPath) {
        this.srpObject = srpObject;
        this.lspObject = lspObject;
        this.llAssociationObjectList = llAssociationObjectList;
        this.msgPath = msgPath;
    }

    @Override
    public PcepSrpObject getSrpObject() {
        return srpObject;
    }

    @Override
    public PcepLspObject getLspObject() {
        return lspObject;
    }

    @Override
    public LinkedList<PcepAssociationObject> getAssociationObjectList() {
        return llAssociationObjectList;
    }

    @Override
    public PcepMsgPath getMsgPath() {
        return msgPath;
    }

    @Override
    public void setSrpObject(PcepSrpObject srpObject) {
        this.srpObject = srpObject;

    }

    @Override
    public void setLspObject(PcepLspObject lspObject) {
        this.lspObject = lspObject;
    }

    @Override
    public void setAssociationObjectList(LinkedList<PcepAssociationObject> llAssociationObj) {
        this.llAssociationObjectList = llAssociationObj;
    }

    @Override
    public void setMsgPath(PcepMsgPath msgPath) {
        this.msgPath = msgPath;
    }

    /**
     * Builder class for PCEP update request.
     */
    public static class Builder implements PcepUpdateRequest.Builder {

        private boolean bIsSrpObjectSet = false;
        private boolean bIsLspObjectSet = false;
        private boolean bIsPcepMsgPathSet = false;
        private boolean bIsllAssociationObjSet = false;

        //PCEP SRP Object
        private PcepSrpObject srpObject;
        //PCEP LSP Object
        private PcepLspObject lspObject;
        //PCEP Attribute list
        private PcepMsgPath msgPath;
        //PCEP Association list
        private LinkedList<PcepAssociationObject> llAssociationObjectList;

        @Override
        public PcepUpdateRequest build() throws PcepParseException {

            //PCEP SRP Object
            PcepSrpObject srpObject = null;
            //PCEP LSP Object
            PcepLspObject lspObject = null;
            //PCEP Attribute list
            PcepMsgPath msgPath = null;
            //PCEP Association list
            LinkedList<PcepAssociationObject> llAssociationObjectList = null;

            if (!this.bIsSrpObjectSet) {
                throw new PcepParseException(" SRP Object NOT Set while building PcepUpdateRequest.");
            } else {
                srpObject = this.srpObject;
            }
            if (!this.bIsLspObjectSet) {
                throw new PcepParseException(" LSP Object NOT Set while building PcepUpdateRequest.");
            } else {
                lspObject = this.lspObject;
            }
            if (bIsllAssociationObjSet) {
                llAssociationObjectList = this.llAssociationObjectList;
            }
            if (!this.bIsPcepMsgPathSet) {
                throw new PcepParseException(" Msg Path NOT Set while building PcepUpdateRequest.");
            } else {
                msgPath = this.msgPath;
            }

            return new PcepUpdateRequestVer1(srpObject, lspObject, llAssociationObjectList, msgPath);
        }

        @Override
        public PcepSrpObject getSrpObject() {
            return this.srpObject;
        }

        @Override
        public PcepLspObject getLspObject() {
            return this.lspObject;
        }

        @Override
        public LinkedList<PcepAssociationObject> getAssociationObjectList() {
            return this.llAssociationObjectList;
        }

        @Override
        public PcepMsgPath getMsgPath() {
            return this.msgPath;
        }

        @Override
        public Builder setSrpObject(PcepSrpObject srpobj) {
            this.srpObject = srpobj;
            this.bIsSrpObjectSet = true;
            return this;

        }

        @Override
        public Builder setLspObject(PcepLspObject lspObject) {
            this.lspObject = lspObject;
            this.bIsLspObjectSet = true;
            return this;
        }

        @Override
        public Builder setAssociationObjectList(LinkedList<PcepAssociationObject> llAssociationObj) {
            this.llAssociationObjectList = llAssociationObj;
            this.bIsllAssociationObjSet = true;
            return this;
        }

        @Override
        public Builder setMsgPath(PcepMsgPath msgPath) {
            this.msgPath = msgPath;
            this.bIsPcepMsgPathSet = true;
            return this;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("SrpObject", srpObject)
                .add("LspObject", lspObject)
                .add("AssociationObjectList", llAssociationObjectList)
                .add("MsgPath", msgPath)
                .toString();
    }
}
