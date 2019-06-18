package org.silkframework.serialization.json

import org.silkframework.runtime.serialization.{ReadContext, SerializationFormat, WriteContext}
import play.api.libs.json.{JsValue, Json}

import scala.reflect.ClassTag

/**
  * JSON serialization format.
  */
abstract class JsonFormat[T: ClassTag] extends SerializationFormat[T, JsValue] {

  /**
    * The types that are generated by this task.
    */
  def typeNames: Set[String] = Set.empty

  /**
    * The MIME types that can be formatted.
    */
  def mimeTypes: Set[String] = Set(JsonFormat.MIME_TYPE_APPLICATION)

  /**
    * Formats a JSON value as string.
    */
  def toString(value: T, mimeType: String)(implicit writeContext: WriteContext[JsValue]): String = {
    Json.prettyPrint(write(value))
  }

  /**
    * Reads a value from a JSON string.
    */
  def fromString(value: String, mimeType: String)(implicit readContext: ReadContext): T = {
    read(parse(value, mimeType))
  }

  /**
    * Read Serialization format from string
    */
  override def parse(value: String, mimeType: String): JsValue = Json.parse(value)

  override def toString(values: Iterable[T], mimeType: String, containerName: Option[String])(implicit writeContext: WriteContext[JsValue]): String = {
    val sb = new StringBuilder()
    sb.append(s"[")
    for((v, idx) <- values.zipWithIndex) {
      if(idx > 0) {
        sb.append(",")
      }
      sb.append(toString(v, mimeType))
    }
    sb.append(s"]")
    sb.toString()
  }
}

abstract class WriteOnlyJsonFormat[T: ClassTag] extends JsonFormat[T] {

  override final def read(value: JsValue)(implicit readContext: ReadContext): T = {
    throw new UnsupportedOperationException(s"Parsing values of type ${implicitly[ClassTag[T]].runtimeClass.getSimpleName} from Json is not supported at the moment")
  }

}

object JsonFormat{
  val MIME_TYPE_APPLICATION = "application/json"
}
