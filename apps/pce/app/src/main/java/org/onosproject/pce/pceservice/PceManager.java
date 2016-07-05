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
package org.onosproject.pce.pceservice;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;

import org.onlab.packet.Ethernet;
import org.onlab.packet.IPv4;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.IpAddress;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.TCP;
import org.onlab.util.Bandwidth;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;

import org.onosproject.incubator.net.resource.label.LabelResourceAdminService;
import org.onosproject.incubator.net.resource.label.LabelResourceId;
import org.onosproject.incubator.net.resource.label.LabelResourceService;
import org.onosproject.core.IdGenerator;
import org.onosproject.incubator.net.tunnel.DefaultTunnel;
import org.onosproject.incubator.net.tunnel.IpTunnelEndPoint;
import org.onosproject.incubator.net.tunnel.LabelStack;
import org.onosproject.incubator.net.tunnel.Tunnel;
import org.onosproject.incubator.net.tunnel.TunnelAdminService;
import org.onosproject.incubator.net.tunnel.TunnelEndPoint;
import org.onosproject.incubator.net.tunnel.TunnelEvent;
import org.onosproject.incubator.net.tunnel.TunnelId;
import org.onosproject.incubator.net.tunnel.TunnelListener;
import org.onosproject.incubator.net.tunnel.TunnelName;
import org.onosproject.incubator.net.tunnel.TunnelService;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.LinkKey;
import org.onosproject.net.MastershipRole;
import org.onosproject.net.Path;
import org.onosproject.net.DefaultAnnotations.Builder;
import org.onosproject.net.config.ConfigFactory;
import org.onosproject.net.config.NetworkConfigEvent;
import org.onosproject.net.config.NetworkConfigListener;
import org.onosproject.net.config.NetworkConfigRegistry;
import org.onosproject.net.config.NetworkConfigService;
import org.onosproject.net.config.basics.SubjectFactories;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.Objective;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.link.LinkListener;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.link.LinkService;
import org.onosproject.pce.pceservice.api.DefaultPcePathReport;
import org.onosproject.pce.pceservice.api.PcePathReport;
import org.onosproject.pce.pceservice.api.PcePathUpdateListener;
import org.onosproject.pce.pceservice.constraint.CapabilityConstraint;
import org.onosproject.pce.pceservice.constraint.CapabilityConstraint.CapabilityType;
import org.onosproject.net.topology.LinkWeight;
import org.onosproject.net.topology.PathService;
import org.onosproject.net.topology.TopologyEdge;
import org.onosproject.net.topology.TopologyEvent;
import org.onosproject.net.topology.TopologyListener;
import org.onosproject.net.topology.TopologyService;
import org.onosproject.pce.pceservice.api.DomainManager;
import org.onosproject.pce.pceservice.api.PceService;
import org.onosproject.pce.pceservice.constraint.CostConstraint;
import org.onosproject.pce.pceservice.constraint.ExcludeDeviceConstraint;
import org.onosproject.pce.pceservice.constraint.PceBandwidthConstraint;
import org.onosproject.pce.pceservice.constraint.SharedBandwidthConstraint;
import org.onosproject.pce.pcestore.PcePathInfo;
import org.onosproject.pce.pcestore.api.PceStore;
import org.onosproject.pcep.api.DeviceCapability;
import org.onosproject.pcep.api.PcepSrpStore;
import org.onosproject.pcep.api.SrpIdMapping;
import org.onosproject.pcep.api.TeLinkConfig;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.DistributedSet;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.Versioned;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import com.google.common.collect.ImmutableSet;

import static org.onosproject.incubator.net.tunnel.Tunnel.Type.MPLS;
import static org.onosproject.incubator.net.tunnel.Tunnel.Type.SDMPLS;
import static org.onosproject.incubator.net.tunnel.Tunnel.Type.MDMPLS;
import static org.onosproject.incubator.net.tunnel.Tunnel.State.INIT;
import static org.onosproject.incubator.net.tunnel.Tunnel.State.ESTABLISHED;
import static org.onosproject.incubator.net.tunnel.Tunnel.State.ACTIVE;
import static org.onosproject.incubator.net.tunnel.Tunnel.State.UNSTABLE;
import static org.onosproject.incubator.net.tunnel.Tunnel.State.INVALID;
import static org.onosproject.incubator.net.tunnel.Tunnel.State.FAILED;
import static org.onosproject.pce.pceservice.LspType.WITH_SIGNALLING;
import static org.onosproject.pce.pceservice.LspType.SR_WITHOUT_SIGNALLING;
import static org.onosproject.pce.pceservice.LspType.WITHOUT_SIGNALLING_AND_WITHOUT_SR;

import static org.onosproject.pce.pceservice.PcepAnnotationKeys.BANDWIDTH;
import static org.onosproject.pce.pceservice.PcepAnnotationKeys.LOCAL_LSP_ID;
import static org.onosproject.pce.pceservice.PcepAnnotationKeys.LSP_SIG_TYPE;
import static org.onosproject.pce.pceservice.PcepAnnotationKeys.PCE_INIT;
import static org.onosproject.pce.pceservice.PcepAnnotationKeys.PLSP_ID;
import static org.onosproject.pce.pceservice.PcepAnnotationKeys.PCC_TUNNEL_ID;
import static org.onosproject.pce.pceservice.PcepAnnotationKeys.DELEGATE;
import static org.onosproject.pce.pceservice.PcepAnnotationKeys.COST_TYPE;
import static org.onosproject.pce.pceservice.PcepAnnotationKeys.VN_NAME;
import static org.onosproject.pce.pceservice.PcepAnnotationKeys.ERROR_TYPE;
import static org.onosproject.pcep.pcepio.types.PcepErrorDetailInfo.ERROR_TYPE_24;

import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;

/**
 * Implementation of PCE service.
 */
@Component(immediate = true)
@Service
public class PceManager implements PceService {
    private static final Logger log = LoggerFactory.getLogger(PceManager.class);

    public static final long GLOBAL_LABEL_SPACE_MIN = 4097;
    public static final long GLOBAL_LABEL_SPACE_MAX = 5121;
    private static final String DEVICE_NULL = "Device-cannot be null";
    private static final String LINK_NULL = "Link-cannot be null";
    public static final String PCE_SERVICE_APP = "org.onosproject.pce";
    private static final String LOCAL_LSP_ID_GEN_TOPIC = "pcep-local-lsp-id";
    private static final String SETUP_PATH_ID_GEN_TOPIC = "pcep-setup-path-id";

    private static final int PREFIX_LENGTH = 32;

    private static final String LSRID = "lsrId";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String END_OF_SYNC_IP_PREFIX = "0.0.0.0/32";
    public static final int PCEP_PORT = 4189;

    private static final Boolean TUNNEL_INIT = false;
    private static final Boolean TUNNEL_CREATED = true;

    private LspType defaultLspType;
    public String pceMode = "PNC";
    private IdGenerator localLspIdIdGen;
    private IdGenerator setupPathIdGen;
    protected DistributedSet<Short> localLspIdFreeList;

    // LSR-id and device-id mapping for checking capability if L3 device is not having its capability
    private Map<String, DeviceId> lsrIdDeviceIdMap = new HashMap<>();

    // PCE path update listener set
    protected Set<PcePathUpdateListener> pcePathUpdateListener = new CopyOnWriteArraySet<>();

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PathService pathService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PceStore pceStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PcepSrpStore pceSrpStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TunnelService tunnelService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TunnelAdminService tunnelAdminService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService linkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkConfigService netCfgService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LabelResourceAdminService labelRsrcAdminService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LabelResourceService labelRsrcService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowObjectiveService flowObjectiveService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MastershipService mastershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkConfigRegistry netConfigRegistry;

    private TunnelListener listener = new InnerTunnelListener();
    private DeviceListener deviceListener = new InternalDeviceListener();
    private LinkListener linkListener = new InternalLinkListener();
    private InternalConfigListener cfgListener = new InternalConfigListener();
    private BasicPceccHandler crHandler;
    private PceccSrTeBeHandler srTeHandler;
    private ApplicationId appId;

    private final PcepPacketProcessor processor = new PcepPacketProcessor();
    private final TopologyListener topologyListener = new InternalTopologyListener();
    private ScheduledExecutorService executor;

    public static final int INITIAL_DELAY = 30;
    public static final int PERIODIC_DELAY = 30;

    private PceService pceService;

    private DomainManager domainManager;

    // TODO: Move this to Topology+ later
    private final ConfigFactory<LinkKey, TeLinkConfig> configFactory =
            new ConfigFactory<LinkKey, TeLinkConfig>(SubjectFactories.LINK_SUBJECT_FACTORY,
                    TeLinkConfig.class, "teLinkConfig", true) {
                @Override
                public TeLinkConfig createConfig() {
                    return new TeLinkConfig();
                }
            };
    /**
     * Creates new instance of PceManager.
     */
    public PceManager() {
    }

    /**
     * Returns domain manager.
     *
     * @return domain manager
     */
    public DomainManager domainManager() {
        return domainManager;
    }

    @Override
    public LspType defaultLspType() {
        return defaultLspType;
    }

