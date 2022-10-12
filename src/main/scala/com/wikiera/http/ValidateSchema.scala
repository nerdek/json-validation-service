package com.wikiera.http

import cats.effect.IO
import com.wikiera.http.Endpoints.validateSchemaEndpoint
import com.wikiera.http.Inputs.SchemaId
import com.wikiera.http.Outputs.ErrorResponse.{invalidJson, schemaNotFound}
import com.wikiera.http.Outputs.{ErrorResponse, SuccessResponse}
import io.circe.Json
import io.circe.parser.parse
import io.circe.schema.Schema
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter

object ValidateSchema {
  def apply(server: Http4sServerInterpreter[IO])(getSchema: SchemaId => IO[Option[Json]])(): HttpRoutes[IO] =
    server.toRoutes {
      validateSchemaEndpoint.serverLogic[IO] { input =>
        val id = input.id
        parse(input.body) match {
          case Left(_) => IO(Left(invalidJson(Actions.Validate, id.id)))
          case Right(json) =>
            getSchema(id).flatMap {
              case None         => IO(Left(schemaNotFound(Actions.Validate, id.id)))
              case Some(schema) => IO(validate(json, schema, id))
            }
        }
      }
    }

  private def validate(json: Json, schema: Json, id: SchemaId): Either[ErrorResponse, SuccessResponse] =
    Schema
      .load(schema)
      .validate(json.deepDropNullValues)
      .bimap(
        nel =>
          ErrorResponse(
            Actions.Validate,
            id.id,
            message = nel.toList.map(_.getMessage).mkString(" , ")
          ),
        _ => SuccessResponse(Actions.Validate, id.id)
      )
      .toEither
}
