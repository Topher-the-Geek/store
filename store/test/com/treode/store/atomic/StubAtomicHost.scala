package com.treode.store.atomic

import java.nio.file.Paths
import scala.util.Random

import com.treode.async.{Async, Callback}
import com.treode.async.stubs.StubScheduler
import com.treode.async.io.stubs.StubFile
import com.treode.async.stubs.implicits._
import com.treode.cluster.{Cluster, HostId}
import com.treode.disk.stubs.{StubDisks, StubDiskDrive}
import com.treode.store._
import com.treode.store.catalog.Catalogs
import com.treode.store.paxos.Paxos
import org.scalatest.Assertions

import Assertions.assertResult
import AtomicTestTools._
import Callback.ignore

private class StubAtomicHost (id: HostId) (implicit kit: StoreTestKit)
extends StubStoreHost (id) {
  import kit._

  implicit val library = new Library

  implicit val storeConfig = TestStoreConfig()
  implicit val recovery = StubDisks.recover()
  implicit val _catalogs = Catalogs.recover()
  val _paxos = Paxos.recover()
  val _atomic = AtomicKit.recover()

  val diskDrive = new StubDiskDrive

  val _launch =
    for {
      launch <- recovery.attach (diskDrive)
      catalogs <- _catalogs.launch (launch)
      paxos <- _paxos.launch (launch)
      atomic <- _atomic.launch (launch, paxos) .map (_.asInstanceOf [AtomicKit])
    } yield {
      launch.launch()
      (launch.disks, catalogs, atomic)
    }

  val captor = _launch.capture()
  scheduler.runTasks()
  while (!captor.wasInvoked)
    Thread.sleep (10)
  implicit val (disks, catalogs, atomic) = captor.passed

  val librarian = new Librarian (atomic.rebalance _)

  cluster.startup()

  def setAtlas (cohorts: Cohort*) {
    val version = library.atlas.version + 1
    val atlas = Atlas (cohorts.toArray, version)
    library.atlas = atlas
    library.residents = atlas.residents (localId)
  }

  def issueAtlas (cohorts: Cohort*) {
    val version = library.atlas.version + 1
    val atlas = Atlas (cohorts.toArray, version)
    library.atlas = atlas
    library.residents = atlas.residents (localId)
    catalogs.issue (Atlas.catalog) (version, atlas) .pass
  }

  def writer (xid: TxId) = atomic.writers.get (xid)

  def read (rt: TxClock, ops: ReadOp*): Async [Seq [Value]] =
    atomic.read (rt, ops:_*)

  def write (xid: TxId, ct: TxClock, ops: WriteOp*): Async [TxClock] =
    atomic.write (xid, ct, ops:_*)

  def scan (table: TableId, key: Bytes, time: TxClock): CellIterator =
    atomic.scan (table, key,  time)

  def putCells (id: TableId, cs: Cell*): Unit =
    atomic.tables.receive (id, cs) .pass

  def expectAtlas (atlas: Atlas) {
    assertResult (atlas) (library.atlas)
    assertResult (librarian.issued) (atlas.version)
    assert (librarian.receipts forall (_._2 == atlas.version))
  }

  def expectCells (id: TableId) (cs: Cell*) {
    val t = atomic.tables.tables.get (id)
    assertResult (cs) (t .iterator (library.residents) .toSeq)
  }}
