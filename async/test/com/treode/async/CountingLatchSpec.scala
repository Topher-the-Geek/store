package com.treode.async

import org.scalatest.FlatSpec

class CountingLatchSpec extends FlatSpec {

  class DistinguishedException extends Exception

  "The CountingLatchSpec" should "release immediately for count==0" in {
    val cb = CallbackCaptor [Unit]
    val ltch = Callback.latch (0, cb)
    cb.passed
  }

  it should "reject extra releases" in {
    val cb = CallbackCaptor [Unit]
    val ltch = Callback.latch (0, cb)
    cb.passed
    intercept [Exception] (ltch (0))
  }

  it should "release after one pass for count==1" in {
    val cb = CallbackCaptor [Unit]
    val ltch = Callback.latch (1, cb)
    cb.expectNotInvoked()
    ltch (0)
    cb.passed
  }

  it should "release after one fail for count==1" in {
    val cb = CallbackCaptor [Unit]
    val ltch = Callback.latch (1, cb)
    cb.expectNotInvoked()
    ltch.fail (new DistinguishedException)
    cb.failed [DistinguishedException]
  }

  it should "release after two passes for count==2" in {
    val cb = CallbackCaptor [Unit]
    val ltch = Callback.latch (2, cb)
    cb.expectNotInvoked()
    ltch (0)
    cb.expectNotInvoked()
    ltch (0)
    cb.passed
  }

  it should "release after a pass and a fail for count==2" in {
    val cb = CallbackCaptor [Unit]
    val ltch = Callback.latch (2, cb)
    cb.expectNotInvoked()
    ltch (0)
    cb.expectNotInvoked()
    ltch.fail (new DistinguishedException)
    cb.failed [DistinguishedException]
  }

  it should "release after two fails for count==2" in {
    val cb = CallbackCaptor [Unit]
    val ltch = Callback.latch (2, cb)
    cb.expectNotInvoked()
    ltch.fail (new Exception)
    cb.expectNotInvoked()
    ltch.fail (new Exception)
    cb.failed [MultiException]
  }}
