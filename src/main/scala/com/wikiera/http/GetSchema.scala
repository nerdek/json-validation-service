package com.wikiera.http

import cats.effect.IO
import com.wikiera.http.Actions.DownloadSchema
import com.wikiera.http.Endpoints.getSchemaEndpoint
import com.wikiera.http.Inputs.SchemaId
import com.wikiera.http.Outputs.ErrorResponse.schemaNotFound
import io.circe.Json
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter

object GetSchema {
  def apply(server: Http4sServerInterpreter[IO])(getSchema: SchemaId => IO[Option[Json]]): HttpRoutes[IO] =
    server.toRoutes {
      getSchemaEndpoint.serverLogic[IO] { input =>
        getSchema(input).map {
          case Some(value) => Right(value)
          case None        => Left(schemaNotFound(DownloadSchema, input.id))
        }
      }
    }
}
