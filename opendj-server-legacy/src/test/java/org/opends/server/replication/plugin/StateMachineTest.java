/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at legal-notices/CDDLv1_0.txt
 * or http://forgerock.org/license/CDDLv1.0.html.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at legal-notices/CDDLv1_0.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2009-2010 Sun Microsystems, Inc.
 *      Portions Copyright 2011-2015 ForgeRock AS
 */
package org.opends.server.replication.plugin;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.forgerock.i18n.LocalizableMessage;
import org.forgerock.i18n.slf4j.LocalizedLogger;
import org.opends.server.TestCaseUtils;
import org.opends.server.admin.server.ConfigurationChangeListener;
import org.opends.server.admin.std.server.ReplicationSynchronizationProviderCfg;
import org.opends.server.admin.std.server.SynchronizationProviderCfg;
import org.opends.server.api.SynchronizationProvider;
import org.opends.server.core.DirectoryServer;
import org.opends.server.replication.ReplicationTestCase;
import org.opends.server.replication.common.CSNGenerator;
import org.opends.server.replication.common.DSInfo;
import org.opends.server.replication.common.ServerState;
import org.opends.server.replication.common.ServerStatus;
import org.opends.server.replication.protocol.AddMsg;
import org.opends.server.replication.protocol.DoneMsg;
import org.opends.server.replication.protocol.EntryMsg;
import org.opends.server.replication.protocol.InitializeTargetMsg;
import org.opends.server.replication.protocol.ReplSessionSecurity;
import org.opends.server.replication.protocol.ReplicationMsg;
import org.opends.server.replication.protocol.ResetGenerationIdMsg;
import org.opends.server.replication.protocol.RoutableMsg;
import org.opends.server.replication.server.ReplServerFakeConfiguration;
import org.opends.server.replication.server.ReplicationServer;
import org.opends.server.replication.service.ReplicationBroker;
import org.opends.server.types.Attribute;
import org.opends.server.types.DN;
import org.opends.server.types.Entry;
import org.opends.server.util.TestTimer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.util.concurrent.TimeUnit.*;

import static org.mockito.Mockito.*;
import static org.opends.server.util.CollectionUtils.*;
import static org.testng.Assert.*;

/**
 * Some tests to go through the DS state machine and validate we get the
 * expected status according to the actions we perform.
 */
@SuppressWarnings("javadoc")
public class StateMachineTest extends ReplicationTestCase
{
  /** Server id definitions. */
  private static final String EXAMPLE_DN = "dc=example,dc=com";
  private static DN EXAMPLE_DN_;

  private static final int DS1_ID = 1;
  private static final int DS2_ID = 2;
  private static final int DS3_ID = 3;
  private static final int RS1_ID = 41;
  private int rs1Port = -1;
  private LDAPReplicationDomain ds1;
  private ReplicationBroker ds2;
  private ReplicationBroker ds3;
  private ReplicationServer rs1;
  /** The tracer object for the debug logger. */
  private static final LocalizedLogger logger = LocalizedLogger.getLoggerForThisClass();
  private final int initWindow = 100;

  private void debugInfo(String s)
  {
    logger.error(LocalizableMessage.raw(s));
    if (logger.isTraceEnabled())
    {
      logger.trace("** TEST **" + s);
    }
  }

  private static void shutdown(BrokerReader reader)
  {
    if (reader != null)
    {
      reader.shutdown();
    }
  }

  private static void shutdown(BrokerWriter writer)
  {
    if (writer != null)
    {
      writer.shutdown();
    }
  }

  private void initTest() throws IOException
  {
    rs1Port = -1;
    ds1 = null;
    ds2 = null;
    ds3 = null;
    rs1Port = TestCaseUtils.findFreePort();
  }

  private void endTest() throws Exception
  {
    if (ds1 != null)
    {
      ds1.shutdown();
      ds1 = null;
    }

    // Clear any reference to a domain in synchro plugin
    MultimasterReplication.deleteDomain(EXAMPLE_DN_);
    stop(ds2, ds3);
    ds2 = ds3 = null;
    remove(rs1);
    rs1 = null;
    rs1Port = -1;
  }

  /** Waits until the provided ds is connected to the replication server. */
  private void waitUntiConnected(final int dsId) throws Exception
  {
    TestTimer timer = new TestTimer.Builder()
      .maxSleep(30, SECONDS)
      .sleepTimes(100, MILLISECONDS)
      .toTimer();
    timer.repeatUntilSuccess(new Callable<Void>()
    {
      @Override
      public Void call() throws Exception
      {
        assertTrue(isConnected(dsId), "checkConnection: DS " + dsId + " is not connected to the RS");
        return null;
      }
    });
  }

  private boolean isConnected(final int dsId)
  {
    switch (dsId)
    {
    case DS1_ID:
      return ds1.isConnected();
    case DS2_ID:
      return ds2.isConnected();
    case DS3_ID:
      return ds3.isConnected();
    default:
      fail("Unknown ds server id.");
      return false;
    }
  }

