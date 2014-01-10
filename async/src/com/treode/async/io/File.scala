package com.treode.async.io

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.{OpenOption, Path}
import java.nio.file.attribute.FileAttribute
import java.util.concurrent.ExecutorService
import scala.collection.JavaConversions._

import com.treode.async.Callback
import com.treode.buffer.PagedBuffer

/** A file that has useful behavior (flush/fill) and that can be mocked. */
class File private [io] (file: AsynchronousFileChannel) {

  /** For testing mocks only. */
  def this() = this (null)

  private class FileFiller (input: PagedBuffer, pos: Long, len: Int, cb: Callback [Unit])
  extends Callback [Int] {

    private [this] var bytebuf = input.buffer (input.writePos, input.writeableBytes)
    private [this] var _pos = pos

    def fill() {
      if (len <= input.readableBytes)
        cb()
      else {
        if (bytebuf.remaining == 0)
          bytebuf = input.buffer (input.writePos, input.writeableBytes)
        file.read (bytebuf, _pos, this, Callback.IntHandler)
      }}

    def pass (result: Int) {
      if (result < 0) {
        cb.fail (new Exception ("End of file reached."))
      } else {
        input.writePos = input.writePos + result
        _pos += result
        fill()
      }}

    def fail (t: Throwable) = cb.fail (t)
  }

  def fill (input: PagedBuffer, pos: Long, len: Int, cb: Callback [Unit]): Unit =
    try {
      if (len <= input.readableBytes) {
        cb()
      } else {
        input.capacity (input.readPos + len)
        new FileFiller (input, pos, len, cb) .fill()
      }
    } catch {
      case t: Throwable => cb.fail (t)
    }

  private class FileFlusher (output: PagedBuffer, pos: Long, cb: Callback [Unit])
  extends Callback [Int] {

    private [this] var bytebuf = output.buffer (output.readPos, output.readableBytes)
    private [this] var _pos = pos

    def flush() {
      if (output.readableBytes == 0) {
        //buffer.release()
        cb()
      } else {
        if (bytebuf.remaining == 0)
          bytebuf = output.buffer (output.readPos, output.readableBytes)
        file.write (bytebuf, _pos, this, Callback.IntHandler)
      }}

    def pass (result: Int) {
      if (result < 0) {
        cb.fail (new Exception ("File write failed."))
      } else {
        output.readPos = output.readPos + result
        _pos += result
        flush()
      }}

    def fail (t: Throwable) = cb.fail (t)
  }

  def flush (output: PagedBuffer, pos: Long, cb: Callback [Unit]): Unit =
    try {
      new FileFlusher (output, pos, cb) .flush()
    } catch {
      case t: Throwable => cb.fail (t)
    }

  def close(): Unit = file.close()
}

object File {

  def open (path: Path, opts: Set [OpenOption], attrs: Set [FileAttribute [Any]], exec: ExecutorService): File =
    new File (AsynchronousFileChannel.open (path, opts, exec, attrs.toArray: _*))
}