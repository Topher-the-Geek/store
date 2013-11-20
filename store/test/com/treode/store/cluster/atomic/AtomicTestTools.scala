package com.treode.store.cluster.atomic

import com.treode.store.Bytes

trait AtomicTestTools {

  val Zero = Bytes (0)
  val One = Bytes (1)
  val Two = Bytes (2)

  implicit class TestableWriteDeputy (s: WriteDeputy) {

    def isRestoring = classOf [WriteDeputy#Restoring] .isInstance (s.state)
    def isOpen = classOf [WriteDeputy#Open] .isInstance (s.state)
    def isPrepared = classOf [WriteDeputy#Prepared] .isInstance (s.state)
    def isCommitted = classOf [WriteDeputy#Committed] .isInstance (s.state)
    def isAborted = classOf [WriteDeputy#Aborted] .isInstance (s.state)
    def isShutdown = classOf [WriteDeputy#Shutdown] .isInstance (s.state)
  }}