  /** Creates a new ReplicationServer. */
  private ReplicationServer createReplicationServer(String testCase,
      int degradedStatusThreshold) throws Exception
  {
    SortedSet<String> replServers = new TreeSet<>();

    String dir = "stateMachineTest" + RS1_ID + testCase + "Db";
    ReplServerFakeConfiguration conf =
        new ReplServerFakeConfiguration(rs1Port, dir, 0, RS1_ID, 0,
            100, replServers, 1, 1000, degradedStatusThreshold);
    return new ReplicationServer(conf);
  }

  /** Creates and starts a new ReplicationDomain configured for the replication server. */
  @SuppressWarnings("unchecked")
  private LDAPReplicationDomain createReplicationDomain(int dsId) throws Exception
  {
    SortedSet<String> replServers = new TreeSet<>();
    replServers.add("localhost:" + rs1Port);

    DomainFakeCfg domainConf = new DomainFakeCfg(EXAMPLE_DN_, dsId, replServers);
    LDAPReplicationDomain replicationDomain = MultimasterReplication.createNewDomain(domainConf);
    replicationDomain.start();
    SynchronizationProvider<SynchronizationProviderCfg> provider =
        DirectoryServer.getSynchronizationProviders().get(0);
    if (provider instanceof ConfigurationChangeListener)
    {
      ConfigurationChangeListener<ReplicationSynchronizationProviderCfg> mmr =
          (ConfigurationChangeListener<ReplicationSynchronizationProviderCfg>) provider;
      mmr.applyConfigurationChange(mock(ReplicationSynchronizationProviderCfg.class));
    }

    return replicationDomain;
  }

  /**
   * Create and connect a replication broker to the replication server with the
   * given state and generation id.
   */
  private ReplicationBroker createReplicationBroker(int dsId,
      ServerState state, long generationId) throws Exception
  {
    SortedSet<String> replServers = newTreeSet("localhost:" + rs1Port);
    DomainFakeCfg fakeCfg = new DomainFakeCfg(EXAMPLE_DN_, dsId, replServers);
    fakeCfg.setHeartbeatInterval(0);
    fakeCfg.setChangetimeHeartbeatInterval(500);
    ReplSessionSecurity security = new ReplSessionSecurity(null, null, null, true);
    ReplicationBroker broker = new ReplicationBroker(
        new DummyReplicationDomain(generationId), state, fakeCfg, security);
    broker.start();
    checkConnection(30, broker, rs1Port);
    return broker;
  }

  /**
   * Make simple state machine test.
   *
   * NC = Not connected status
   * N = Normal status
   * D = Degraded status
   * FU = Full update status
   * BG = Bad generation id status
   *
   * The test path should be:
   * ->NC->N->NC
   * @throws Exception If a problem occurred
   */
  @Test(enabled=true)
  public void testStateMachineBasic() throws Exception
  {
    String testCase = "testStateMachineBasic";

    debugInfo("Starting " + testCase);

    initTest();

    try
    {
      // DS1 start, no RS available: DS1 should be in not connected status
      ds1 = createReplicationDomain(DS1_ID);
      waitUntilStatusEquals(ds1, ServerStatus.NOT_CONNECTED_STATUS);

      // RS1 starts , DS1 should connect to it and be in normal status
      rs1 = createReplicationServer(testCase, 5000);
      waitUntilStatusEquals(ds1, ServerStatus.NORMAL_STATUS);

      // RS1 stops, DS1 should go in not connected status
      rs1.remove();
      waitUntilStatusEquals(ds1, ServerStatus.NOT_CONNECTED_STATUS);
    } finally
    {
      endTest();
    }
  }

  /** Returns various init values for test testStateMachineStatusAnalyzer. */
  @DataProvider(name="stateMachineStatusAnalyzerTestProvider")
  public Object [][] stateMachineStatusAnalyzerTestProvider() throws Exception
  {
    return new Object [][] { {1} , {10}, {50}, {120} };
  }