    @Override
    public void setdefaultLspType(LspType defaultLspType) {
        this.defaultLspType = defaultLspType;
    }

    @Override
    public void setPceMode(String mode) {
        this.pceMode = mode;
    }

    @Override
    public String getPceMode() {
        return pceMode;
    }

    @Activate
    protected void activate() {
        appId = coreService.registerApplication(PCE_SERVICE_APP);
        domainManager = new DomainManagerImpl(deviceService);
        crHandler = BasicPceccHandler.getInstance();
        crHandler.initialize(labelRsrcService, flowObjectiveService, appId, pceStore);

        srTeHandler = PceccSrTeBeHandler.getInstance();
        srTeHandler.initialize(labelRsrcAdminService, labelRsrcService, flowObjectiveService, appId, pceStore,
                               deviceService);

        tunnelService.addListener(listener);
        deviceService.addListener(deviceListener);
        linkService.addListener(linkListener);
        netCfgService.addListener(cfgListener);

        localLspIdIdGen = coreService.getIdGenerator(LOCAL_LSP_ID_GEN_TOPIC);
        localLspIdIdGen.getNewId(); // To prevent 0, the 1st value generated from being used in protocol.
        setupPathIdGen = coreService.getIdGenerator(SETUP_PATH_ID_GEN_TOPIC);
        setupPathIdGen.getNewId();
        localLspIdFreeList = storageService.<Short>setBuilder()
                .withName("pcepLocalLspIdDeletedList")
                .withSerializer(Serializer.using(KryoNamespaces.API))
                .build()
                .asDistributedSet();

        packetService.addProcessor(processor, PacketProcessor.director(4));
        topologyService.addListener(topologyListener);

        // Reserve global node pool
        if (!srTeHandler.reserveGlobalPool(GLOBAL_LABEL_SPACE_MIN, GLOBAL_LABEL_SPACE_MAX)) {
            log.debug("Global node pool was already reserved.");
        }
        netConfigRegistry.registerConfigFactory(configFactory);
        pceService = this;
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        tunnelService.removeListener(listener);
        deviceService.removeListener(deviceListener);
        linkService.removeListener(linkListener);
        netCfgService.removeListener(cfgListener);
        packetService.removeProcessor(processor);
        topologyService.removeListener(topologyListener);
        netConfigRegistry.unregisterConfigFactory(configFactory);
        log.info("Stopped");
    }

    /**
     * Returns an edge-weight capable of evaluating links on the basis of the
     * specified constraints.
     *
     * @param constraints path constraints
     * @return edge-weight function
     */
    private LinkWeight weight(List<Constraint> constraints) {
        return new TeConstraintBasedLinkWeight(constraints);
    }

    // Setup the path at MDSC and PNC based on mode and split the parent path to child and setup all paths.
    private PathErr setupPath(String vnName, DeviceId src, DeviceId dst, String tunnelName,
                              List<Constraint> constraints, LspType lspType) {
        Set<Path> paths = null;

        //compute the path with given constraints
        Set<Path> computedPathSet = computePath(src, dst, constraints);
        if (computedPathSet.isEmpty()) {
            return PathErr.COMPUTATION_FAIL;
        }
        Path computedPath = computedPathSet.iterator().next();

        // In case of MDSC, split the parent tunnel to multiple child tunnels and pass on to PNC for its local handling
        if (getPceMode().equals("MDSC")) {
            //Setup path for parent tunnel.
            setupPath(vnName, src, dst, tunnelName, constraints, WITH_SIGNALLING, MDMPLS, computedPath);

            //Store parent child mapping
            Collection<Tunnel> tunnels = tunnelService.queryTunnel(TunnelName.tunnelName(tunnelName));
            Tunnel parentTunnel = tunnels.iterator().next();
            pceStore.addParentTunnel(parentTunnel.tunnelId(), parentTunnel.state());

            //Split the parent path to multiple child paths and setup the tunnel for each child paths.
            paths = domainManager().getDomainSpecificPaths(computedPath);
            if (paths.isEmpty()) {
                pceStore.removeParentTunnel(parentTunnel.tunnelId());
                return PathErr.COMPUTATION_FAIL;
            }

            paths.forEach(path -> {
                String childTunnelName = tunnelName.concat("C-" + Long.toString(setupPathIdGen.getNewId()));
                PceManager.this.setupPath(vnName, path.src().deviceId(), path.dst().deviceId(), childTunnelName,
                        constraints, WITH_SIGNALLING, SDMPLS, path);
                //Store child mapping
                Collection<Tunnel> childTunnels = tunnelService.queryTunnel(TunnelName.tunnelName(childTunnelName));
                Tunnel childTunnel = childTunnels.iterator().next();
                pceStore.addChildTunnel(parentTunnel.tunnelId(), childTunnel.tunnelId(), childTunnel.state());
            });
        } else {
            return setupPath(vnName, src, dst, tunnelName, constraints, lspType, MPLS, computedPath);
        }
        return PathErr.SUCCESS;
    }

    @Override
    public long generatePathId() {
        return setupPathIdGen.getNewId();
    }

    /**
     * Creates new path based on virtual network name, constraints and LSP type.
     *
     * @param vnName virtual network name
     * @param src source device
     * @param dst destination device
     * @param tunnelName name of the tunnel
     * @param constraints list of constraints to be applied on path
     * @param lspType type of path to be setup
     * @return false on failure and true on successful path creation
     */
    private PathErr setupPath(String vnName, DeviceId src, DeviceId dst, String tunnelName,
                              List<Constraint> constraints, LspType lspType, Tunnel.Type type, Path computedPath) {
        LspType localLspType;

        // If tunnel type is MPLS then take the user requested type else take the default domain specific lsp type
        if (type == MPLS) {
            localLspType = lspType;
        } else {
            localLspType = defaultLspType();
        }

        // Convert from DeviceId to TunnelEndPoint
        Device srcDevice = deviceService.getDevice(src);
        Device dstDevice = deviceService.getDevice(dst);

        if (srcDevice == null || dstDevice == null) {
            // Device is not known.
            if (vnName == null) {
                pceStore.addFailedPathInfo(new PcePathInfo(src, dst, tunnelName, constraints, localLspType));
            }

            return PathErr.DEVICE_LSR_NOT_EXIST;
        }

        // In future projections instead of annotations will be used to fetch LSR ID.
        String srcLsrId = srcDevice.annotations().value(LSRID);
        String dstLsrId = dstDevice.annotations().value(LSRID);

        if (srcLsrId == null || dstLsrId == null) {
            // LSR id is not known.
            if (vnName == null) {
                pceStore.addFailedPathInfo(new PcePathInfo(src, dst, tunnelName, constraints, localLspType));
            }

            return PathErr.DEVICE_LSR_NOT_EXIST;
        }

        // If tunnel Type is MPLS it has direct connectivity to the device to need to check the capability.
        if (type == MPLS) {
            // Get device config from network config, to ascertain that session with ingress is present.
            DeviceCapability cfg = netCfgService.getConfig(DeviceId.deviceId(srcLsrId), DeviceCapability.class);
            if (cfg == null) {
                log.debug("No session to ingress.");
                if (vnName == null) {
                    pceStore.addFailedPathInfo(new PcePathInfo(src, dst, tunnelName, constraints, localLspType));
                }
                return PathErr.SESSION_NOT_EXIST;
            }
        }

        TunnelEndPoint srcEndPoint = IpTunnelEndPoint.ipTunnelPoint(IpAddress.valueOf(srcLsrId));
        TunnelEndPoint dstEndPoint = IpTunnelEndPoint.ipTunnelPoint(IpAddress.valueOf(dstLsrId));

        double bwConstraintValue = 0;
        CostConstraint costConstraint = null;
        if (constraints != null) {
            if (type == MPLS) {
                constraints.add(CapabilityConstraint.of(CapabilityType.valueOf(localLspType.name())));
            }

            Iterator<Constraint> iterator = constraints.iterator();

            while (iterator.hasNext()) {
                Constraint constraint = iterator.next();
                if (constraint instanceof PceBandwidthConstraint) {
                    bwConstraintValue = ((PceBandwidthConstraint) constraint).bandwidth().bps();
                } else if (constraint instanceof CostConstraint) {
                    costConstraint = (CostConstraint) constraint;
                }
            }

            /*
             * Add cost at the end of the list of constraints. The path computation algorithm also computes
             * cumulative cost. The function which checks the limiting/capability constraints also returns
             * per link cost. This function can either return the result of limiting/capability constraint
             * validation or the value of link cost, depending upon what is the last constraint in the loop.
             */
            if (costConstraint != null) {
                constraints.remove(costConstraint);
                constraints.add(costConstraint);
            }
        } else if (type == MPLS) {
            constraints = new LinkedList<>();
            constraints.add(CapabilityConstraint.of(CapabilityType.valueOf(localLspType.name())));
        }

        Builder annotationBuilder = DefaultAnnotations.builder();
        if (bwConstraintValue != 0) {
            annotationBuilder.set(BANDWIDTH, String.valueOf(bwConstraintValue));
        }

        if (costConstraint != null) {
            annotationBuilder.set(COST_TYPE, String.valueOf(costConstraint.type()));
        }

        if (vnName != null) {
            annotationBuilder.set(VN_NAME, vnName);
        }

        annotationBuilder.set(LSP_SIG_TYPE, localLspType.name());
        annotationBuilder.set(PCE_INIT, TRUE);
        annotationBuilder.set(DELEGATE, TRUE);

        LabelStack labelStack = null;

        if (localLspType == SR_WITHOUT_SIGNALLING) {
            labelStack = srTeHandler.computeLabelStack(computedPath);
            // Failed to form a label stack.
            if (labelStack == null) {
                if (vnName == null) {
                    pceStore.addFailedPathInfo(new PcePathInfo(src, dst, tunnelName, constraints, localLspType));
                }
                return PathErr.COMPUTATION_FAIL;
            }
        }

        if (localLspType != WITH_SIGNALLING) {
                /*
                 * Local LSP id which is assigned by RSVP for RSVP signalled LSPs, will be assigned by
                 * PCE for non-RSVP signalled LSPs.
                 */
            annotationBuilder.set(LOCAL_LSP_ID, String.valueOf(getNextLocalLspId()));
        }

        // For SR-TE tunnels, call SR manager for label stack and put it inside tunnel.
        Tunnel tunnel = new DefaultTunnel(null, srcEndPoint, dstEndPoint, type, INIT, null, null,
                TunnelName.tunnelName(tunnelName), computedPath,
                labelStack, annotationBuilder.build());

        // Allocate bandwidth.
        if (bwConstraintValue != 0 && localLspType != WITH_SIGNALLING) {
            if (!reserveBandwidth(computedPath, bwConstraintValue, null)) {
                if (vnName == null) {
                    pceStore.addFailedPathInfo(new PcePathInfo(src, dst, tunnelName, constraints, localLspType));
                }
                return PathErr.BW_RESV_FAIL;
            }
        }

        TunnelId tunnelId = tunnelService.setupTunnel(appId, src, tunnel, computedPath);
        if (tunnelId == null) {
            if (vnName == null) {
                pceStore.addFailedPathInfo(new PcePathInfo(src, dst, tunnelName, constraints, localLspType));
            }
            if (bwConstraintValue != 0 && localLspType != WITH_SIGNALLING) {
                //resourceService.release(consumerId);
                computedPath.links().forEach(ln -> pceStore.releaseLocalReservedBw(LinkKey.linkKey(ln),
                        Double.parseDouble(tunnel.annotations().value(BANDWIDTH))));
            }
            return PathErr.ERROR;
        }
        return PathErr.SUCCESS;
    }

