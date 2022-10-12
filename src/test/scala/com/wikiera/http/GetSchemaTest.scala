package com.wikiera.http

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.wikiera.http.Inputs.SchemaId
import com.wikiera.http.Outputs.ErrorResponse
import org.http4s.Method.GET
import org.http4s.Request
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import sttp.tapir.server.http4s.Http4sServerInterpreter
import org.http4s.syntax.all._
import org.http4s.circe._
import io.circe.parser._
import io.circe.generic.auto._

class GetSchemaTest extends AsyncWordSpec with AsyncIOSpec with Matchers with OptionValues {

  private val server: Http4sServerInterpreter[IO] = Http4sServerInterpreter[IO]()

  "GET schema/{schemaId}" should {
    "return schema details when it exists" in {
      val existingSchema =
        parse("""
                |{
                |  "$schema": "http://json-schema.org/draft-04/schema#",
                |  "type": "object",
                |  "properties": {
                |    "source": {
                |      "type": "string"
                |    },
                |    "destination": {
                |      "type": "string"
                |    },
                |    "timeout": {
                |      "type": "integer",
                |      "minimum": 0,
                |      "maximum": 32767
                |    },
                |    "chunks": {
                |      "type": "object",
                |      "properties": {
                |        "size": {
                |          "type": "integer"
                |        },
                |        "number": {
                |          "type": "integer"
                |        }
                |      },
                |      "required": ["size"]
                |    }
                |  },
                |  "required": ["source", "destination"]
                |}
                |""".stripMargin).toOption.get

      val request = Request[IO](method = GET, uri = uri"/schema/existing_id")

      val getSchemaRoute = GetSchema(server)(id =>
        IO.pure {
          id shouldBe SchemaId("existing_id")
          Some(existingSchema)
        }
      )

      getSchemaRoute.run(request).value.flatMap { response =>
        response.value.status.code shouldBe 200
        response.value.json.map(_ shouldBe existingSchema)
      }
    }
    "return 400 when schema is not found" in {
      val nonExistingId: String = "non_existing_id"
      val request               = Request[IO](method = GET, uri = uri"/schema/non_existing_id")
      val schemaNotFound = ErrorResponse
        .schemaNotFound(Actions.DownloadSchema, nonExistingId)

      val getSchemaRoute = GetSchema(server)(id =>
        IO.pure {
          id shouldBe SchemaId(nonExistingId)
          None
        }
      )

      getSchemaRoute.run(request).value.flatMap { response =>
        response.value.status.code shouldBe 400
        response.value.json.map(
          _.as[ErrorResponse].toOption.value shouldBe schemaNotFound
        )
      }
    }
  }
}
