package com.wikiera.endpoints

import cats.effect.IO
import com.wikiera.endpoints.Actions.{DownloadSchema, UploadSchema, ValidateSchema}
import com.wikiera.endpoints.Outputs.ErrorResponse._
import com.wikiera.endpoints.Outputs.{ErrorResponse, SuccessResponse}
import com.wikiera.log.Logging
import io.circe.Json
import io.circe.parser._
import io.circe.schema.Schema

object Routes extends Logging {
  var schemas = scala.collection.mutable.Map("1" -> parse("{}").getOrElse(Json.Null))

  val getSchemaLogic =
    Endpoints.getSchema.serverLogic[IO] { input =>
      IO {
        schemas.get(input.id) match {
          case Some(value) => Right(value)
          case None        => Left(schemaNotFound(DownloadSchema, input.id))
        }
      }
    }

  val addSchemaLogic = Endpoints.addSchema.serverLogic[IO] { input =>
    IO {
      parse(input.body) match {
        case Left(_) => Left(invalidJson(UploadSchema, input.id.id))
        case Right(value) =>
          schemas += (input.id.id -> value)
          Right(SuccessResponse(UploadSchema, input.id.id))
      }
    }
  }

  val validateSchemaLogic = Endpoints.validateJson.serverLogic[IO] { input =>
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