  /**
   * Test the status analyzer system that allows to go from normal to degraded
   * and vice versa, using the configured threshold value
   *
   * NC = Not connected status
   * N = Normal status
   * D = Degraded status
   * FU = Full update status
   * BG = Bad generation id status
   *
   * Expected path:
   * ->NC->N->D->N->NC
   * @throws Exception If a problem occurred
   */
  @Test(enabled=true, groups="slow", dataProvider="stateMachineStatusAnalyzerTestProvider")
  public void testStateMachineStatusAnalyzer(int thresholdValue) throws Throwable
  {
    String testCase = "testStateMachineStatusAnalyzer with threhold " + thresholdValue;

    debugInfo("Starting " + testCase + " with " + thresholdValue);

      initTest();

      BrokerReader br3 = null;
      BrokerReader br2 = null;
      BrokerWriter bw = null;

    try
    {
      /* RS1 starts with specified threshold value */
      rs1 = createReplicationServer(testCase, thresholdValue);

      /*
       * DS2 starts and connects to RS1. No reader and low window value at the beginning
       * so writer for DS2 in RS should enqueue changes after first changes sent to DS.
       * (window value reached: a window msg needed by RS for following sending changes to DS)
       */
      ds2 = createReplicationBroker(DS2_ID, new ServerState(), EMPTY_DN_GENID);
      waitUntiConnected(DS2_ID);

      /* DS3 starts and connects to RS1 */
      ds3 = createReplicationBroker(DS3_ID, new ServerState(), EMPTY_DN_GENID);
      br3 = new BrokerReader(ds3, DS3_ID);
      waitUntiConnected(DS3_ID);

      // Send first changes to reach window and block DS2 writer queue. Writer will take them
      // from queue and block (no more changes removed from writer queue) after
      // having sent them to TCP receive queue of DS2.
      bw = new BrokerWriter(ds3, DS3_ID, false);
      bw.followAndPause(11);

      /*
       * DS3 sends changes (less than threshold): DS2 should still be in normal status
       * so no topo message should be sent (update topo message for telling status of DS2 changed)
       */
      int nChangesSent = 0;
      if (thresholdValue > 1)
      {
        nChangesSent = thresholdValue - 1;
        bw.followAndPause(nChangesSent);
        Thread.sleep(1000); // Be sure status analyzer has time to test
        ReplicationMsg msg = br3.getLastMsg();
        debugInfo(testCase + " Step 1: last message from writer: " + msg);
        assertNull(msg, (msg != null) ? msg.toString() : "null");
      }

      /*
       * DS3 sends changes to reach the threshold value,
       * DS3 should receive an update topo message with status of DS2: degraded status
       */
      bw.followAndPause(thresholdValue - nChangesSent);
      // wait for a status MSG status analyzer to broker 3
      waitForDegradedStatusOnBroker3();

      /*
       * DS3 sends 10 additional changes after threshold value,
       * DS2 should still be degraded so no topo message received.
       */
      bw.followAndPause(10);
      shutdown(bw);
      Thread.sleep(1000); // Be sure status analyzer has time to test
      ReplicationMsg lastMsg = br3.getLastMsg();
      ReplicationMsg msg = br3.getLastMsg();
      debugInfo(testCase + " Step 3: last message from writer: " + msg);
      assertNull(lastMsg);

      /*
       * DS2 replays every changes and should go back to normal status
       * (create a reader to emulate replay of messages (messages read from queue))
       */
      br2 = new BrokerReader(ds2, DS2_ID);
      // wait for a status MSG status analyzer to broker 3
      waitForDegradedStatusOnBroker3();
    } finally
    {
      endTest();
      shutdown(bw);
      shutdown(br3);
      shutdown(br2);
    }
  }

  private void waitForDegradedStatusOnBroker3() throws InterruptedException
  {
    for (int count = 0; count< 50; count++)
    {
      DSInfo dsInfo = ds3.getReplicaInfos().get(DS2_ID);
      if (dsInfo != null && dsInfo.getStatus() == ServerStatus.DEGRADED_STATUS)
      {
        break;
      }

      assertTrue(count < 50, "DS2 did not get degraded : " + dsInfo);
      Thread.sleep(200); // Be sure status analyzer has time to test
    }
  }

