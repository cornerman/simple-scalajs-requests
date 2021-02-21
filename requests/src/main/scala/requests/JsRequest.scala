package requests

import java.nio.ByteBuffer
import scala.concurrent.{Future, ExecutionContext}
import scala.scalajs.js

trait JsRequest[T] {
  def post(url: String, payload: T)(implicit ec: ExecutionContext): Future[T]
}

object JsRequestBytes extends JsRequest[ByteBuffer] {
  import scala.scalajs.js.typedarray._
  import scala.scalajs.js.typedarray.TypedArrayBufferOps._
  import org.scalajs.dom.experimental.{Fetch, RequestInit, HttpMethod, ReadableStreamReader}

  def post(url: String, payload: ByteBuffer)(implicit ec: ExecutionContext): Future[ByteBuffer] = for {
      result <- fetchData(url, method = HttpMethod.POST, payload = payload)
      buffers <- readBytes(result.body.getReader())
    } yield combineByteBuffers(buffers)

  private def fetchData(url: String, method: HttpMethod, payload: js.UndefOr[ByteBuffer]) =
    Fetch.fetch(
      url,
      js.Dynamic.literal(method = method, body = payload.map(_.typedArray())).asInstanceOf[RequestInit]
    ).toFuture

  private def readBytes(reader: ReadableStreamReader[Uint8Array])(implicit ec: ExecutionContext): Future[List[ByteBuffer]] =
    reader.read().toFuture.flatMap { chunk =>
      if (js.typeOf(chunk.value) == "undefined") Future.successful(Nil)
      else {
        val buffer = TypedArrayBuffer.wrap(chunk.value.buffer)
        if (chunk.done) Future.successful(buffer :: Nil)
        else readBytes(reader).map(buffer :: _)
      }
    }

  private def combineByteBuffers(buffers: List[ByteBuffer]): ByteBuffer = {
    val result = ByteBuffer.allocate(buffers.map(_.limit()).sum);
    buffers.foreach(result.put)
    result.rewind()
    result
  }
}

object JsRequestJson extends JsRequest[String] {
  import org.scalajs.dom.experimental.{Fetch, RequestInit, HttpMethod}

  def post(url: String, payload: String)(implicit ec: ExecutionContext): Future[String] = for {
      result <- fetchData(url, method = HttpMethod.POST, payload = payload)
      json <- result.json().toFuture
    } yield json.asInstanceOf[String]

  private def fetchData(url: String, method: HttpMethod, payload: js.UndefOr[String]) =
    Fetch.fetch(
      url,
      js.Dynamic.literal(method = method, body = payload).asInstanceOf[RequestInit]
    ).toFuture
}
