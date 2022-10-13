package com.wikiera.http

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.wikiera.http.Inputs.SchemaId
import com.wikiera.http.Outputs.ErrorResponse
import io.circe.generic.auto._
import org.http4s.Method.GET
import org.http4s.Request
import org.http4s.circe._
import org.http4s.syntax.all._
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import sttp.tapir.server.http4s.Http4sServerInterpreter

class GetSchemaTest extends AsyncWordSpec with AsyncIOSpec with Matchers with OptionValues with Fixtures {

  private val server: Http4sServerInterpreter[IO] = Http4sServerInterpreter[IO]()

  "GET schema/{schemaId}" should {
    "return schema details when it exists" in {
      val request = Request[IO](method = GET, uri = uri"/schema/existing_id")

      val getSchemaRoute = GetSchema(server)(id =>
        IO.pure {
          id shouldBe SchemaId(existingId)
          Some(existingSchema)
        }
      )

      getSchemaRoute.run(request).value.flatMap { response =>
        response.value.status.code shouldBe 200
        response.value.json.map(_ shouldBe existingSchema)
      }
    }
    "return 404 when schema is not found" in {
      val request = Request[IO](method = GET, uri = uri"/schema/non_existing_id")
      val schemaNotFound = ErrorResponse
        .schemaNotFound(Actions.DownloadSchema, nonExistingId)
        ._2

      val getSchemaRoute = GetSchema(server)(id =>
        IO.pure {
          id shouldBe SchemaId(nonExistingId)
          None
        }
      )

      getSchemaRoute.run(request).value.flatMap { response =>
        response.value.status.code shouldBe 404
        response.value.json.map(
          _.as[ErrorResponse].toOption.value shouldBe schemaNotFound
        )
      }
    }
  }
}
