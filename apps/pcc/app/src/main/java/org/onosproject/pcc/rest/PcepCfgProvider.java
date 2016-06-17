package org.onosproject.pcc.rest;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.config.ConfigFactory;
import org.onosproject.net.config.NetworkConfigEvent;
import org.onosproject.net.config.NetworkConfigListener;
import org.onosproject.net.config.NetworkConfigRegistry;
import org.onosproject.net.config.NetworkConfigService;
import org.onosproject.net.config.basics.SubjectFactories;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.pcc.pccmgr.ctl.PcepConfig;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by root1 on 9/6/16.
 */
@Component(immediate = true)
public class PcepCfgProvider extends AbstractProvider {

    private static final Logger log = getLogger(PcepCfgProvider.class);

    static final String PROVIDER_ID = "org.onosproject.pcc.rest";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkConfigRegistry configRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkConfigService configService;

    private final ConfigFactory configFactory =
            new ConfigFactory(SubjectFactories.APP_SUBJECT_FACTORY, PcepAppConfig.class, "pcepapp") {
                @Override
                public PcepAppConfig createConfig() {
                    return new PcepAppConfig();
                }
            };

    private final NetworkConfigListener configListener = new InternalConfigListener();

    private ApplicationId appId;

    /**
     * Creates a Bgp config provider.
     */
    public PcepCfgProvider() {
        super(new ProviderId("pcep", PROVIDER_ID));
    }

    @Activate
    public void activate(ComponentContext context) {
        appId = coreService.registerApplication(PROVIDER_ID);
        configService.addListener(configListener);
        configRegistry.registerConfigFactory(configFactory);
        createConfiguration();
        log.info("PCEP cfg provider started");
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
        configRegistry.unregisterConfigFactory(configFactory);
        configService.removeListener(configListener);
    }
    /**
     * Reads the configuration and set it to the BGP-LS south bound protocol.
     */
    private void createConfiguration() {
        List<PcepAppConfig.PcepPeerConfig> nodes;
        PcepConfig pcepConfig = PcepConfig.getInstance();
        PcepAppConfig config = configRegistry.getConfig(appId, PcepAppConfig.class);

        log.info("pcep peer configuration received through rest");
        if (config == null) {
            log.warn("No configuration found");
            return;
        }

        nodes = config.pcepPeer();
        for (int i = 0; i < nodes.size(); i++) {
            pcepConfig.addPeer(nodes.get(i).peerIp(), nodes.get(i).asNumber());
            pcepConfig.connectPeer(nodes.get(i).peerIp());
        }
    }

    private void deleteConfiguration() {
        List<PcepAppConfig.PcepPeerConfig> nodes;
        PcepConfig pcepConfig = PcepConfig.getInstance();
        PcepAppConfig config = configRegistry.getConfig(appId, PcepAppConfig.class);

        if (config == null) {
            log.warn("No configuration found");
            return;
        }

        nodes = config.pcepPeer();
        for (int i = 0; i < nodes.size(); i++) {
            pcepConfig.disconnectPeer(nodes.get(i).peerIp());
            pcepConfig.removePeer(nodes.get(i).peerIp());
        }
    }

    /**
     * BGP config listener to populate the configuration.
     */
    private class InternalConfigListener implements NetworkConfigListener {

        @Override
        public void event(NetworkConfigEvent event) {
            if (!event.configClass().equals(PcepAppConfig.class)) {
                return;
            }

            switch (event.type()) {
                case CONFIG_ADDED:
                    createConfiguration();
                    break;
                case CONFIG_REMOVED:
                    deleteConfiguration();
                    break;
                default:
                    break;
            }
        }
    }

}