  /**
   * Go through the possible state machine transitions:
   *
   * NC = Not connected status
   * N = Normal status
   * D = Degraded status
   * FU = Full update status
   * BG = Bad generation id status
   *
   * The test path should be:
   * ->NC->D->N->NC->N->D->NC->D->N->BG->NC->N->D->BG->FU->NC->N->D->FU->NC->BG->NC->N->FU->NC->N->NC
   * @throws Exception If a problem occurred
   */
  @Test(enabled = false, groups = "slow")
  public void testStateMachineFull() throws Exception
  {
    String testCase = "testStateMachineFull";

    debugInfo("Starting " + testCase);

    initTest();
    BrokerReader br = null;
    BrokerWriter bw = null;

    try
    {
      int DEGRADED_STATUS_THRESHOLD = 1;

      // RS1 starts with 1 message as degraded status threshold value
      rs1 = createReplicationServer(testCase, DEGRADED_STATUS_THRESHOLD);

      // DS2 starts and connects to RS1
      ds2 = createReplicationBroker(DS2_ID, new ServerState(), EMPTY_DN_GENID);
      br = new BrokerReader(ds2, DS2_ID);
      waitUntiConnected(DS2_ID);

      // DS2 starts sending a lot of changes
      bw = new BrokerWriter(ds2, DS2_ID, false);
      bw.follow();
      Thread.sleep(1000); // Let some messages being queued in RS

      /*
       * DS1 starts and connects to RS1, server state exchange should lead to start in degraded status
       * as some changes should be in queued in the RS and the threshold value is 1 change in queue.
       */
      ds1 = createReplicationDomain(DS1_ID);
      waitUntiConnected(DS1_ID);
      waitUntilStatusEquals(ds1, ServerStatus.DEGRADED_STATUS);

      /* DS2 stops sending changes: DS1 should replay pending changes and should enter the normal status */
      bw.pause();
      // Sleep enough so that replay can be done and analyzer has time
      // to see that the queue length is now under the threshold value.
      waitUntilStatusEquals(ds1, ServerStatus.NORMAL_STATUS);

      /* RS1 stops to make DS1 go to not connected status (from normal status) */
      rs1.remove();
      waitUntilStatusEquals(ds1, ServerStatus.NOT_CONNECTED_STATUS);

      /*
       * DS2 restarts with up to date server state
       * (this allows to have restarting RS1 not sending him some updates he already sent)
       */
      ds2.stop();
      shutdown(bw);
      shutdown(br);
      ServerState curState = ds1.getServerState();
      ds2 = createReplicationBroker(DS2_ID, curState, EMPTY_DN_GENID);
      br = new BrokerReader(ds2, DS2_ID);

      /* RS1 restarts, DS1 should get back to normal status */
      rs1 = createReplicationServer(testCase, DEGRADED_STATUS_THRESHOLD);
      waitUntiConnected(DS2_ID);
      waitUntilStatusEquals(ds1, ServerStatus.NORMAL_STATUS);

      /* DS2 sends again a lot of changes to make DS1 degraded again */
      bw = new BrokerWriter(ds2, DS2_ID, false);
      bw.follow();
      Thread.sleep(8000); // Let some messages being queued in RS, and analyzer see the change
      waitUntilStatusEquals(ds1, ServerStatus.DEGRADED_STATUS);

      /* RS1 stops to make DS1 go to not connected status (from degraded status) */
      rs1.remove();
      bw.pause();
      waitUntilStatusEquals(ds1, ServerStatus.NOT_CONNECTED_STATUS);

      /*
       * DS2 restarts with up to date server state
       * (this allows to have restarting RS1 not sending him some updates he already sent)
       */
      ds2.stop();
      shutdown(bw);
      shutdown(br);
      curState = ds1.getServerState();
      ds2 = createReplicationBroker(DS2_ID, curState, EMPTY_DN_GENID);
      br = new BrokerReader(ds2, DS2_ID);

      /*
       * RS1 restarts, DS1 should reconnect in degraded status
       * (from not connected this time, not from state machine entry)
       */
      rs1 = createReplicationServer(testCase, DEGRADED_STATUS_THRESHOLD);
      // It is too difficult to tune the right sleep so disabling this test:
      // Sometimes the status analyzer may be fast and quickly change the status
      // of DS1 to NORMAL_STATUS
      //sleep(2000);
      //sleepAssertStatusEquals(30, ds1, ServerStatus.DEGRADED_STATUS);
      waitUntiConnected(DS2_ID);

      /* DS1 should come back in normal status after a while */
      waitUntilStatusEquals(ds1, ServerStatus.NORMAL_STATUS);

      /*
       * DS2 sends a reset gen id order with wrong gen id:
       * DS1 should go into bad generation id status
       */
      long BAD_GEN_ID = 999999L;
      resetGenId(ds2, BAD_GEN_ID); // ds2 will also go bad gen
      waitUntilStatusEquals(ds1, ServerStatus.BAD_GEN_ID_STATUS);

      /*
       * DS2 sends again a reset gen id order with right id: DS1 should be disconnected by RS
       * then reconnect and enter again in normal status. This goes through not connected status
       * but not possible to check as should reconnect immediately
       */
      resetGenId(ds2, EMPTY_DN_GENID); // ds2 will also be disconnected
      ds2.stop();
      shutdown(br); // Reader could reconnect broker, but gen id would be bad: need to recreate a
                    // broker to send changex
      waitUntilStatusEquals(ds1, ServerStatus.NORMAL_STATUS);

      /* DS2 sends again a lot of changes to make DS1 degraded again */
      curState = ds1.getServerState();
      ds2 = createReplicationBroker(DS2_ID, curState, EMPTY_DN_GENID);
      waitUntiConnected(DS2_ID);
      bw = new BrokerWriter(ds2, DS2_ID, false);
      br = new BrokerReader(ds2, DS2_ID);
      bw.follow();
      Thread.sleep(8000); // Let some messages being queued in RS, and analyzer see the change
      waitUntilStatusEquals(ds1, ServerStatus.DEGRADED_STATUS);

      /*
       * DS2 sends reset gen id order with bad gen id: DS1 should go in bad gen id status
       * (from degraded status this time)
       */
      resetGenId(ds2, -1); // -1 to allow next step full update and flush RS db so that DS1 can reconnect after full update
      waitUntilStatusEquals(ds1, ServerStatus.BAD_GEN_ID_STATUS);
      bw.pause();

      /*
       * DS2 engages full update (while DS1 in bad gen id status),
       * DS1 should go in full update status
       */
      BrokerInitializer bi = new BrokerInitializer(ds2, DS2_ID, false);
      bi.initFullUpdate(DS1_ID, 200);
      waitUntilStatusEquals(ds1, ServerStatus.FULL_UPDATE_STATUS);

      /*
       * DS2 terminates full update to DS1: DS1 should reconnect (goes through not connected status)
       * and come back to normal status (RS genid was -1 so RS will adopt new gen id)
       */
      bi.runFullUpdate();
      waitUntilStatusEquals(ds1, ServerStatus.NORMAL_STATUS);

      /* DS2 sends changes to DS1: DS1 should go in degraded status */
      ds2.stop(); // will need a new broker with another gen id restart it
      shutdown(bw);
      shutdown(br);
      long newGen = ds1.getGenerationID();
      curState = ds1.getServerState();
      ds2 = createReplicationBroker(DS2_ID, curState, newGen);
      waitUntiConnected(DS2_ID);
      bw = new BrokerWriter(ds2, DS2_ID, false);
      br = new BrokerReader(ds2, DS2_ID);
      bw.follow();
      Thread.sleep(8000); // Let some messages being queued in RS, and analyzer see the change
      waitUntilStatusEquals(ds1, ServerStatus.DEGRADED_STATUS);

      /* DS2 engages full update (while DS1 in degraded status), DS1 should go in full update status */
      bi = new BrokerInitializer(ds2, DS2_ID, false);
      bi.initFullUpdate(DS1_ID, 300);
      waitUntilStatusEquals(ds1, ServerStatus.FULL_UPDATE_STATUS);
      bw.pause();

      /*
       * DS2 terminates full update to DS1: DS1 should reconnect (goes through not connected status)
       * and come back to bad gen id status (RS genid was another gen id (300 entries instead of 200)
       */
      bi.runFullUpdate();
      waitUntilStatusEquals(ds1, ServerStatus.BAD_GEN_ID_STATUS);

      /*
       * DS2 sends reset gen id with gen id same as DS1:
       * DS1 will be disconnected by RS (not connected status) and come back to normal status
       */
      ds2.stop(); // will need a new broker with another gen id restart it
      shutdown(bw);
      shutdown(br);
      newGen = ds1.getGenerationID();
      curState = ds1.getServerState();
      ds2 = createReplicationBroker(DS2_ID, curState, newGen);
      waitUntiConnected(DS2_ID);
      br = new BrokerReader(ds2, DS2_ID);
      resetGenId(ds2, newGen); // Make DS1 reconnect in normal status

      waitUntilStatusEquals(ds1, ServerStatus.NORMAL_STATUS);

      /* DS2 engages full update (while DS1 in normal status), DS1 should go in full update status */
      bi = new BrokerInitializer(ds2, DS2_ID, false);
      bi.initFullUpdate(DS1_ID, 300); // 300 entries will compute same genid of the RS
      waitUntilStatusEquals(ds1, ServerStatus.FULL_UPDATE_STATUS);

      /*
       * DS2 terminates full update to DS1: DS1 should reconnect (goes through not connected status)
       * and come back to normal status (process full update with same data as before so RS already
       * has right gen id: version with 300 entries)
       */
      bi.runFullUpdate();
      ds2.stop();
      shutdown(br);
      waitUntilStatusEquals(ds1, ServerStatus.NORMAL_STATUS);

      /* RS1 stops, DS1 should go to not connected status */
      rs1.remove();
      waitUntilStatusEquals(ds1, ServerStatus.NOT_CONNECTED_STATUS);
    } finally
    {
      // Finalize test
      endTest();
      shutdown(bw);
      shutdown(br);
    }
  }

