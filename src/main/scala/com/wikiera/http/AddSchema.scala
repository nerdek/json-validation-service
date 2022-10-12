package com.wikiera.http

import cats.effect.IO
import com.wikiera.http.Actions.{DownloadSchema, UploadSchema}
import com.wikiera.http.Endpoints.{addSchemaEndpoint, getSchemaEndpoint}
import com.wikiera.http.Inputs.{SchemaId, SchemaInput}
import com.wikiera.http.Outputs.ErrorResponse.{invalidJson, schemaNotFound}
import com.wikiera.http.Outputs.SuccessResponse
import io.circe.Json
import io.circe.parser.parse
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter

object AddSchema {
  def apply(server: Http4sServerInterpreter[IO])(addSchema: SchemaInput => IO[Unit]): HttpRoutes[IO] =
    server.toRoutes {
      addSchemaEndpoint.serverLogic[IO] { input =>
        parse(input.body) match {
          case Left(_) => IO(Left(invalidJson(UploadSchema, input.id.id)))
          case Right(_) =>
            addSchema(input) >>
              IO(Right(SuccessResponse(UploadSchema, input.id.id)))
        }
      }
    }
}
