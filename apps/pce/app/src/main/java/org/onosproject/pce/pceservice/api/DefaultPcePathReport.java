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

import org.onlab.packet.IpAddress;
import org.onosproject.net.Path;

import java.util.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Implementation of an entity which provides functionality of pce path report.
 */
public final class DefaultPcePathReport implements PcePathReport {

    private String pathName;
    private String srpId;
    private String plspId;
    private String localLspId;
    private String pceTunnelId;
    private boolean isDelegate;
    private boolean isSync;
    private boolean isRemoved;
    private State adminState;
    private State state;
    private IpAddress ingress;
    private IpAddress egress;
    private String errorInfo;
    private Path eroPath;
    private Path rroPath;
    private Path xroPath;

    /**
     * Initializes PCE path report attributes.
     *
     * @param pathName path name
     * @param plspId path plspid
     * @param localLspId path localLspId
     * @param pceTunnelId path tunnel id
     * @param isDelegate path delegate flag
     * @param isSync path sync flag
     * @param isRemoved path remove flag
     * @param adminState path administrative state
     * @param state path operation state
     * @param ingress path ingress
     * @param egress path egress
     * @param errorInfo path error info
     * @param eroPath path ero
     * @param rroPath path rro
     * @param xroPath path xro
     */
    private DefaultPcePathReport(String pathName, String srpId, String plspId, String localLspId, String pceTunnelId,
                                 boolean isDelegate, boolean isSync, boolean isRemoved, State adminState,
                                 State state, IpAddress ingress, IpAddress egress, String errorInfo,
                                 Path eroPath, Path rroPath, Path xroPath) {
        this.pathName = pathName;
        this.srpId = srpId;
        this.plspId = plspId;
        this.localLspId = localLspId;
        this.pceTunnelId = pceTunnelId;
        this.isDelegate = isDelegate;
        this.isSync = isSync;
        this.isRemoved = isRemoved;
        this.adminState = adminState;
        this.state = state;
        this.ingress = ingress;
        this.egress = egress;
        this.errorInfo = errorInfo;
        this.eroPath = eroPath;
        this.rroPath = rroPath;
        this.xroPath = xroPath;
    }

    @Override
    public String pathName() {
        return pathName;
    }

    @Override
    public String srpId() {
        return srpId;
    }

    @Override
    public String plspId() {
        return plspId;
    }

    @Override
    public  String localLspId() {
        return localLspId;
    }

    @Override
    public String pceTunnelId() {
        return pceTunnelId;
    }

    @Override
    public boolean isDelegate() {
        return isDelegate;
    }

    @Override
    public boolean isSync() {
        return isSync;
    }

    @Override
    public boolean isRemoved() {
        return isRemoved;
    }

    @Override
    public State adminState() {
        return adminState;
    }

    @Override
    public State state() {
        return state;
    }

    @Override
    public IpAddress ingress() {
        return ingress;
    }

    @Override
    public IpAddress egress() {
        return egress;
    }

    @Override
    public String errorInfo() {
        return errorInfo;
    }

    @Override
    public Path eroPath() {
        return eroPath;
    }

    @Override
    public Path rroPath() {
        return rroPath;
    }

    @Override
    public Path xroPath() {
        return xroPath;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathName, srpId, plspId, localLspId, pceTunnelId, isDelegate, isSync, isRemoved, adminState,
                state, ingress, egress, errorInfo, eroPath, rroPath, xroPath);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DefaultPcePathReport) {
            DefaultPcePathReport that = (DefaultPcePathReport) obj;
            return Objects.equals(pathName, that.pathName)
                    && Objects.equals(srpId, that.srpId)
                    && Objects.equals(plspId, that.plspId)
                    && Objects.equals(localLspId, that.localLspId)
                    && Objects.equals(pceTunnelId, that.pceTunnelId)
                    && Objects.equals(isDelegate, that.isDelegate)
                    && Objects.equals(isSync, that.isSync)
                    && Objects.equals(isRemoved, that.isRemoved)
                    && Objects.equals(adminState, that.adminState)
                    && Objects.equals(state, that.state)
                    && Objects.equals(ingress, that.ingress)
                    && Objects.equals(egress, that.egress)
                    && Objects.equals(errorInfo, that.errorInfo)
                    && Objects.equals(eroPath, that.eroPath)
                    && Objects.equals(rroPath, that.rroPath)
                    && Objects.equals(xroPath, that.xroPath);
        }
        return false;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("pathName", pathName())
                .add("srpId", srpId())
                .add("plspId", plspId())
                .add("localLspId", localLspId())
                .add("pceTunnelId", pceTunnelId())
                .add("isDelegate", String.valueOf(isDelegate()))
                .add("isSync", String.valueOf(isSync()))
                .add("isRemoved", String.valueOf(isRemoved()))
                .add("adminState", adminState().toString())
                .add("state", state().toString())
                .add("ingress", ingress().toString())
                .add("egress", egress().toString())
                .add("errorInfo", errorInfo())
                .add("eroPath", eroPath().toString())
                .add("rroPath", rroPath().toString())
                .add("xroPath", xroPath().toString())
                .toString();
    }

    /**
     * Creates an instance of the pce path report builder.
     *
     * @return instance of builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for pce path report.
     */
    public static final class Builder implements PcePathReport.Builder {
        private String pathName;
        private String srpId;
        private String plspId;
        private String localLspId;
        private String pceTunnelId;
        private boolean isDelegate;
        private boolean isSync;
        private boolean isRemoved;
        private State adminState;
        private State state;
        private IpAddress ingress;
        private IpAddress egress;
        private String errorInfo;
        private Path eroPath;
        private Path rroPath;
        private Path xroPath;

        @Override
        public Builder pathName(String name) {
            this.pathName = name;
            return this;
        }

        @Override
        public Builder srpId(String srpId) {
            this.srpId = srpId;
            return this;
        }

        @Override
        public Builder plspId(String plspId) {
            this.plspId = plspId;
            return this;
        }

        @Override
        public Builder localLspId(String localLspId) {
            this.localLspId = localLspId;
            return this;
        }

        @Override
        public Builder pceTunnelId(String tunnelId) {
            this.pceTunnelId = tunnelId;
            return this;
        }

        @Override
        public Builder isDelegate(boolean delegate) {
            this.isDelegate = delegate;
            return this;
        }

        @Override
        public Builder isSync(boolean sync) {
            this.isSync = sync;
            return this;
        }

        @Override
        public Builder isRemoved(boolean removed) {
            this.isRemoved = removed;
            return this;
        }

        @Override
        public Builder adminState(State adminState) {
            this.adminState = adminState;
            return this;
        }

        @Override
        public Builder state(State state) {
            this.state = state;
            return this;
        }

        @Override
        public Builder ingress(IpAddress ingress) {
            this.ingress = ingress;
            return this;
        }

        @Override
        public Builder egress(IpAddress egress) {
            this.egress = egress;
            return this;
        }

        @Override
        public Builder errorInfo(String error) {
            this.errorInfo = error;
            return this;
        }

        @Override
        public Builder eroPath(Path path) {
            this.eroPath = path;
            return this;
        }

        @Override
        public Builder rroPath(Path path) {
            this.rroPath = path;
            return this;
        }

        @Override
        public Builder xroPath(Path path) {
            this.xroPath = path;
            return this;
        }

        @Override
        public PcePathReport build() {
            return new DefaultPcePathReport(pathName, srpId, plspId, localLspId, pceTunnelId, isDelegate, isSync,
                    isRemoved, adminState, state, ingress, egress, errorInfo, eroPath, rroPath, xroPath);
        }
    }
}