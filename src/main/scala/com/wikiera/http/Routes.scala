package com.wikiera.http

import cats.effect.IO
import com.wikiera.http.Actions.{DownloadSchema, UploadSchema, ValidateSchema}
import com.wikiera.http.Endpoints.{addSchema, getSchema, swagger, validateJson}
import com.wikiera.http.Outputs.ErrorResponse._
import com.wikiera.http.Outputs.{ErrorResponse, SuccessResponse}
import com.wikiera.log.Logging
import io.circe.Json
import io.circe.parser._
import io.circe.schema.Schema
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter
import cats.implicits._

class Routes(server: Http4sServerInterpreter[IO]) extends Logging {
  var schemas = scala.collection.mutable.Map("1" -> parse("{}").getOrElse(Json.Null))

  def combinedRoutes: HttpRoutes[IO] =
    publicRoutes <+> swaggerRoute

  private lazy val publicRoutes: HttpRoutes[IO] =
    server.toRoutes(List(getSchemaLogic, addSchemaLogic, validateSchemaLogic))
  private lazy val swaggerRoute: HttpRoutes[IO] = server.toRoutes(swagger)

  private val getSchemaLogic =
    getSchema.serverLogic[IO] { input =>
      IO {
        schemas.get(input.id) match {
          case Some(value) => Right(value)
          case None        => Left(schemaNotFound(DownloadSchema, input.id))
        }
      }
    }

  private val addSchemaLogic = addSchema.serverLogic[IO] { input =>
    IO {
      parse(input.body) match {
        case Left(_) => Left(invalidJson(UploadSchema, input.id.id))
        case Right(value) =>
          schemas += (input.id.id -> value)
          Right(SuccessResponse(UploadSchema, input.id.id))
      }
    }
  }

  private val validateSchemaLogic = validateJson.serverLogic[IO] { input =>
    IO {
      val id = input.id.id
      parse(input.body) match {
        case Left(_) => Left(invalidJson(ValidateSchema, id))
        case Right(value) =>
          val schema = schemas.get(id)
          schema match {
            case None => Left(schemaNotFound(ValidateSchema, id))
            case Some(schema) =>
              Schema
                .load(schema)
                .validate(value.deepDropNullValues)
                .bimap(
                  nel => ErrorResponse(ValidateSchema, id, message = nel.toList.map(_.getMessage).mkString(" , ")),
                  _ => SuccessResponse(ValidateSchema, id)
                )
                .toEither
          }
      }
    }
  }
}
