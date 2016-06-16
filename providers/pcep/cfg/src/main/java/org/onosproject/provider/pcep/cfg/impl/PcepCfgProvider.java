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
package org.onosproject.provider.pcep.cfg.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.IpAddress;
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
import org.onosproject.pcep.controller.PcepCfg;
import org.onosproject.pcep.controller.PcepCfgData;
import org.onosproject.pcep.controller.PcepClientController;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * PCEP config provider to validate and populate the configuration.
 */
@Component(immediate = true)
public class PcepCfgProvider extends AbstractProvider {

    private static final Logger log = getLogger(PcepCfgProvider.class);

    static final String PROVIDER_ID = "org.onosproject.provider.pcep.cfg";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PcepClientController pcepController;

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
     * Creates a PCEP config provider.
     */
    public PcepCfgProvider() {
        super(new ProviderId("pcep", PROVIDER_ID));
    }

    @Activate
    public void activate(ComponentContext context) {
        appId = coreService.registerApplication(PROVIDER_ID);
        configService.addListener(configListener);
        configRegistry.registerConfigFactory(configFactory);
        readConfiguration();
        log.info("PCEP cfg provider started");
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
        configRegistry.unregisterConfigFactory(configFactory);
        configService.removeListener(configListener);
    }

    void setPcepController(PcepClientController pcepController) {
        this.pcepController = pcepController;
    }

    /**
     * Reads the configuration and set it to the PCEP south bound protocol.
     */
    private void readConfiguration() {
        PcepCfg pcepConfig = null;
        List<PcepAppConfig.PcepConfig> nodes;
        pcepConfig = pcepController.getConfig();
        PcepAppConfig config = configRegistry.getConfig(appId, PcepAppConfig.class);

        if (config == null) {
            log.warn("No configuration found");
            return;
        }
        /*String lspType = config.lspType();
        if (lspType != null) {
            int lsp = 1;
            if (lspType.equals("SR")) {
                lsp = 2;
            } else if (lspType.equals("CR")) {
                lsp = 3;
            }
            pcepConfig.setLspType(lsp);
        }*/

        nodes = config.pcepDomainMap();
        for (int i = 0; i < nodes.size(); i++) {
            pcepConfig.addConfig(IpAddress.valueOf(nodes.get(i).ipAddress()), nodes.get(i).asNumber());
        }
    }

    /**
     * Read the configuration and update it to the BGP-LS south bound protocol.
     */
    private void updateConfiguration() {
        PcepCfg pcepConfig = null;
        List<PcepAppConfig.PcepConfig> nodes;
        TreeMap<Integer, PcepCfgData> pcepTree;
        pcepConfig = pcepController.getConfig();
        PcepCfgData cfgData = null;
        PcepAppConfig config = configRegistry.getConfig(appId, PcepAppConfig.class);

        if (config == null) {
            log.warn("No configuration found");
            return;
        }

        /*String lspType = config.lspType();
        if (lspType != null) {
            int lsp = 1;
            if (lspType.equals("SR")) {
                lsp = 2;
            } else if (lspType.equals("CR")) {
                lsp = 3;
            }
            pcepConfig.setLspType(lsp);
        }*/

        /* update the peer configuration */
        pcepTree = pcepConfig.pcepDomainMap();
        if (pcepTree.isEmpty()) {
            log.info("There are no PCEP peers to iterate");
        } else {
            Set set = pcepTree.entrySet();
            Iterator i = set.iterator();
            List<PcepCfgData> absPeerList = new ArrayList<PcepCfgData>();

            boolean exists = false;

            while (i.hasNext()) {
                Map.Entry me = (Map.Entry) i.next();
                cfgData = (PcepCfgData) me.getValue();

                nodes = config.pcepDomainMap();
                for (int j = 0; j < nodes.size(); j++) {
                    Integer peerIp = nodes.get(j).asNumber();
                    if (peerIp.equals(cfgData.asNumber())) {
                        cfgData.setIpAddress(IpAddress.valueOf(nodes.get(j).ipAddress()));
                        nodes.remove(j);
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    absPeerList.add(cfgData);
                    exists = false;
                }
            }

            /* Remove the absent nodes. */
            for (int j = 0; j < absPeerList.size(); j++) {
                pcepConfig.deleteConfig(absPeerList.get(j).asNumber());
            }
        }

        nodes = config.pcepDomainMap();
        for (int i = 0; i < nodes.size(); i++) {
            pcepConfig.addConfig(IpAddress.valueOf(nodes.get(i).ipAddress()), nodes.get(i).asNumber());
        }
    }

    /**
     * PCEP config listener to populate the configuration.
     */
    private class InternalConfigListener implements NetworkConfigListener {

        @Override
        public void event(NetworkConfigEvent event) {
            if (!event.configClass().equals(PcepAppConfig.class)) {
                return;
            }

            switch (event.type()) {
                case CONFIG_ADDED:
                    readConfiguration();
                    break;
                case CONFIG_UPDATED:
                    updateConfiguration();
                    break;
                case CONFIG_REMOVED:
                default:
                    break;
            }
        }
    }
}