  /**
   * Set up the environment.
   *
   * @throws Exception
   *           If the environment could not be set up.
   */
  @BeforeClass
  @Override
  public void setUp() throws Exception
  {
    super.setUp();
    EXAMPLE_DN_ = DN.valueOf(EXAMPLE_DN);

    // Note: this test does not use the memory test backend as for having a DS
    // going into degraded status, we need to send a lot of updates. This makes
    // the memory test backend crash with OutOfMemoryError. So we prefer here
    // a backend backed up with a file
    TestCaseUtils.clearBackend("userRoot");
  }

  /**
   * Clean up the environment.
   *
   * @throws Exception If the environment could not be set up.
   */
  @AfterClass
  @Override
  public void classCleanUp() throws Exception
  {
    callParanoiaCheck = false;
    super.classCleanUp();

    TestCaseUtils.clearBackend("userRoot");

    paranoiaCheck();
  }

  /**
   * Sends a reset genid message through the given replication broker, with the
   * given new generation id.
   */
  private void resetGenId(ReplicationBroker rb, long newGenId)
  {
    ResetGenerationIdMsg resetMsg = new ResetGenerationIdMsg(newGenId);
    rb.publish(resetMsg);
  }

  /**
   * Utility class for making a full update through a broker. No separated thread
   * Usage:
   * BrokerInitializer bi = new BrokerInitializer(rb, sid, nEntries);
   * bi.initFullUpdate(); // Initializes a full update session by sending InitializeTargetMsg
   * bi.runFullUpdate(); // loops sending nEntries entries and finalizes the full update by sending the EntryDoneMsg
   */
  private class BrokerInitializer
  {
    private ReplicationBroker rb;
    private int serverId = -1;
    private long userId;
    /** Server id of server to initialize. */
    private int destId = -1;
    /** Number of entries to send to dest. */
    private long nEntries = -1;
    private boolean createReader;

