/*
 * Copyright 2014 Treode, Inc.
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

package com.treode.store

import com.treode.pickle.Pickler

class CatalogDescriptor [C] (val id: CatalogId, val pcat: Pickler [C]) {

  def listen (f: C => Any) (implicit store: Store.Controller): Unit =
    store.listen (this) (f)

  def issue (version: Int, cat: C) (implicit store: Store.Controller): Unit =
    store.issue (this) (version, cat)

  override def toString = s"CatalogDescriptor($id)"
}

object CatalogDescriptor {

  def apply [M] (id: CatalogId, pval: Pickler [M]): CatalogDescriptor [M] =
    new CatalogDescriptor (id, pval)
}
