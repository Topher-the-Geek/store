package com.treode.async

import org.scalatest.FlatSpec

class SeqLatchSpec extends FlatSpec {

  class DistinguishedException extends Exception

  "The SeqLatch" should "release immediately for count==0" in {
    val cb = CallbackCaptor [Seq [Int]]
    val ltch = Callback.seq (0, cb)
    expectResult (Seq [Int] ()) (cb.passed)
  }

  it should "reject extra releases" in {
    val cb = CallbackCaptor [Seq [Int]]
    val ltch = Callback.seq (0, cb)
    expectResult (Seq [Int] ()) (cb.passed)
    intercept [Exception] (ltch (1))
  }

  it should "release after one pass for count==1" in {
    val cb = CallbackCaptor [Seq [Int]]
    val ltch = Callback.seq (1, cb)
    cb.expectNotInvoked()
    ltch (1)
    expectResult (Seq (1)) (cb.passed)
  }

  it should "release after one fail for count==1" in {
    val cb = CallbackCaptor [Seq [Int]]
    val ltch = Callback.seq (1, cb)
    cb.expectNotInvoked()
    ltch.fail (new DistinguishedException)
    cb.failed [DistinguishedException]
  }

  it should "release after two passes for count==2" in {
    val cb = CallbackCaptor [Seq [Int]]
    val ltch = Callback.seq (2, cb)
    cb.expectNotInvoked()
    ltch (1)
    cb.expectNotInvoked()
    ltch (2)
    expectResult (Seq (1, 2)) (cb.passed)
  }

  it should "release after two reversed passes for count==2" in {
    val cb = CallbackCaptor [Seq [Int]]
    val ltch = Callback.seq (2, cb)
    cb.expectNotInvoked()
    ltch (2)
    cb.expectNotInvoked()
    ltch (1)
    expectResult (Seq (2, 1)) (cb.passed)
  }

  it should "release after a pass and a fail for count==2" in {
    val cb = CallbackCaptor [Seq [Int]]
    val ltch = Callback.seq (2, cb)
    cb.expectNotInvoked()
    ltch (1)
    cb.expectNotInvoked()
    ltch.fail (new DistinguishedException)
    cb.failed [DistinguishedException]
  }

  it should "release after two fails for count==2" in {
    val cb = CallbackCaptor [Seq [Int]]
    val ltch = Callback.seq (2, cb)
    cb.expectNotInvoked()
    ltch.fail (new Exception)
    cb.expectNotInvoked()
    ltch.fail (new Exception)
    cb.failed [MultiException]
  }}