    @Override
    public Set<Path> computePath(DeviceId src, DeviceId dst, List<Constraint> constraints) {
        if (pathService == null) {
            return ImmutableSet.of();
        }

        if (!getPceMode().equals("MDSC")) {
            if (constraints == null) {
                constraints = new LinkedList<>();
                constraints.add(CapabilityConstraint.of(CapabilityType.WITH_SIGNALLING));
            } else {
                if (constraints.stream().filter(con -> con instanceof CapabilityConstraint).count() == 0) {
                    constraints.add(CapabilityConstraint.of(CapabilityType.WITH_SIGNALLING));
                }
            }
        }


        Set<Path> paths = pathService.getPaths(src, dst, weight(constraints));
        if (!paths.isEmpty()) {
            return paths;
        }
        return ImmutableSet.of();
    }

    //[TODO:] handle requests in queue
    @Override
    public PathErr setupPath(DeviceId src, DeviceId dst, String tunnelName, List<Constraint> constraints,
                             LspType lspType, String vnName) {
        checkNotNull(src);
        checkNotNull(dst);
        checkNotNull(tunnelName);

        if (lspType == null) {
            lspType = defaultLspType();
        }

        return setupPath(vnName, src, dst, tunnelName, constraints, lspType);
    }

    @Override
    public PathErr setupPath(String vnName, IpAddress srcLsrId, IpAddress dstLsrId, String tunnelName,
                             List<Constraint> constraints, LspType lspType) {
        checkNotNull(srcLsrId);
        checkNotNull(dstLsrId);
        checkNotNull(tunnelName);

        if (lspType == null) {
            lspType = defaultLspType();
        }

        // Get the devices from lsrId's
        DeviceId srcDeviceId = pceStore.getLsrIdDevice(srcLsrId.toString());
        DeviceId dstDeviceId = pceStore.getLsrIdDevice(dstLsrId.toString());

        return setupPath(vnName, srcDeviceId, dstDeviceId, tunnelName, constraints, lspType);

    }

    @Override
    public PathErr updatePath(IpAddress srcLsrId, IpAddress dstLsrId, String plspId, List<Constraint> constraints) {
        checkNotNull(plspId);
        checkNotNull(srcLsrId);
        checkNotNull(dstLsrId);

        PathErr result = null;
        TunnelEndPoint tunSrc = IpTunnelEndPoint.ipTunnelPoint(srcLsrId);
        TunnelEndPoint tunDst = IpTunnelEndPoint.ipTunnelPoint(dstLsrId);

        Collection<Tunnel> tunnels = tunnelService.queryTunnel(tunSrc, tunDst);
        Optional<Tunnel> tunnel = tunnels.stream()
                .filter(t -> t.annotations().value(PLSP_ID).equals(plspId))
                .findFirst();

        if (tunnel.isPresent()) {
            result =  updatePath(tunnel.get().tunnelId(), constraints);
        }
        return result;
    }

    @Override
    public PathErr updatePath(TunnelId tunnelId, List<Constraint> constraints) {
        checkNotNull(tunnelId);
        Set<Path> computedPathSet = null;
        Tunnel tunnel = tunnelService.queryTunnel(tunnelId);

        if (tunnel == null) {
            return PathErr.TUNNEL_NOT_FOUND;
        }

        if (((tunnel.type() != MPLS) && (tunnel.type() != SDMPLS) && (tunnel.type() != MDMPLS))
                || FALSE.equalsIgnoreCase(tunnel.annotations().value(DELEGATE))) {
            // Only delegated LSPs can be updated.
            return PathErr.TYPE_MISMATCH;
        }

        List<Link> links = tunnel.path().links();
        String lspSigType = tunnel.annotations().value(LSP_SIG_TYPE);
        double bwConstraintValue = 0;
        String costType = null;
        SharedBandwidthConstraint shBwConstraint = null;
        PceBandwidthConstraint bwConstraint = null;
        CostConstraint costConstraint = null;

        if (constraints != null) {
            // Call path computation in shared bandwidth mode.
            Iterator<Constraint> iterator = constraints.iterator();
            while (iterator.hasNext()) {
                Constraint constraint = iterator.next();
                if (constraint instanceof PceBandwidthConstraint) {
                    bwConstraint = (PceBandwidthConstraint) constraint;
                    bwConstraintValue = bwConstraint.bandwidth().bps();
                } else if (constraint instanceof CostConstraint) {
                    costConstraint = (CostConstraint) constraint;
                    costType = costConstraint.type().name();
                }
            }

            // Remove and keep the cost constraint at the end of the list of constraints.
            if (costConstraint != null) {
                constraints.remove(costConstraint);
            }

            Bandwidth existingBwValue = null;
            String existingBwAnnotation = tunnel.annotations().value(BANDWIDTH);
            if (existingBwAnnotation != null) {
                existingBwValue = Bandwidth.bps(Double.parseDouble(existingBwAnnotation));

                /*
                 * The computation is a shared bandwidth constraint based, so need to remove bandwidth constraint which
                 * has been utilized to create shared bandwidth constraint.
                 */
                if (bwConstraint != null) {
                    constraints.remove(bwConstraint);
                }
            }

            if (existingBwValue != null) {
                if (bwConstraintValue == 0) {
                    bwConstraintValue = existingBwValue.bps();
                }
                //If bandwidth constraints not specified , take existing bandwidth for shared bandwidth calculation
                shBwConstraint = bwConstraint != null ? new SharedBandwidthConstraint(links,
                        existingBwValue, bwConstraint.bandwidth()) : new SharedBandwidthConstraint(links,
                        existingBwValue, existingBwValue);

                constraints.add(shBwConstraint);
            }
        } else {
            constraints = new LinkedList<>();
        }

        // Devices are not directly accessible at MDSC so no need to add capability constraints.
        if (!getPceMode().equals("MDSC")) {
            constraints.add(CapabilityConstraint.of(CapabilityType.valueOf(lspSigType)));
        }

        if (costConstraint != null) {
            constraints.add(costConstraint);
        }

        computedPathSet = computePath(tunnel.path().src().deviceId(), tunnel.path().dst().deviceId(), constraints);

        // NO-PATH
        if (computedPathSet.isEmpty()) {
            return PathErr.COMPUTATION_FAIL;
        }

        Builder annotationBuilder = DefaultAnnotations.builder();
        annotationBuilder.set(BANDWIDTH, String.valueOf(bwConstraintValue));
        if (costType != null) {
            annotationBuilder.set(COST_TYPE, costType);
        }
        annotationBuilder.set(LSP_SIG_TYPE, lspSigType);
        annotationBuilder.set(PCE_INIT, TRUE);
        annotationBuilder.set(DELEGATE, TRUE);
        annotationBuilder.set(PLSP_ID, tunnel.annotations().value(PLSP_ID));
        annotationBuilder.set(PCC_TUNNEL_ID, tunnel.annotations().value(PCC_TUNNEL_ID));
        Path computedPath = computedPathSet.iterator().next();

        LabelStack labelStack = null;
        LspType lspType = LspType.valueOf(lspSigType);
        long localLspId = 0;
        if (lspType != WITH_SIGNALLING) {
            /*
             * Local LSP id which is assigned by RSVP for RSVP signalled LSPs, will be assigned by
             * PCE for non-RSVP signalled LSPs.
             */
            localLspId = getNextLocalLspId();
            annotationBuilder.set(LOCAL_LSP_ID, String.valueOf(localLspId));

            if (lspType == SR_WITHOUT_SIGNALLING) {
                labelStack = srTeHandler.computeLabelStack(computedPath);
                // Failed to form a label stack.
                if (labelStack == null) {
                    return PathErr.COMPUTATION_FAIL;
                }
            }
        }

        Tunnel updatedTunnel = new DefaultTunnel(null, tunnel.src(), tunnel.dst(), tunnel.type(), INIT, null, null,
                tunnel.tunnelName(), computedPath,
                labelStack, annotationBuilder.build());

        // Allocate shared bandwidth.
        if (bwConstraintValue != 0  && lspType != WITH_SIGNALLING) {
            if (!reserveBandwidth(computedPath, bwConstraintValue, shBwConstraint)) {
                return PathErr.BW_RESV_FAIL;
            }
        }

        TunnelId updatedTunnelId = tunnelService.setupTunnel(appId, links.get(0).src().deviceId(), updatedTunnel,
                computedPath);

        if (updatedTunnelId == null && lspType != WITH_SIGNALLING) {
            if (bwConstraintValue != 0) {
                //resourceService.release(consumerId);
                releaseSharedBandwidth(updatedTunnel, tunnel);
            }
            return PathErr.ERROR;
        }

        // For CR cases, download labels and send update message.
        if (lspType == WITHOUT_SIGNALLING_AND_WITHOUT_SR) {
            Tunnel tunnelForlabelDownload = new DefaultTunnel(null, tunnel.src(), tunnel.dst(), MPLS, INIT, null,
                    updatedTunnelId, tunnel.tunnelName(), computedPath,
                    labelStack, annotationBuilder.build());

            if (!crHandler.allocateLabel(tunnelForlabelDownload)) {
                log.error("Unable to allocate labels for the tunnel {}.", tunnel.toString());
            }
        }

        if (tunnel.type() == MDMPLS) {
            pceStore.addParentTunnel(updatedTunnelId, INIT);
            boolean ret = updateParentTunnelProcess(tunnel, updatedTunnelId, computedPath, constraints);

        } else {

            return PathErr.SUCCESS;
        }

        return PathErr.SUCCESS;
    }

