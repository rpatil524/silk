package org.silkframework.plugins.dataset.json

import java.io.File

import org.scalatest.{FlatSpec, Matchers}
import org.silkframework.config.Prefixes
import org.silkframework.dataset.{DataSource, TypedProperty}
import org.silkframework.entity.{Entity, EntitySchema, StringValueType}
import org.silkframework.entity.paths.{DirectionalPathOperator, ForwardOperator, PathOperator, TypedPath, UntypedPath}
import org.silkframework.execution.EntityHolder
import org.silkframework.runtime.activity.UserContext
import org.silkframework.runtime.resource.{ClasspathResourceLoader, FileResource, InMemoryResourceManager, WritableResource}
import org.silkframework.util.Uri

class JsonSinkTest extends FlatSpec with Matchers {

  implicit val userContext: UserContext = UserContext.Empty
  private val resources = ClasspathResourceLoader("org/silkframework/plugins/dataset/json/")
  private val tempFile = File.createTempFile("json-write-test-1", ".json")
  tempFile.deleteOnExit()
  implicit val prefixes: Prefixes = Prefixes.empty


  it should "read and write from json datasources" in {
    val inputEntites = getEntities()
    val sink = new JsonSink(FileResource(tempFile))

    val typedProps: Seq[TypedProperty] = inputEntites.head.schema.typedPaths.map(_.property.get)
    sink.openTable("typeUri", typedProps)
    var uri: Int = 0
    for (entity <- inputEntites) {
      sink.writeEntity(Uri(uri.toString), entity.values)
      uri += 1
    }
    sink.closeTable()
    sink.close()

    val source = scala.io.Source.fromFile(tempFile)
    val lines = try source.mkString finally source.close()

    inputEntites.size shouldBe 2
    tempFile.exists() shouldBe(true)
    lines shouldBe("{\"Entity\":[{\"value\":\"val\"},{\"value\":\"val2\"}]}")
  }

  private val jsonComplex =
    """{ "object": {"blah": 3},
      |  "objects": [
      |    {"value":"val", "nestedObject": {"nestedValue": "nested"}},
      |    null,
      |    {"value": "val2", "boolean": true, "int": 3, "float": 3.41, "emptyObject": {}, "emptyArray": [], "array": [1,2,3], "objectArray": [{"v": 2}]}
      |  ],
      |  "values": ["arr1", "arr2"]
      |}""".stripMargin


  def getEntities(): Traversable[Entity] = {
    val source: DataSource = jsonSource(jsonComplex)
    source.retrieve(EntitySchema("objects", typedPaths = IndexedSeq(UntypedPath.parse("value").asStringTypedPath))).entities
  }

  private def jsonSource(json: String): JsonSource = {
    val jsonResource = InMemoryResourceManager().get("temp.json")
    jsonResource.writeString(json)
    val source = JsonDataset(jsonResource).source
    source.asInstanceOf[JsonSource]
  }
}
