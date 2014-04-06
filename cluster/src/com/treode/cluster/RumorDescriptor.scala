package com.treode.cluster

import com.treode.pickle.{Pickler, Picklers}

class RumorDescriptor [M] (val id: RumorId, val pmsg: Pickler [M]) {

  def listen (f: (M, Peer) => Any) (implicit c: Cluster): Unit =
    c.listen (this) (f)

  def spread (msg: M) (implicit c: Cluster): Unit =
    c.spread (this) (msg)

  override def toString = s"RumorDescriptor($id)"
}

object RumorDescriptor {

  def apply [M] (id: RumorId, pval: Pickler [M]): RumorDescriptor [M] =
    new RumorDescriptor (id, pval)
}
