package com.wikiera.http

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.wikiera.http.Inputs.SchemaId
import com.wikiera.http.Outputs.{ErrorResponse, SuccessResponse}
import io.circe.generic.auto._
import org.http4s.Method.POST
import org.http4s.Request
import org.http4s.circe._
import org.http4s.syntax.all._
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import sttp.tapir.server.http4s.Http4sServerInterpreter

class AddSchemaTest extends AsyncWordSpec with AsyncIOSpec with Matchers with OptionValues {

  private val server: Http4sServerInterpreter[IO] = Http4sServerInterpreter[IO]()

  "POST schema/{schemaId}" should {
    "create schema details when it's proper JSON" in {
      val properSchema =
        """
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
                |""".stripMargin

      val request = Request[IO](method = POST, uri = uri"/schema/new_id").withEntity(properSchema)

      val addSchemaRoute = AddSchema(server)(input =>
        IO.pure {
          input.id shouldBe SchemaId("new_id")
          input.body shouldBe properSchema
        }.void
      )

      addSchemaRoute.run(request).value.flatMap { response =>
        response.value.status.code shouldBe 201
        response.value.json
          .map(_.as[SuccessResponse].toOption.value shouldBe SuccessResponse(Actions.UploadSchema, "new_id"))
      }
    }
    "return 400 when schema is not proper JSON" in {
      val improperSchema        = "bad_schema"
      val nonExistingId: String = "non_existing_id"
      val request = Request[IO](method = POST, uri = uri"/schema/non_existing_id").withEntity(improperSchema)
      val invalidSchema = ErrorResponse
        .invalidJson(Actions.UploadSchema, nonExistingId)

      val addSchemaRoute = AddSchema(server)(input =>
        IO.pure {
          input.id shouldBe SchemaId(nonExistingId)
          input.body shouldBe improperSchema
        }.void
      )

      addSchemaRoute.run(request).value.flatMap { response =>
        response.value.status.code shouldBe 400
        response.value.json.map(
          _.as[ErrorResponse].toOption.value shouldBe invalidSchema
        )
      }
    }
  }
}
