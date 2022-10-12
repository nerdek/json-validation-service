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

class ValidateSchemaTest extends AsyncWordSpec with AsyncIOSpec with Matchers with OptionValues with Fixtures {

  private val server: Http4sServerInterpreter[IO] = Http4sServerInterpreter[IO]()

  "POST validate/{schemaId}" should {
    "return success when schema exists and request body is aligned to it" in {
      val request = Request[IO](method = POST, uri = uri"/validate/existing_id").withEntity(properJson)

      val validateSchemaRoute = ValidateSchema(server)(id =>
        IO.pure {
          id shouldBe SchemaId("existing_id")
          Some(existingSchema)
        }
      )

      validateSchemaRoute.run(request).value.flatMap { response =>
        response.value.status.code shouldBe 200
        response.value.json
          .map(_.as[SuccessResponse].toOption.value shouldBe SuccessResponse(Actions.Validate, "existing_id"))
      }
    }
    "return 400 when schema is not found" in {
      val request        = Request[IO](method = POST, uri = uri"/validate/non_existing_id").withEntity(properJson)
      val schemaNotFound = ErrorResponse.schemaNotFound(Actions.Validate, nonExistingId)

      val validateSchemaRoute = ValidateSchema(server)(id =>
        IO.pure {
          id shouldBe SchemaId(nonExistingId)
          None
        }
      )

      validateSchemaRoute.run(request).value.flatMap { response =>
        response.value.status.code shouldBe 400
        response.value.json.map(
          _.as[ErrorResponse].toOption.value shouldBe schemaNotFound
        )
      }
    }
    "return 400 when sent JSON is not correct" in {
      val request     = Request[IO](method = POST, uri = uri"/validate/existing_id").withEntity("bad_json")
      val invalidJson = ErrorResponse.invalidJson(Actions.Validate, existingId)

      val validateSchemaRoute = ValidateSchema(server)(id =>
        IO.pure {
          id shouldBe SchemaId("existing_id")
          Some(existingSchema)
        }
      )

      validateSchemaRoute.run(request).value.flatMap { response =>
        response.value.status.code shouldBe 400
        response.value.json.map(
          _.as[ErrorResponse].toOption.value shouldBe invalidJson
        )
      }
    }
    "return 400 when JSON is not aligned to schema" in {
      val request = Request[IO](method = POST, uri = uri"/validate/existing_id").withEntity(jsonNotAlignedWithSchema)

      val validateSchemaRoute = ValidateSchema(server)(id =>
        IO.pure {
          id shouldBe SchemaId("existing_id")
          Some(existingSchema)
        }
      )

      validateSchemaRoute.run(request).value.flatMap { response =>
        response.value.status.code shouldBe 400
        val error = response.value.json.map(_.as[ErrorResponse].toOption.value)
        error.map(_.id shouldBe "existing_id")
        error.map(_.status shouldBe Actions.Validate)
        error.map(_.message should not be empty)
      }
    }
  }
}
