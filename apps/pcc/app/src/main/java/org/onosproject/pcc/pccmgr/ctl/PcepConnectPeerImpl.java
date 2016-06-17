package org.onosproject.pcc.pccmgr.ctl;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.onosproject.pcc.pccmgr.api.PcepCfg;
import org.onosproject.pcc.pccmgr.api.PcepConnectPeer;
import org.onosproject.pcc.pccmgr.api.PcepPeerCfg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ramanjaneya on 3/6/16.
 *
 * Implements connection initiation to peer on peer configuration and manage channel using netty channel handler.
 *
 */
public class PcepConnectPeerImpl implements PcepConnectPeer {
    private static final Logger log = LoggerFactory.getLogger(PcepConnectPeerImpl.class);

    private ScheduledExecutorService connectExecutor = null;
    private final String pceSvrIp;
    private static final int RETRY_INTERVAL = 5;
    private final int pceSvrPort;
    private int connectRetryCounter = 0;
    private int connectRetryTime;
    private ClientBootstrap peerBootstrap;
    private PcepCfg pcepconfig;

    public PcepConnectPeerImpl(Controller controller, String pceSvrIp, int pceSvrPort) {
        this.pcepconfig = PcepConfig.getInstance();
        ChannelPipelineFactory pfact = new PcepPipelineFactory(controller);
        this.peerBootstrap = Controller.peerBootstrap();
        this.peerBootstrap.setPipelineFactory(pfact);
        this.pceSvrIp = pceSvrIp;
        this.pceSvrPort = pceSvrPort;
        this.connectRetryTime = 0;
    }

    @Override
    public void disconnectPeer() {
        if (connectExecutor != null) {
            log.info("shutdown peer {}", pceSvrIp);
            connectExecutor.shutdown();
            connectExecutor = null;
        }
    }

    @Override
    public void connectPeer() {
        scheduleConnectionRetry(this.connectRetryTime);
    }

    /**
     * Retry connection with exponential back-off mechanism.
     *
     * @param retryDelay retry delay
     */
    private void scheduleConnectionRetry(long retryDelay) {
        if (this.connectExecutor == null) {
            log.info("connectExecutor  newSingleThreadScheduledExecutor {}", pceSvrIp);
            this.connectExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        this.connectExecutor.schedule(new ConnectionRetry(), retryDelay, TimeUnit.SECONDS);
    }

    /**
     * Implements BGP connection and manages connection to peer with back-off mechanism in case of failure.
     */
    class ConnectionRetry implements Runnable {
        @Override
        public void run() {
            log.info("Connect to peer {}", pceSvrIp);

            InetSocketAddress connectToSocket = new InetSocketAddress(pceSvrIp, pceSvrPort);

            try {
                pcepconfig.setPeerConnState(pceSvrIp, PcepPeerCfg.State.CONNECT);
                peerBootstrap.connect(connectToSocket).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            pcepconfig.setPeerConnState(pceSvrIp, PcepPeerCfg.State.ACTIVE);
                            connectRetryCounter++;
                            log.error("Connection failed, ConnectRetryCounter {} remote host {}", connectRetryCounter,
                                    pceSvrIp);
                            /*
                             * Reconnect to peer on failure is exponential till 4 mins, later on retry after every 4
                             * mins.
                             */
                            if (connectRetryTime < RETRY_INTERVAL) {
                                connectRetryTime = (connectRetryTime != 0) ? connectRetryTime * 2 : 1;
                            }
                            scheduleConnectionRetry(connectRetryTime);
                        } else {

                            connectRetryCounter++;
                            log.info("Connected to remote host {}, Connect Counter {}", pceSvrIp, connectRetryCounter);
                            disconnectPeer();
                        }
                    }
                });
            } catch (Exception e) {
                log.info("Connect peer exception : " + e.toString());
                disconnectPeer();
            }
        }
    }
}