    /**
     * If the BrokerInitializer is to be used for a lot of entries to send
     * (which is often the case), the reader thread should be enabled to make
     * the window subsystem work and allow the broker to send as much entries as
     * he wants. If not enabled, the user is responsible to call the receive
     * method of the broker himself.
     */
    private BrokerReader reader;

    /** Creates a broker initializer. Also creates a reader according to request */
    public BrokerInitializer(ReplicationBroker rb, int serverId,
      boolean createReader)
    {
      this.rb = rb;
      this.serverId = serverId;
      this.createReader = createReader;
    }

    /** Initializes a full update session by sending InitializeTargetMsg. */
    public void initFullUpdate(int destId, long nEntries)
    {
      // Also create reader ?
      if (createReader)
      {
        reader = new BrokerReader(rb, serverId);
      }

      debugInfo("Broker " + serverId + " initializer sending InitializeTargetMsg to server " + destId);

      this.destId = destId;
      this.nEntries = nEntries;

      // Send init msg to warn dest server it is going do be initialized
      RoutableMsg initTargetMsg = new InitializeTargetMsg(
          EXAMPLE_DN_, serverId, destId, serverId, nEntries, initWindow);
      rb.publish(initTargetMsg);

      // Send top entry for the domain
      String topEntry = "dn: " + EXAMPLE_DN + "\n"
        + "objectClass: top\n"
        + "objectClass: domain\n"
        + "dc: example\n"
        + "entryUUID: 11111111-1111-1111-1111-111111111111\n\n";
      EntryMsg entryMsg = new EntryMsg(serverId, destId, topEntry.getBytes(), 1);
      rb.publish(entryMsg);
    }

    private EntryMsg createNextEntryMsg()
    {
      String userEntryUUID = "11111111-1111-1111-1111-111111111111";
      long curId = ++userId;
      String userdn = "uid=full_update_user" + curId + "," + EXAMPLE_DN;
      String entryWithUUIDldif = "dn: " + userdn + "\n" + "objectClass: top\n" +
        "objectClass: person\n" + "objectClass: organizationalPerson\n" +
        "objectClass: inetOrgPerson\n" +
        "uid: full_update_user" + curId + "\n" +
        "homePhone: 951-245-7634\n" +
        "description: This is the description for Aaccf Amar.\n" + "st: NC\n" +
        "mobile: 027-085-0537\n" +
        "postalAddress: Aaccf Amar$17984 Thirteenth Street" +
        "$Rockford, NC  85762\n" + "mail: user.1@example.com\n" +
        "cn: Aaccf Amar\n" + "l: Rockford\n" + "pager: 508-763-4246\n" +
        "street: 17984 Thirteenth Street\n" + "telephoneNumber: 216-564-6748\n" +
        "employeeNumber: 1\n" + "sn: Amar\n" + "givenName: Aaccf\n" +
        "postalCode: 85762\n" + "userPassword: password\n" + "initials: AA\n" +
        "entryUUID: " + userEntryUUID + "\n\n";
      // -> WARNING: EntryMsg PDUs are concatenated before calling import on LDIF
      // file so need \n\n to separate LDIF entries to conform to LDIF file format

      // Create an entry message
      return new EntryMsg(serverId, destId, entryWithUUIDldif.getBytes(),
          (int) userId);
    }

    /**
     * Loops sending entries for full update (EntryMsg messages). When
     * terminates, sends the EntryDoneMsg to finalize full update. Number of
     * sent entries is determined at initFullUpdate call time.
     */
    public void runFullUpdate()
    {
      debugInfo("Broker " + serverId + " initializer starting sending entries to server " + destId);

      for(long i = 0 ; i<nEntries ; i++) {
          EntryMsg entryMsg = createNextEntryMsg();
          rb.publish(entryMsg);
      }

      debugInfo("Broker " + serverId + " initializer stopping sending entries");

      debugInfo("Broker " + serverId + " initializer sending EntryDoneMsg");
      DoneMsg doneMsg = new DoneMsg(serverId, destId);
      rb.publish(doneMsg);

      if (createReader)
      {
        shutdown(reader);
      }

      debugInfo("Broker " + serverId + " initializer thread is dying");
    }
  }