    private boolean updateParentTunnelProcess(Tunnel oldParentTunnel, TunnelId newParentTunnelId, Path computedPath,
                                              List<Constraint> constraints) {

        //Call domain manager with old path
        Set<Path> oldPaths = domainManager().getDomainSpecificPaths(oldParentTunnel.path());
        Set<Path> newPaths = domainManager().getDomainSpecificPaths(computedPath);
        Map<DomainManager.Oper, Set<Path>> setPath = domainManager().compareDomainSpecificPaths(oldPaths, newPaths);

        Set<TunnelId> childTunnelIds = pceStore.childTunnel(oldParentTunnel.tunnelId()).keySet();

        for (Map.Entry<DomainManager.Oper, Set<Path>> entry : setPath.entrySet()) {
            Set<Path> path = entry.getValue();
            Iterator iterator = path.iterator();
            if (entry.getKey().equals(DomainManager.Oper.ADD)) {
                while (iterator.hasNext()) {
                    Path setupPath = (Path) iterator.next();
                    String vnName = oldParentTunnel.annotations().value("vnName");
                    String childTunnelName = vnName.concat(Long.toString(generatePathId()));
                    setupPath(vnName, setupPath.src().deviceId(),
                            setupPath.dst().deviceId(), childTunnelName,
                            constraints, LspType.WITH_SIGNALLING, SDMPLS, setupPath);
                    //Store child mapping
                    Collection<Tunnel> childTunnels = tunnelService.queryTunnel(TunnelName.tunnelName(childTunnelName));
                    Tunnel childTunnel = childTunnels.iterator().next();
                    pceStore.addChildTunnel(newParentTunnelId, childTunnel.tunnelId(), childTunnel.state());
                }
            } else if (entry.getKey().equals(DomainManager.Oper.UPDATE)) {
                while (iterator.hasNext()) {
                    Path updatePath = (Path) iterator.next();
                    TunnelId updateTunnel = getTunnel(updatePath, childTunnelIds);
                    if (updateTunnel != null) {
                        updatePath(updateTunnel, constraints);
                        //pceStore.removeChildTunnel(oldParentTunnel.tunnelId(), updateTunnel);
                        pceStore.updateTunnelStatus(updateTunnel, Tunnel.State.INACTIVE);
                        Tunnel tempTunnel = tunnelService.queryTunnel(updateTunnel);
                        Collection<Tunnel> childTunnels = tunnelService.queryTunnel(tempTunnel.tunnelName());
                        childTunnels.forEach(t -> {
                            if (updateTunnel != t.tunnelId()) {
                                pceStore.addChildTunnel(newParentTunnelId, t.tunnelId(), t.state());
                            }
                        });
                    }
                }
            } else if (entry.getKey().equals(DomainManager.Oper.DELETE)) {
                while (iterator.hasNext()) {
                    Path deletePath = (Path) iterator.next();
                    TunnelId deleteTunnel = getTunnel(deletePath, childTunnelIds);
                    if (deleteTunnel != null) {
                        releasePath(deleteTunnel);
                        pceStore.updateTunnelStatus(deleteTunnel, Tunnel.State.INACTIVE);
                        //pceStore.removeChildTunnel(oldParentTunnel.tunnelId(), deleteTunnel);
                    }
                }
            }
        }
        return true;
    }


    public TunnelId getTunnel(Path updatepPath, Set<TunnelId> childTunnelIds) {
        //Path updatepPath = (Path) iterator.next();
        DeviceId src = updatepPath.links().get(0).src().deviceId();
        DeviceId dst = updatepPath.links().get(updatepPath.links().size() - 1).dst().deviceId();

        Iterator childTunnelItr = childTunnelIds.iterator();
        while (childTunnelItr.hasNext()) {
            TunnelId childTunnelId = (TunnelId) childTunnelItr.next();
            Tunnel tmpTunnel = tunnelService.queryTunnel(childTunnelId);
            DeviceId tunnelSrc = tmpTunnel.path().links().get(0).src().deviceId();
            DeviceId tunnelDst = tmpTunnel.path().links().get(tmpTunnel.path().links().size() - 1).dst().deviceId();

            if (tunnelSrc.equals(src) && tunnelDst.equals(dst)) {
               return  childTunnelId;
            }
        }
        return null;
    }

    @Override
    public boolean releasePath(TunnelId tunnelId) {
        checkNotNull(tunnelId);
        // 1. Query Tunnel from Tunnel manager.
        Tunnel tunnel = tunnelService.queryTunnel(tunnelId);

        if (tunnel == null) {
            return false;
        }

        if (tunnel.type() == MDMPLS) {
            tunnelService.downTunnel(appId, tunnel.tunnelId());
            pceStore.updateTunnelStatus(tunnel.tunnelId(), Tunnel.State.INACTIVE);
            Set<TunnelId> childTunnelIds = pceStore.childTunnel(tunnel.tunnelId()).keySet();
            childTunnelIds.forEach(t -> {
                tunnelService.downTunnel(appId, t);
                pceStore.updateTunnelStatus(t, Tunnel.State.INACTIVE);
            });
            return true;
        }
        // 2. Call tunnel service.
        return tunnelService.downTunnel(appId, tunnel.tunnelId());
    }

    @Override
    public PathErr releasePath(IpAddress srcLsrId, IpAddress dstLsrId, String plspId) {
        checkNotNull(plspId);
        checkNotNull(srcLsrId);
        checkNotNull(dstLsrId);

        boolean result = false;
        TunnelEndPoint tunSrc = IpTunnelEndPoint.ipTunnelPoint(srcLsrId);
        TunnelEndPoint tunDst = IpTunnelEndPoint.ipTunnelPoint(dstLsrId);

        Collection<Tunnel> tunnels = tunnelService.queryTunnel(tunSrc, tunDst);
        Optional<Tunnel> tunnel = tunnels.stream()
                .filter(t -> t.annotations().value(PLSP_ID).equals(plspId))
                .findFirst();

        if (tunnel.isPresent()) {
            result = releasePath(tunnel.get().tunnelId());
        }

       if (!result) {
           return PathErr.ERROR;
        }
        return PathErr.SUCCESS;
    }

    @Override
    public Iterable<Tunnel> queryAllPath() {
        return tunnelService.queryTunnel(MPLS);
    }

