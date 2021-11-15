package requests

import java.nio.ByteBuffer
import scala.concurrent.{Future, ExecutionContext}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

import org.scalajs.dom.{BodyInit, Fetch, RequestInit, HttpMethod, ReadableStreamReader, Response, Headers}

trait JsRequest[T] {
  def request(method: HttpMethod, url: String, payload: Option[T], header: Map[String, String] = Map.empty)(implicit ec: ExecutionContext): Future[T]

  final def fetchData(method: HttpMethod, url: String, payload: js.UndefOr[BodyInit] = js.undefined, headers: Map[String, String] = Map.empty): Future[Response] =
    Fetch.fetch(url, js.Dynamic.literal("method" -> method, "body" -> payload.asInstanceOf[js.Any], "headers" -> new Headers(headers.map { case (k, v) => js.Array(k,v) }.toJSArray)).asInstanceOf[RequestInit]).toFuture

  final def get(url: String, header: Map[String, String] = Map.empty)(implicit ec: ExecutionContext): Future[T] = request(HttpMethod.GET, url, None, header)
  final def post(url: String, payload: T, header: Map[String, String] = Map.empty)(implicit ec: ExecutionContext): Future[T] = request(HttpMethod.GET, url, Some(payload), header)
}

object JsRequestBytes extends JsRequest[ByteBuffer] {
  import scala.scalajs.js.typedarray._
  import scala.scalajs.js.typedarray.TypedArrayBufferOps._

  def request(method: HttpMethod, url: String, payload: Option[ByteBuffer], header: Map[String, String] = Map.empty)(implicit ec: ExecutionContext): Future[ByteBuffer] = for {
      result <- fetchData(method, url, payload.map(_.typedArray().asInstanceOf[BodyInit]).orUndefined, header)
      buffers <- readBytes(result.body.getReader())
    } yield combineByteBuffers(buffers)

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

  def request(method: HttpMethod, url: String, payload: Option[String], header: Map[String, String] = Map.empty)(implicit ec: ExecutionContext): Future[String] = for {
      result <- fetchData(method, url, payload.orUndefined, header)
      json <- result.json().toFuture
    } yield json.asInstanceOf[String]
}

object JsRequestText extends JsRequest[String] {

  def request(method: HttpMethod, url: String, payload: Option[String], header: Map[String, String] = Map.empty)(implicit ec: ExecutionContext): Future[String] = for {
      result <- fetchData(method, url, payload.orUndefined, header)
      text <- result.text().toFuture
    } yield text.asInstanceOf[String]
}