  /** Thread for sending a lot of changes through a broker. */
  private class BrokerWriter extends Thread
  {
    private ReplicationBroker rb;
    private int serverId = -1;
    private long userId;
    private AtomicBoolean shutdown = new AtomicBoolean(false);
    /** The writer starts suspended. */
    private AtomicBoolean suspended = new AtomicBoolean(true);
    /**
     * Tells a sending session is finished. A session is sending messages
     * between the follow and the pause calls, or the time a followAndPause
     * method runs.
     */
    private AtomicBoolean sessionDone = new AtomicBoolean(true);
    private boolean careAboutAmountOfChanges;
    /** Number of sent changes. */
    private int nChangesSent;
    private int nChangesSentLimit;
    private CSNGenerator gen;
    private Object sleeper = new Object();
    /**
     * If the BrokerWriter is to be used for a lot of changes to send (which is
     * often the case), the reader thread should be enabled to make the window
     * subsystem work and allow the broker to send as much changes as he wants.
     * If not enabled, the user is responsible to call the receive method of
     * the broker himself.
     */
    private BrokerReader reader;

    /** Creates a broker writer. Also creates a reader according to request */
    public BrokerWriter(ReplicationBroker rb, int serverId,
      boolean createReader)
    {
      super("BrokerWriter for broker " + serverId);
      this.rb = rb;
      this.serverId = serverId;
      // Create a csn generator to generate new csns
      // when we need to send changes
      gen = new CSNGenerator(serverId, 0);

      // Start thread (is paused by default so will have to call follow anyway)
      start();

      // Also create reader ?
      if (createReader)
      {
        reader = new BrokerReader(rb, serverId);
      }
    }

    /**
     * Loops sending changes: add operations creating users with different ids
     * This starts paused and has to be resumed calling a follow method.
     */
    @Override
    public void run()
    {
      boolean dbg1Written = false, dbg2Written;
      // No stop msg when entering the loop (thread starts with paused writer)
      dbg2Written = true;
      while (!shutdown.get())
      {
        long startSessionTime = -1;
        boolean startedNewSession = false;
        // When not in pause, loop sending changes to RS
        while (!suspended.get())
        {
          startedNewSession = true;
          if (!dbg1Written)
          {
            startSessionTime = System.currentTimeMillis();
            debugInfo("Broker " + serverId +
              " writer starting sending changes session at: " + startSessionTime);
            dbg1Written = true;
            dbg2Written = false;
          }
          AddMsg addMsg = createNextAddMsg();
          rb.publish(addMsg);
          // End session if amount of changes sent has been requested
          if (careAboutAmountOfChanges)
          {
            nChangesSent++;
            if (nChangesSent == nChangesSentLimit)
            {
              // Requested number of changes to send sent, end session
              debugInfo("Broker " + serverId + " writer reached " +
                nChangesSent + " changes limit");
              suspended.set(true);
              break;
            }
          }
        }
        if (!dbg2Written)
        {
          long endSessionTime = System.currentTimeMillis();
          debugInfo("Broker " + serverId +
            " writer stopping sending changes session at: " + endSessionTime +
            " (duration: " + (endSessionTime - startSessionTime) + " ms)");
          dbg1Written = false;
          dbg2Written = true;
        }
        // Mark session is finished
        if (startedNewSession)
        {
          sessionDone.set(true);
        }

        try
        {
          // Writer in pause, sleep a while to let other threads work
          synchronized(sleeper)
          {
            sleeper.wait(1000);
          }
        } catch (InterruptedException ex)
        {
          /* Don't care */
        }
      }
      debugInfo("Broker " + serverId + " writer thread is dying");
    }

    /** Stops the writer thread. */
    public void shutdown()
    {
      suspended.set(true); // If were working
      shutdown.set(true);
      synchronized (sleeper)
      {
        sleeper.notify();
      }
      try
      {
        join();
      } catch (InterruptedException ex)
      {
        /* Don't care */
      }

      // Stop reader if any
      StateMachineTest.shutdown(reader);
    }

    /** Suspends the writer thread. */
    public void pause()
    {
      if (isPaused())
      {
        return; // Already suspended
      }
      suspended.set(true);
      // Wait for all messages sent
      while (!sessionDone.get())
      {
        TestCaseUtils.sleep(200);
      }
    }

    /** Test if the writer is suspended. */
    public boolean isPaused()
    {
      return sessionDone.get();
    }

    /** Resumes the writer thread until it is paused. */
    public void follow()
    {
      sessionDone.set(false);
      suspended.set(false);
    }