    @Override
    public Tunnel queryPath(TunnelId tunnelId) {
        return tunnelService.queryTunnel(tunnelId);
    }

    @Override
    public Collection<Tunnel> queryPath(TunnelEndPoint src, TunnelEndPoint dst) {
        return tunnelService.queryTunnel(src, dst);
    }

    @Override
    public Iterable<Tunnel> queryPath(String vnName) {
        // TODO: query tunnels by vn name
        return tunnelService.queryTunnel(MDMPLS);
    }

    @Override
    public void addListener(PcePathUpdateListener listener) {
        this.pcePathUpdateListener.add(listener);
    }

    @Override
    public void removeListener(PcePathUpdateListener listener) {
        this.pcePathUpdateListener.remove(listener);
    }

    @Override
    public boolean pceBandwidthAvailable(Link link, Double bandwidth) {
        Versioned<Double> localAllocBw = pceStore.getAllocatedLocalReservedBw(LinkKey.linkKey(link));
        Set<Double> unResvBw = pceStore.getUnreservedBw(LinkKey.linkKey(link));
        Double prirZeroBw = unResvBw.iterator().next();
        return (bandwidth >= prirZeroBw - localAllocBw.value());
    }

    @Override
    public Boolean queryParentTunnelStatus(TunnelId tunnelId) {
        return pceStore.isAllChildUp(tunnelId);
    }

    @Override
    public List<PcePathReport> queryAllInitiateTunnelsByMdsc() {

        List<PcePathReport> pcePathList = new LinkedList<PcePathReport>();
        Collection<Tunnel> tunnels = tunnelService.queryTunnel(MPLS);
        PcePathReport.State state = null;
        for (Tunnel tunnel : tunnels) {
            if (tunnel.annotations().value(VN_NAME) != null) {
                if ((tunnel.state() == ESTABLISHED) || (tunnel.state() == ACTIVE)) {
                    state = PcePathReport.State.UP;
                } else {
                    state = PcePathReport.State.DOWN;
                }

                PcePathReport pcePath = DefaultPcePathReport.builder()
                        .pathName(tunnel.tunnelName().toString())
                        .plspId(tunnel.annotations().value(PcepAnnotationKeys.PLSP_ID))
                        .localLspId(tunnel.annotations().value(PcepAnnotationKeys.LOCAL_LSP_ID))
                        .pceTunnelId(tunnel.annotations().value(PcepAnnotationKeys.PCC_TUNNEL_ID))
                        .isDelegate(Boolean.parseBoolean(tunnel.annotations().value(DELEGATE)))
                        .state(state)
                        .ingress(((IpTunnelEndPoint) tunnel.src()).ip())
                        .egress(((IpTunnelEndPoint) tunnel.dst()).ip())
                        .eroPath(tunnel.path())
                        .rroPath(tunnel.path())
                        .build();

                pcePathList.add(pcePath);
            }
        }
        return pcePathList;

    }

    /**
     * Send Tunnel update report to all listeners.
     *
     * @param tunnel tunnel information
     * @param success whether to send success report or not
     * @param remove whether to send remove tunnel or not
     * @return none
     */
    private void reportTunnelToListeners(Tunnel tunnel, boolean success, boolean remove, int srpId) {
        PcePathReport.State state;

        if (tunnel.annotations().value(VN_NAME) != null) {
            if (success) {
                state = PcePathReport.State.UP;
            } else {
                state = PcePathReport.State.DOWN;
            }

            PcePathReport report = DefaultPcePathReport.builder()
                    .pathName(tunnel.tunnelName().toString())
                    .srpId(String.valueOf(srpId))
                    .plspId(tunnel.annotations().value(PcepAnnotationKeys.PLSP_ID))
                    .localLspId(tunnel.annotations().value(PcepAnnotationKeys.LOCAL_LSP_ID))
                    .pceTunnelId(tunnel.annotations().value(PcepAnnotationKeys.PCC_TUNNEL_ID))
                    .isDelegate(Boolean.parseBoolean(tunnel.annotations().value(DELEGATE)))
                    .state(state)
                    .isRemoved(remove)
                    .ingress(((IpTunnelEndPoint) tunnel.src()).ip())
                    .egress(((IpTunnelEndPoint) tunnel.dst()).ip())
                    .eroPath(tunnel.path())
                    .rroPath(tunnel.path())
                    .build();

            pcePathUpdateListener.forEach(item -> item.updatePath(report));
        }
    }

    private int getMdscSrpId(String pathName) {
        SrpIdMapping srpIdMapping = pceSrpStore.getSrpIdMapping(pathName);
        if (srpIdMapping == null) {
            return 0;
        }

        if (srpIdMapping.pncSrpId() == srpIdMapping.rptSrpId()) {
            int mdscSrpId = srpIdMapping.mdscSrpId();
            pceSrpStore.removeSrpIdMapping(pathName);
            return mdscSrpId;
        }

        return 0;
    }

    /**
     * Returns the next local LSP identifier to be used either by getting from
     * freed list if available otherwise generating a new one.
     *
     * @return value of local LSP identifier
     */
    private short getNextLocalLspId() {
        // If there is any free id use it. Otherwise generate new id.
        if (localLspIdFreeList.isEmpty()) {
            return (short) localLspIdIdGen.getNewId();
        }
        Iterator<Short> it = localLspIdFreeList.iterator();
        Short value = it.next();
        localLspIdFreeList.remove(value);
        return value;
    }

    protected class TeConstraintBasedLinkWeight implements LinkWeight {

        private final List<Constraint> constraints;

        /**
         * Creates a new edge-weight function capable of evaluating links
         * on the basis of the specified constraints.
         *
         * @param constraints path constraints
         */
        public TeConstraintBasedLinkWeight(List<Constraint> constraints) {
            if (constraints == null) {
                this.constraints = Collections.emptyList();
            } else {
                this.constraints = ImmutableList.copyOf(constraints);
            }
        }

        @Override
        public double weight(TopologyEdge edge) {
            if (!constraints.iterator().hasNext()) {
                //Takes default cost/hopcount as 1 if no constraints specified
                return 1.0;
            }

            Iterator<Constraint> it = constraints.iterator();
            double cost = 1;

            //If any constraint fails return -1 also value of cost returned from cost constraint can't be negative
            while (it.hasNext() && cost > 0) {
                Constraint constraint = it.next();
                if (constraint instanceof CapabilityConstraint) {
                    cost = ((CapabilityConstraint) constraint).isValidLink(edge.link(), deviceService,
                                                                           netCfgService) ? 1 : -1;
                } else if (constraint instanceof ExcludeDeviceConstraint) {
                    cost = ((ExcludeDeviceConstraint) constraint).isValidLink(edge.link(), deviceService) ? 1 : -1;

                } else if (constraint instanceof PceBandwidthConstraint) {
                    cost = ((PceBandwidthConstraint) constraint).isValidLink(edge.link(), pceService) ? 1 : -1;

                } else if (constraint instanceof SharedBandwidthConstraint) {
                    cost = ((SharedBandwidthConstraint) constraint).isValidLink(edge.link(), pceService) ? 1 : -1;
                } else {
                    cost = constraint.cost(edge.link(), null);
                }
            }
            return cost;
        }
    }

    //TODO: annotations used for temporarily later projection/network config will be used
    private class InternalTopologyListener implements TopologyListener {
       @Override
        public void event(TopologyEvent event) {
             event.reasons().forEach(e -> {
                //If event type is link removed, get the impacted tunnel
                //Recompute on topology event only for MPLS tunnels. Parent and child tunnels will react to report
                if (e instanceof LinkEvent) {
                    LinkEvent linkEvent = (LinkEvent) e;
                    if (linkEvent.type() == LinkEvent.Type.LINK_REMOVED) {
                        tunnelService.queryTunnel(MPLS).forEach(t -> {
                                if (t.path().links().contains((e.subject()))) {
                                    // Check whether this ONOS instance is master for ingress device if yes,
                                    // recompute and send update
                                    checkForMasterAndUpdateTunnel(t.path().src().deviceId(), t);
                                }
                        });
                    }
                }
             });
        }
    }

    private boolean checkForMasterAndUpdateTunnel(DeviceId src, Tunnel tunnel) {
        /**
         * Master of ingress node will recompute and also delegation flag must be set.
         */
        if (mastershipService.isLocalMaster(src)
                && Boolean.valueOf(tunnel.annotations().value(DELEGATE)) != null) {

            updateFailedPath(tunnel);
            return true;
        }

        return false;
    }

