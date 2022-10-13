package com.wikiera.http

import cats.effect.IO
import com.wikiera.http.Actions.UploadSchema
import com.wikiera.http.Endpoints.addSchemaEndpoint
import com.wikiera.http.Inputs.{JsonSchema, SchemaId, SchemaInput}
import com.wikiera.http.Outputs.ErrorResponse.{invalidJson, schemaAlreadyExists}
import com.wikiera.http.Outputs.SuccessResponse
import io.circe.Json
import io.circe.parser.parse
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter

object AddSchema {
  def apply(
      server: Http4sServerInterpreter[IO]
  )(getSchema: SchemaId => IO[Option[Json]])(addSchema: JsonSchema => IO[Unit]): HttpRoutes[IO] =
    server.toRoutes {
      addSchemaEndpoint.serverLogic[IO] { input =>
        parse(input.body) match {
          case Left(_) => IO(Left(invalidJson(UploadSchema, input.id.id)))
          case Right(parsed) =>
            getSchema(input.id).flatMap {
              case Some(_) => IO(Left(schemaAlreadyExists(input.id.id)))
              case None =>
                addSchema(JsonSchema(input.id, parsed)) >>
                  IO(Right(SuccessResponse(UploadSchema, input.id.id)))
            }
        }
      }
    }
}
