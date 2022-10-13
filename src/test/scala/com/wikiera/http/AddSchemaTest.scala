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

class AddSchemaTest extends AsyncWordSpec with AsyncIOSpec with Matchers with OptionValues with Fixtures {

  private val server: Http4sServerInterpreter[IO] = Http4sServerInterpreter[IO]()

  val nonExistingSchema: SchemaId => IO[None.type] = _ => IO.pure(None)
  "POST schema/{schemaId}" should {
    "add schema when it's proper JSON" in {

      val request = Request[IO](method = POST, uri = uri"/schema/new_id").withEntity(properSchema)
      val addSchemaRoute = AddSchema(server)(nonExistingSchema)(input =>
        IO.pure {
          input.id shouldBe SchemaId("new_id")
          input.schema shouldBe existingSchema
        }.void
      )

      addSchemaRoute.run(request).value.flatMap { response =>
        response.value.status.code shouldBe 201
        response.value.json
          .map(_.as[SuccessResponse].toOption.value shouldBe SuccessResponse(Actions.UploadSchema, "new_id"))
      }
    }
    "return 400 when schema is not proper JSON" in {
      val improperSchema = "bad_schema"
      val request        = Request[IO](method = POST, uri = uri"/schema/non_existing_id").withEntity(improperSchema)
      val invalidSchema = ErrorResponse
        .invalidJson(Actions.UploadSchema, nonExistingId)
        ._2

      val addSchemaRoute = AddSchema(server)(nonExistingSchema)(input =>
        IO.pure {
          input.id shouldBe SchemaId(nonExistingId)
        }.void
      )

      addSchemaRoute.run(request).value.flatMap { response =>
        response.value.status.code shouldBe 400
        response.value.json.map(
          _.as[ErrorResponse].toOption.value shouldBe invalidSchema
        )
      }
    }
    "return 409 when schema already exists" in {
      val request = Request[IO](method = POST, uri = uri"/schema/non_existing_id").withEntity(properSchema)
      val schemaAlreadyExists = ErrorResponse
        .schemaAlreadyExists(nonExistingId)
        ._2

      val addSchemaRoute = AddSchema(server)(_ => IO.pure(Some(existingSchema)))(_ => IO().void)

      addSchemaRoute.run(request).value.flatMap { response =>
        response.value.status.code shouldBe 409
        response.value.json.map(
          _.as[ErrorResponse].toOption.value shouldBe schemaAlreadyExists
        )
      }
    }
  }
}