    private void updateFailedPath(Tunnel tunnel) {
        LinkedList<Constraint> constraintList = new LinkedList<>();

        if (tunnel.annotations().value(BANDWIDTH) != null) {
            //Requested bandwidth will be same as previous allocated bandwidth for the tunnel
            PceBandwidthConstraint localConst = new PceBandwidthConstraint(Bandwidth.bps(Double.parseDouble(tunnel
                    .annotations().value(BANDWIDTH))));
            constraintList.add(localConst);
        }
        if (tunnel.annotations().value(COST_TYPE) != null) {
            constraintList.add(CostConstraint.of(CostConstraint.Type.valueOf(tunnel.annotations().value(
                    COST_TYPE))));
        }

        /*
         * If tunnel was UP after recomputation failed then store failed path in PCE store send PCIntiate(remove)
         * and If tunnel is failed and computation fails nothing to do because tunnel status will be same[Failed]
         */
        if (PathErr.SUCCESS != updatePath(tunnel.tunnelId(), constraintList)
                && !tunnel.state().equals(Tunnel.State.FAILED)) {

            if (tunnel.annotations().value(VN_NAME) != null && tunnel.type() == MPLS) {
                reportTunnelToListeners(tunnel, false, false, 0);
                // send admin down over protocol.
                tunnelAdminService.updateTunnel(tunnel, tunnel.path(), FAILED);
            } else {
                // If updation fails store in PCE store as failed path
                // then PCInitiate (Remove)
                pceStore.addFailedPathInfo(new PcePathInfo(tunnel.path().src().deviceId(), tunnel
                        .path().dst().deviceId(), tunnel.tunnelName().value(), constraintList,
                        LspType.valueOf(tunnel.annotations().value(LSP_SIG_TYPE))));

                //Release that tunnel calling PCInitiate
                releasePath(tunnel.tunnelId());
            }
        }
    }

     // Allocates the bandwidth locally for PCECC tunnels.
    private boolean reserveBandwidth(Path computedPath, double bandwidthConstraint,
                                  SharedBandwidthConstraint shBwConstraint) {
        checkNotNull(computedPath);
        checkNotNull(bandwidthConstraint);
        double bwToAllocate;
        Map<Link, Double> linkMap = new HashMap<>();

        /**
         * Shared bandwidth sub-case : Lesser bandwidth required than original -
         * No reservation required.
         */
        Double additionalBwValue = null;
        if (shBwConstraint != null) {
            additionalBwValue = ((bandwidthConstraint - shBwConstraint.sharedBwValue().bps()) <= 0) ? null
                : (bandwidthConstraint - shBwConstraint.sharedBwValue().bps());
        }

        for (Link link : computedPath.links()) {
            bwToAllocate = 0;
            if ((shBwConstraint != null) && (shBwConstraint.links().contains(link))) {
                if (additionalBwValue != null) {
                    bwToAllocate = bandwidthConstraint - additionalBwValue;
                }
            } else {
                bwToAllocate = bandwidthConstraint;
            }

            /**
             *  In shared bandwidth cases, where new BW is lesser than old BW, it
             *  is not required to allocate anything.
             */
            if (bwToAllocate != 0) {

                if (!pceStore.allocLocalReservedBw(LinkKey.linkKey(link), bwToAllocate)) {
                    // If allocation for any link fails, then release the partially allocated bandwidth.
                    linkMap.forEach((ln, aDouble) -> pceStore.releaseLocalReservedBw(LinkKey.linkKey(ln), aDouble));
                    return false;
                }

                linkMap.put(link, bwToAllocate);
            }
        }

        /*
         * Note: Storing of tunnel consumer id is done by caller of bandwidth reservation function. So deleting tunnel
         * consumer id should be done by caller of bandwidth releasing function. This will prevent ambiguities related
         * to who is supposed to store/delete.
         */
        return true;
    }

    /*
     * Deallocates the bandwidth which is reserved locally for PCECC tunnels.
     */
    private void releaseBandwidth(Tunnel tunnel) {
        // Between same source and destination, search the tunnel with same symbolic path name.
        Collection<Tunnel> tunnelQueryResult = tunnelService.queryTunnel(tunnel.src(), tunnel.dst());
        Tunnel newTunnel = null;
        for (Tunnel tunnelObj : tunnelQueryResult) {
            if (tunnel.tunnelName().value().equals(tunnelObj.tunnelName().value())) {
                newTunnel = tunnelObj;
                break;
            }
        }

        // Even if one link is shared, the bandwidth release should happen based on shared mechanism.
        boolean isLinkShared = false;
        if (newTunnel != null) {
            for (Link link : tunnel.path().links()) {
                if (newTunnel.path().links().contains(link)) {
                    isLinkShared = true;
                    break;
                }
            }
        }

        if (isLinkShared) {
            releaseSharedBandwidth(newTunnel, tunnel);
            return;
        }

        tunnel.path().links().forEach(tn -> pceStore.releaseLocalReservedBw(LinkKey.linkKey(tn),
                Double.parseDouble(tunnel.annotations().value(BANDWIDTH))));
    }

    /**
     *  Re-allocates the bandwidth for the tunnel for which the bandwidth was
     *  allocated in shared mode initially.
     */
    private synchronized void releaseSharedBandwidth(Tunnel newTunnel, Tunnel oldTunnel) {

        boolean isAllocate = false;
        Double oldTunnelBw = Double.parseDouble(oldTunnel.annotations().value(BANDWIDTH));
        Double newTunnelBw = Double.parseDouble(newTunnel.annotations().value(BANDWIDTH));

        if (newTunnelBw > oldTunnelBw) {
            isAllocate = true;
        }

        for (Link link : newTunnel.path().links()) {
            if (oldTunnel.path().links().contains(link)) {
                if (isAllocate) {
                    pceStore.allocLocalReservedBw(LinkKey.linkKey(link), newTunnelBw - oldTunnelBw);
                } else {
                    pceStore.releaseLocalReservedBw(LinkKey.linkKey(link), newTunnelBw - oldTunnelBw);
                }

            } else {
                pceStore.releaseLocalReservedBw(LinkKey.linkKey(link), oldTunnelBw);
            }
        }
    }

    /**
     * Allocates node label to specific device.
     *
     * @param specificDevice device to which node label needs to be allocated
     */
    public void allocateNodeLabel(Device specificDevice) {
        checkNotNull(specificDevice, DEVICE_NULL);

        DeviceId deviceId = specificDevice.id();

        // Retrieve lsrId of a specific device
        if (specificDevice.annotations() == null) {
            log.debug("Device {} does not have annotations.", specificDevice.toString());
            return;
        }

        String lsrId = specificDevice.annotations().value(LSRID);
        if (lsrId == null) {
            log.debug("Unable to retrieve lsr-id of a device {}.", specificDevice.toString());
            return;
        }

        // Get capability config from netconfig
        DeviceCapability cfg = netCfgService.getConfig(DeviceId.deviceId(lsrId), DeviceCapability.class);
        if (cfg == null) {
            log.error("Unable to find corresponding capability for a lsrd {} from NetConfig.", lsrId);
            // Save info. When PCEP session is comes up then allocate node-label
            lsrIdDeviceIdMap.put(lsrId, specificDevice.id());
            return;
        }

        // Check whether device has SR-TE Capability
        if (cfg.labelStackCap()) {
            srTeHandler.allocateNodeLabel(deviceId, lsrId);
        }
    }

    /**
     * Releases node label of a specific device.
     *
     * @param specificDevice this device label and lsr-id information will be
     *            released in other existing devices
     */
    public void releaseNodeLabel(Device specificDevice) {
        checkNotNull(specificDevice, DEVICE_NULL);

        DeviceId deviceId = specificDevice.id();

        // Retrieve lsrId of a specific device
        if (specificDevice.annotations() == null) {
            log.debug("Device {} does not have annotations.", specificDevice.toString());
            return;
        }

        String lsrId = specificDevice.annotations().value(LSRID);
        if (lsrId == null) {
            log.debug("Unable to retrieve lsr-id of a device {}.", specificDevice.toString());
            return;
        }

        // Get capability config from netconfig
        DeviceCapability cfg = netCfgService.getConfig(DeviceId.deviceId(lsrId), DeviceCapability.class);
        if (cfg == null) {
            log.error("Unable to find corresponding capabilty for a lsrd {} from NetConfig.", lsrId);
            return;
        }

        // Check whether device has SR-TE Capability
        if (cfg.labelStackCap()) {
            if (!srTeHandler.releaseNodeLabel(deviceId, lsrId)) {
                log.error("Unable to release node label for a device id {}.", deviceId.toString());
            }
        }
    }

    /**
     * Allocates adjacency label for a link.
     *
     * @param link link
     */
    public void allocateAdjacencyLabel(Link link) {
        checkNotNull(link, LINK_NULL);

        Device specificDevice = deviceService.getDevice(link.src().deviceId());
        DeviceId deviceId = specificDevice.id();

        // Retrieve lsrId of a specific device
        if (specificDevice.annotations() == null) {
            log.debug("Device {} does not have annotations.", specificDevice.toString());
            return;
        }

        String lsrId = specificDevice.annotations().value(LSRID);
        if (lsrId == null) {
            log.debug("Unable to retrieve lsr-id of a device {}.", specificDevice.toString());
            return;
        }

        // Get capability config from netconfig
        DeviceCapability cfg = netCfgService.getConfig(DeviceId.deviceId(lsrId), DeviceCapability.class);
        if (cfg == null) {
            log.error("Unable to find corresponding capabilty for a lsrd {} from NetConfig.", lsrId);
            // Save info. When PCEP session comes up then allocate adjacency
            // label
            if (lsrIdDeviceIdMap.get(lsrId) != null) {
                lsrIdDeviceIdMap.put(lsrId, specificDevice.id());
            }
            return;
        }

        // Check whether device has SR-TE Capability
        if (cfg.labelStackCap()) {
            srTeHandler.allocateAdjacencyLabel(link);
        }

        return;
    }