    /**
     * Resumes the writer and suspends it after a given amount of changes has been
     * sent. If the writer was working it will be paused anyway after the given
     * amount of changes, starting from the current call time.
     * -> blocking call
     */
    public void followAndPause(int nChanges)
    {
      debugInfo("Requested broker writer " + serverId + " to write " + nChanges + " change(s).");
      pause(); // If however we were already working

      // Initialize counter system variables
      nChangesSent = 0;
      nChangesSentLimit = nChanges;
      careAboutAmountOfChanges = true;

      // Start session
      sessionDone.set(false);
      suspended.set(false);

      // Wait for all messages sent
      while (!sessionDone.get())
      {
        TestCaseUtils.sleep(1000);
      }
      careAboutAmountOfChanges = false;
    }

    private AddMsg createNextAddMsg()
    {
      String userEntryUUID = "11111111-1111-1111-1111-111111111111";
      long curId =  userId++;
      String userdn = "uid=user" + curId + "," + EXAMPLE_DN;
      String entryWithUUIDldif = "dn: " + userdn + "\n" + "objectClass: top\n" +
        "objectClass: person\n" + "objectClass: organizationalPerson\n" +
        "objectClass: inetOrgPerson\n" +
        "uid: user" + curId + "\n" +
        "homePhone: 951-245-7634\n" +
        "description: This is the description for Aaccf Amar.\n" + "st: NC\n" +
        "mobile: 027-085-0537\n" +
        "postalAddress: Aaccf Amar$17984 Thirteenth Street" +
        "$Rockford, NC  85762\n" + "mail: user.1@example.com\n" +
        "cn: Aaccf Amar\n" + "l: Rockford\n" + "pager: 508-763-4246\n" +
        "street: 17984 Thirteenth Street\n" + "telephoneNumber: 216-564-6748\n" +
        "employeeNumber: 1\n" + "sn: Amar\n" + "givenName: Aaccf\n" +
        "postalCode: 85762\n" + "userPassword: password\n" + "initials: AA\n" +
        "entryUUID: " + userEntryUUID + "\n";

      Entry personWithUUIDEntry = null;
      try
      {
        personWithUUIDEntry = TestCaseUtils.entryFromLdifString(
          entryWithUUIDldif);
      } catch (Exception e)
      {
        throw new RuntimeException(e);
      }

      // Create an update message to add an entry.
      return new AddMsg(gen.newCSN(),
        personWithUUIDEntry.getName(),
        userEntryUUID,
        null,
        personWithUUIDEntry.getObjectClassAttribute(),
        personWithUUIDEntry.getAttributes(), new ArrayList<Attribute>());
    }
  }

  /**
   * This simple reader just throws away the received
   * messages. It is used on a breaker we want to be able to send or read from some message
   * with (changes, entries (full update)...). Calling the receive method of the
   * broker allows to unblock the window mechanism and to send the desired messages.
   * Calling the updateWindowAfterReplay method allows to send when necessary the
   * window message to the RS to allow him send other messages he may want to send us.
   */
  private class BrokerReader extends Thread
  {
    private ReplicationBroker rb;
    private int serverId = -1;
    private boolean shutdown;
    private ReplicationMsg lastMsg;

    public BrokerReader(ReplicationBroker rb, int serverId)
    {
      super("BrokerReader for broker " + serverId);
      this.rb = rb;
      this.serverId = serverId;
      start();
    }

    /** Loop reading and throwing update messages. */
    @Override
    public void run()
    {
      while (!shutdown)
      {
        try
        {
          ReplicationMsg msg = rb.receive(); // Allow more messages to be sent by broker writer
          rb.updateWindowAfterReplay();  // Allow RS to send more messages to broker
          if (msg != null)
          {
            debugInfo("Broker " + serverId + " reader received: " + msg);
          }
          lastMsg = msg;
        } catch (SocketTimeoutException ex)
        {
          if (shutdown)
          {
            return;
          }
        }
      }
      debugInfo("Broker " + serverId + " reader thread is dying");
    }

    /** Returns last received message from reader When read, last value is cleared. */
    public ReplicationMsg getLastMsg()
    {
      ReplicationMsg toReturn = lastMsg;
      lastMsg = null;
      return toReturn;
    }

    /** Stops reader thread. */
    public void shutdown()
    {
      shutdown = true;

      try
      {
        join();
      } catch (InterruptedException ex)
      {
        /* Don't care */
      }
    }
  }

  /**
   * Waits until the domain status reaches the expected status.
   * @param domain The domain whose status we want to test
   * @param expectedStatus The expected domain status
   */
  private void waitUntilStatusEquals(final LDAPReplicationDomain domain, final ServerStatus expectedStatus) throws Exception
  {
    assertNotNull(domain);
    assertNotNull(expectedStatus);

    TestTimer timer = new TestTimer.Builder()
      .maxSleep(30, SECONDS)
      .sleepTimes(500, MILLISECONDS)
      .toTimer();
    timer.repeatUntilSuccess(new Callable<Void>()
    {
      @Override
      public Void call() throws Exception
      {
        assertEquals(domain.getStatus(), expectedStatus);
        return null;
      }
    });
  }
}
