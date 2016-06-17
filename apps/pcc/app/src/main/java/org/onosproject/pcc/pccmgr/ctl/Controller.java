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
package org.onosproject.pcc.pccmgr.ctl;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.onosproject.pcc.pccmgr.api.PceId;
import org.onosproject.pcc.pccmgr.api.PcepAgent;
import org.onosproject.pcc.pccmgr.api.PcepClientDriver;
import org.onosproject.pcc.pccmgr.api.PcepPacketStats;
import org.onosproject.pcep.pcepio.protocol.PcepFactories;
import org.onosproject.pcep.pcepio.protocol.PcepFactory;
import org.onosproject.pcep.pcepio.protocol.PcepVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.onlab.util.Tools.groupedThreads;

/**
 * The main controller class. Handles all setup and network listeners -
 * Distributed ownership control of pcc through IControllerRegistryService
 */
public class Controller {

    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    private static final PcepFactory FACTORY1 = PcepFactories.getFactory(PcepVersion.PCEP_1);
    public static final int PCEP_PORT_NUM = 4189;

    private ChannelGroup cg;

    // Configuration options
    private int workerThreads = 10;

    // Start time of the controller
    private long systemStartTime;

    private PcepAgent agent;

    private NioClientSocketChannelFactory execFactory;
    private static ClientBootstrap peerBootstrap;

    public Controller() {
        PcepConfig pcepConfig = PcepConfig.getInstance();
        if (pcepConfig != null) {
            pcepConfig.setController(this);
        }
    }

    // Perf. related configuration
    private static final int SEND_BUFFER_SIZE = 4 * 1024 * 1024;

    /**
     * Returns factory version for processing pcep messages.
     *
     * @return instance of factory version
     */
    public PcepFactory getPcepMessageFactory1() {
        return FACTORY1;
    }

    /**
     * To get system start time.
     *
     * @return system start time in milliseconds
     */
    public long getSystemStartTime() {
        return (this.systemStartTime);
    }

    /**
     * Tell controller that we're ready to accept pcc connections.
     */
    public void run() {
        try {
            peerBootstrap = createPeerBootStrap();

            peerBootstrap.setOption("reuseAddr", true);
            peerBootstrap.setOption("child.keepAlive", true);
            peerBootstrap.setOption("child.tcpNoDelay", true);
            peerBootstrap.setOption("child.sendBufferSize", Controller.SEND_BUFFER_SIZE);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates peer boot strap.
     *
     * @return ClientBootstrap
     */
    private ClientBootstrap createPeerBootStrap() {

        if (workerThreads == 0) {
            execFactory = new NioClientSocketChannelFactory(
                    Executors.newCachedThreadPool(groupedThreads("onos/pcc", "boss-%d")),
                    Executors.newCachedThreadPool(groupedThreads("onos/pcc", "worker-%d")));
            return new ClientBootstrap(execFactory);
        } else {
            execFactory = new NioClientSocketChannelFactory(
                    Executors.newCachedThreadPool(groupedThreads("onos/pcc",  "boss-%d")),
                    Executors.newCachedThreadPool(groupedThreads("onos/pcc", "worker-%d")),
                    workerThreads);
            return new ClientBootstrap(execFactory);
        }
    }

    /**
     * Gets peer bootstrap.
     *
     * @return peer  bootstrap
     */
    public static ClientBootstrap peerBootstrap() {
        return peerBootstrap;
    }

    /**
     * Initialize internal data structures.
     */
    public void init() {
        // These data structures are initialized here because other
        // module's startUp() might be called before ours
        this.systemStartTime = System.currentTimeMillis();
    }

    public Map<String, Long> getMemory() {
        Map<String, Long> m = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        m.put("total", runtime.totalMemory());
        m.put("free", runtime.freeMemory());
        return m;
    }

    public Long getUptime() {
        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        return rb.getUptime();
    }

    /**
     * Creates instance of Pcep client.
     *
     * @param pceId pce identifier
     * @param sessionID session id
     * @param pv pcep version
     * @param pktStats pcep packet statistics
     * @return instance of PcepClient
     */
    protected PcepClientDriver getPcepClientInstance(PceId pceId, int sessionID, PcepVersion pv,
            PcepPacketStats pktStats) {
        PcepClientDriver pcepClientDriver = new PcepClientImpl();
        pcepClientDriver.init(pceId, pv, pktStats);
        pcepClientDriver.setAgent(agent);
        return pcepClientDriver;
    }

    /**
     * Starts the pcep controller.
     *
     * @param ag Pcep agent
     */
    public void start(PcepAgent ag) {
        log.info("Started");
        this.agent = ag;
        this.init();
        this.run();
    }

    public PcepAgent getAgent() {
        return this.agent;
    }
    /**
     * Stops the pcep controller.
     */
    public void stop() {
        log.info("Stopped");
        execFactory.shutdown();
        cg.close();
    }
}