    /**
     * Releases allocated adjacency label of a link.
     *
     * @param link link
     */
    public void releaseAdjacencyLabel(Link link) {
        checkNotNull(link, LINK_NULL);

        Device specificDevice = deviceService.getDevice(link.src().deviceId());
        DeviceId deviceId = specificDevice.id();

        // Retrieve lsrId of a specific device
        if (specificDevice.annotations() == null) {
            log.debug("Device {} does not have annotations.", specificDevice.toString());
            return;
        }

        String lsrId = specificDevice.annotations().value(LSRID);
        if (lsrId == null) {
            log.debug("Unable to retrieve lsr-id of a device {}.", specificDevice.toString());
            return;
        }

        // Get capability config from netconfig
        DeviceCapability cfg = netCfgService.getConfig(DeviceId.deviceId(lsrId), DeviceCapability.class);
        if (cfg == null) {
            log.error("Unable to find corresponding capabilty for a lsrd {} from NetConfig.", lsrId);
            return;
        }

        // Check whether device has SR-TE Capability
        if (cfg.labelStackCap()) {
            if (!srTeHandler.releaseAdjacencyLabel(link)) {
                log.error("Unable to release adjacency labels for a link {}.", link.toString());
                return;
            }
        }

        return;
    }

    /*
     * Handle device events.
     */
    private class InternalDeviceListener implements DeviceListener {
        @Override
        public void event(DeviceEvent event) {
            Device specificDevice = event.subject();
            if (specificDevice == null) {
                log.error("Unable to find device from device event.");
                return;
            }

            switch (event.type()) {

            case DEVICE_ADDED:
                // Node-label allocation is being done during Label DB Sync.
                // So, when device is detected, no need to do node-label
                // allocation.
                if (specificDevice.annotations().value(LSRID) != null) {
                    pceStore.addLsrIdDevice(specificDevice.annotations().value(LSRID), specificDevice.id());
                }
                break;

            case DEVICE_REMOVED:
                // Release node-label
                if (mastershipService.getLocalRole(specificDevice.id()) == MastershipRole.MASTER) {
                    releaseNodeLabel(specificDevice);
                }

                if (specificDevice.annotations().value(LSRID) != null) {
                    pceStore.removeLsrIdDevice(specificDevice.annotations().value(LSRID));
                }
                break;
            default:
                break;
            }
        }
    }

    /*
     * Handle link events.
     */
    private class InternalLinkListener implements LinkListener {
        @Override
        public void event(LinkEvent event) {
            Link link = event.subject();
            switch (event.type()) {

            case LINK_ADDED:
                // Allocate adjacency label
                if (mastershipService.getLocalRole(link.src().deviceId()) == MastershipRole.MASTER) {
                    allocateAdjacencyLabel(link);
                }
                break;

            case LINK_REMOVED:
                // Release adjacency label
                if (mastershipService.getLocalRole(link.src().deviceId()) == MastershipRole.MASTER) {
                    releaseAdjacencyLabel(link);
                }
                break;
            default:
                break;
            }
        }
    }

    // Listens on tunnel events.
    private class InnerTunnelListener implements TunnelListener {
        @Override
        public void event(TunnelEvent event) {
            // Event gets generated with old tunnel object.
            Tunnel tunnel = event.subject();
            if (tunnel.type() != MPLS && tunnel.type() != MDMPLS && tunnel.type() != SDMPLS) {
                return;
            }

            LspType lspType = LspType.valueOf(tunnel.annotations().value(LSP_SIG_TYPE));
            String tunnelBandwidth = tunnel.annotations().value(BANDWIDTH);
            double bwConstraintValue = 0;
            if (tunnelBandwidth != null) {
                bwConstraintValue = Double.parseDouble(tunnelBandwidth);
            }

            switch (event.type()) {
            case TUNNEL_ADDED:
                if (tunnel.state() == ACTIVE) {
                    int srpId = getMdscSrpId(tunnel.tunnelName().value());
                    reportTunnelToListeners(tunnel, true, false, srpId);
                    // This means that PNC gone through MBB and come up with new tunnel.
                    if (tunnel.type() == SDMPLS) {
                        Collection<Tunnel> tempTunnel = tunnelService.queryTunnel(tunnel.tunnelName());
                        if (tempTunnel.isEmpty()) {
                            return;
                        }
                        TunnelId cTunnelId = tempTunnel.iterator().next().tunnelId();
                        pceStore.addChildTunnel(pceStore.parentTunnel(cTunnelId), cTunnelId, tunnel.state());
                    }
                }
                break;

            case TUNNEL_UPDATED:
                // Allocate/send labels for basic PCECC tunnels.
                if ((tunnel.state() == ESTABLISHED) && (lspType == WITHOUT_SIGNALLING_AND_WITHOUT_SR)
                        && (mastershipService.getLocalRole(tunnel.path().src().deviceId()) == MastershipRole.MASTER)) {
                    if (!crHandler.allocateLabel(tunnel)) {
                        log.error("Unable to allocate labels for a tunnel {}.", tunnel.toString());
                    }
                }

                if (tunnel.state() == UNSTABLE) {
                    /*
                     * During LSP DB sync if PCC doesn't report LSP which was PCE initiated, it's state is turned into
                     * unstable so that it can be setup again. Add into failed path store so that it can be recomputed
                     * and setup while global reoptimization.
                     */
                    List<Constraint> constraints = new LinkedList<>();
                    String bandwidth = tunnel.annotations().value(BANDWIDTH);
                    if (bandwidth != null) {
                        constraints.add(new PceBandwidthConstraint(Bandwidth
                                .bps(Double.parseDouble(bandwidth))));
                    }

                    String costType = tunnel.annotations().value(COST_TYPE);
                    if (costType != null) {
                        CostConstraint costConstraint = new CostConstraint(CostConstraint.Type.valueOf(costType));
                        constraints.add(costConstraint);
                    }

                    constraints.add(CapabilityConstraint
                            .of(CapabilityType.valueOf(tunnel.annotations().value(LSP_SIG_TYPE))));

                    List<Link> links = tunnel.path().links();
                    pceStore.addFailedPathInfo(new PcePathInfo(links.get(0).src().deviceId(),
                                                                  links.get(links.size() - 1).dst().deviceId(),
                                                                  tunnel.tunnelName().value(), constraints, lspType));
                } else if (tunnel.state() == ACTIVE) {
                    int srpId = getMdscSrpId(tunnel.tunnelName().value());
                    reportTunnelToListeners(tunnel, true, false, srpId);
                    pceStore.updateTunnelStatus(tunnel.tunnelId(), tunnel.state());
                } else if (tunnel.state() == INVALID) {
                    // Send protocol error message to the caller.
                    pcePathUpdateListener.forEach(item -> item.reportError());
                    tunnelService.downTunnel(appId, tunnel.tunnelId());
                } else if (tunnel.state() == FAILED && tunnel.type() == SDMPLS) {
                    // Trigger MBB at MDLS - Find parent tunnel.
                    TunnelId parentTunnelId = pceStore.parentTunnel(tunnel.tunnelId());
                    Tunnel parentTunnel = queryPath(parentTunnelId);

                    String errType = tunnel.annotations().value(ERROR_TYPE);
                    if (errType != null && Integer.valueOf(errType) == ERROR_TYPE_24) {

                        LinkedList<Constraint> constraintList = new LinkedList<>();

                        if (tunnel.annotations().value(BANDWIDTH) != null) {
                            //Requested bandwidth will be same as previous allocated bandwidth for the tunnel
                            PceBandwidthConstraint localConst = new PceBandwidthConstraint(Bandwidth
                                    .bps(Double.parseDouble(tunnel.annotations().value(BANDWIDTH))));
                            constraintList.add(localConst);
                        }
                        if (tunnel.annotations().value(COST_TYPE) != null) {
                            constraintList.add(CostConstraint.of(CostConstraint.Type.valueOf(tunnel.annotations()
                                    .value(COST_TYPE))));
                        }

                        List<Device> excludeDeviceList = new ArrayList<>();
                        String srcBorderLsrId = ((IpTunnelEndPoint) parentTunnel.src()).ip().toString();
                        DeviceId srcBorderDevId = pceStore.getLsrIdDevice(srcBorderLsrId);
                        if (srcBorderDevId == null) {
                            log.error("Ingress border device id not found! {}", srcBorderLsrId);
                        }

                        Device srcBorderDev = deviceService.getDevice(srcBorderDevId);
                        if (srcBorderDev == null) {
                            log.error("Ingress border device not found! {}", srcBorderLsrId);
                        }
                        excludeDeviceList.add(srcBorderDev);

                        ExcludeDeviceConstraint srcBdrExcludeConstraint = ExcludeDeviceConstraint.of(excludeDeviceList);
                        constraintList.add(srcBdrExcludeConstraint);

                        if (updatePath(parentTunnelId, constraintList) == PathErr.COMPUTATION_FAIL) {
                            constraintList.remove(srcBdrExcludeConstraint);
                            excludeDeviceList = new ArrayList<>();
                            String dstBorderLsrId = ((IpTunnelEndPoint) parentTunnel.dst()).ip().toString();
                            DeviceId dstBorderDevId = pceStore.getLsrIdDevice(dstBorderLsrId);
                            if (dstBorderDevId == null) {
                                log.error("Egress border device id not found! {}", dstBorderLsrId);
                            }

                            Device dstBorderDev = deviceService.getDevice(dstBorderDevId);
                            if (dstBorderDev == null) {
                                log.error("Egress border device not found! {}", dstBorderLsrId);
                            }
                            excludeDeviceList.add(dstBorderDev);

                            ExcludeDeviceConstraint dstBdrExcConstraint = ExcludeDeviceConstraint.of(excludeDeviceList);
                            constraintList.add(dstBdrExcConstraint);

                            updatePath(parentTunnelId, constraintList);
                        }
                        break;
                    }
                    // For parent tunnel, MDSC will not be the master for source device.
                    updateFailedPath(parentTunnel);
                }

                break;

            case TUNNEL_REMOVED:
                if (lspType != WITH_SIGNALLING) {
                    localLspIdFreeList.add(Short.valueOf(tunnel.annotations().value(LOCAL_LSP_ID)));
                }

                // If not zero bandwidth, and delegated (initiated LSPs will also be delegated).
                if (bwConstraintValue != 0) {
                    if (lspType != WITH_SIGNALLING) {
                        releaseBandwidth(event.subject());
                    }
                }

                // Release basic PCECC labels.
                if (lspType == WITHOUT_SIGNALLING_AND_WITHOUT_SR) {
                    if (mastershipService.getLocalRole(tunnel.path().src().deviceId()) == MastershipRole.MASTER) {
                            crHandler.releaseLabel(tunnel);
                    }
                } else {
                    pceStore.removeTunnelInfo(tunnel.tunnelId());
                }

                int srpId = getMdscSrpId(tunnel.tunnelName().value());
                reportTunnelToListeners(tunnel, true, true, srpId);
                if (tunnel.type() == SDMPLS) {
                    TunnelId pTunnelId = pceStore.parentTunnel(tunnel.tunnelId());
                    pceStore.removeChildTunnel(pTunnelId, tunnel.tunnelId());
                    if (pceStore.childTunnel(pTunnelId).size() == 1) {
                        tunnelAdminService.removeTunnel(pTunnelId);
                        pceStore.removeParentTunnel(pTunnelId);
                    }
                }
                break;

            default:
                break;
            }
        }
    }

    private class InternalConfigListener implements NetworkConfigListener {

        @Override
        public void event(NetworkConfigEvent event) {

            if ((event.type() == NetworkConfigEvent.Type.CONFIG_ADDED)
                    && event.configClass().equals(DeviceCapability.class)) {

                DeviceId deviceIdLsrId = (DeviceId) event.subject();
                String lsrId = deviceIdLsrId.toString();
                DeviceId deviceId = lsrIdDeviceIdMap.get(lsrId);
                if (deviceId == null) {
                    log.debug("Unable to find device id for a lsr-id {} from lsr-id and device-id map.", lsrId);
                    return;
                }

                DeviceCapability cfg = netCfgService.getConfig(DeviceId.deviceId(lsrId), DeviceCapability.class);
                if (cfg == null) {
                    log.error("Unable to find corresponding capabilty for a lsrd {}.", lsrId);
                    return;
                }

                if (cfg.labelStackCap()) {
                    if (mastershipService.getLocalRole(deviceId) == MastershipRole.MASTER) {
                        // Allocate node-label
                        srTeHandler.allocateNodeLabel(deviceId, lsrId);

                        // Allocate adjacency label to links which are
                        // originated from this specific device id
                        Set<Link> links = linkService.getDeviceEgressLinks(deviceId);
                        for (Link link : links) {
                            if (!srTeHandler.allocateAdjacencyLabel(link)) {
                                return;
                            }
                        }
                    }
                }
                // Remove lsrId info from map
                lsrIdDeviceIdMap.remove(lsrId);
            }

            if (event.configClass().equals(TeLinkConfig.class)) {
                LinkKey linkKey = (LinkKey) event.subject();

                TeLinkConfig cfg = netCfgService.getConfig(linkKey, TeLinkConfig.class);
                if (cfg == null) {
                    log.error("Unable to get the configuration of the link.");
                    return;
                }

                switch (event.type()) {
                    case  CONFIG_ADDED:
                    case  CONFIG_UPDATED:
                        pceStore.addUnreservedBw(linkKey, cfg.unResvBandwidth());
                        break;
                    case CONFIG_REMOVED:
                        pceStore.removeUnreservedBw(linkKey);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private boolean syncLabelDb(DeviceId deviceId) {
        checkNotNull(deviceId);

        DeviceId actualDevcieId = pceStore.getLsrIdDevice(deviceId.toString());
        if (actualDevcieId == null) {
            log.error("Device not available {}.", deviceId.toString());
            pceStore.addPccLsr(deviceId);
            return false;
        }

        Device specificDevice = deviceService.getDevice(actualDevcieId);
        if (specificDevice == null) {
            log.error("Unable to find device for specific device id {}.", actualDevcieId.toString());
            return false;
        }

        if (pceStore.getGlobalNodeLabel(actualDevcieId) != null) {
            Map<DeviceId, LabelResourceId> globalNodeLabelMap = pceStore.getGlobalNodeLabels();

            for (Entry<DeviceId, LabelResourceId> entry : globalNodeLabelMap.entrySet()) {
                // Convert from DeviceId to TunnelEndPoint
                Device srcDevice = deviceService.getDevice(entry.getKey());

                /*
                 * If there is a slight difference in timing such that if device subsystem has removed the device but
                 * PCE store still has it, just ignore such devices.
                 */
                if (srcDevice == null) {
                    continue;
                }

                String srcLsrId = srcDevice.annotations().value(LSRID);
                if (srcLsrId == null) {
                    continue;
                }

                srTeHandler.advertiseNodeLabelRule(actualDevcieId,
                                                   entry.getValue(),
                                                   IpPrefix.valueOf(IpAddress.valueOf(srcLsrId), PREFIX_LENGTH),
                                                   Objective.Operation.ADD, false);
            }

            Map<Link, LabelResourceId> adjLabelMap = pceStore.getAdjLabels();
            for (Entry<Link, LabelResourceId> entry : adjLabelMap.entrySet()) {
                if (entry.getKey().src().deviceId().equals(actualDevcieId)) {
                    srTeHandler.installAdjLabelRule(actualDevcieId,
                                                    entry.getValue(),
                                                    entry.getKey().src().port(),
                                                    entry.getKey().dst().port(),
                                                    Objective.Operation.ADD);
                }
            }
        }

        srTeHandler.advertiseNodeLabelRule(actualDevcieId,
                                           LabelResourceId.labelResourceId(0),
                                           IpPrefix.valueOf(END_OF_SYNC_IP_PREFIX),
                                           Objective.Operation.ADD, true);

        log.debug("End of label DB sync for device {}", actualDevcieId);

        if (mastershipService.getLocalRole(specificDevice.id()) == MastershipRole.MASTER) {
            // Allocate node-label to this specific device.
            allocateNodeLabel(specificDevice);

            // Allocate adjacency label
            Set<Link> links = linkService.getDeviceEgressLinks(specificDevice.id());
            if (links != null) {
                for (Link link : links) {
                    allocateAdjacencyLabel(link);
                }
            }
        }
        return true;
    }

    // Process the packet received.
    private class PcepPacketProcessor implements PacketProcessor {
        // Process the packet received and in our case initiates the label DB sync.
        @Override
        public void process(PacketContext context) {
            // Stop processing if the packet has been handled, since we
            // can't do any more to it.
            log.debug("Received trigger for label DB sync.");
            if (context.isHandled()) {
                return;
            }

            InboundPacket pkt = context.inPacket();
            if (pkt == null) {
                return;
            }

            Ethernet ethernet = pkt.parsed();
            if (ethernet == null || ethernet.getEtherType() != Ethernet.TYPE_IPV4) {
                return;
            }

            IPv4 ipPacket = (IPv4) ethernet.getPayload();
            if (ipPacket == null || ipPacket.getProtocol() != IPv4.PROTOCOL_TCP) {
                return;
            }

            TCP tcp = (TCP) ipPacket.getPayload();
            if (tcp == null || tcp.getDestinationPort() != PCEP_PORT) {
                return;
            }

            syncLabelDb(pkt.receivedFrom().deviceId());
        }
    }

    private boolean checkForMasterAndSetupPath(PcePathInfo failedPathInfo) {
        /**
         * Master of ingress node will setup the path failed stored in PCE store.
         */
        if (mastershipService.isLocalMaster(failedPathInfo.src())) {
            if (PathErr.SUCCESS == setupPath(failedPathInfo.src(), failedPathInfo.dst(), failedPathInfo.name(),
                    failedPathInfo.constraints(), failedPathInfo.lspType(), null)) {
                // If computation is success remove that path
                pceStore.removeFailedPathInfo(failedPathInfo);
                return true;
            }
        }

        return false;
    }
}