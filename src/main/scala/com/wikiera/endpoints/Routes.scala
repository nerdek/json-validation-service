package com.wikiera.endpoints

import cats.effect.IO
import com.wikiera.log.Logging
import io.circe.Json
import io.circe.parser._
import io.circe.schema.Schema

object Routes extends Logging {
  var schemas = scala.collection.mutable.Map("1" -> parse("{}").getOrElse(Json.Null))

  val getSchemaLogic = Endpoints.getSchema.serverLogic[IO] { input =>
    IO {
      schemas.get(input.id) match {
        case Some(value) => Right(value)
        case None        => Left(ErrorResponse("downloadSchema", input.id, "error", "schema not found"))
      }
    }
  }

  val addSchemaLogic = Endpoints.addSchema.serverLogic[IO] { input =>
    IO {
      val parseRes = parse(input.body)
      parseRes match {
        case Left(_) => Left(ErrorResponse("uploadSchema", input.id.id, "error", "Invalid JSON"))
        case Right(value) =>
          schemas += (input.id.id -> value)
          Right(SuccessResponse("uploadSchema", input.id.id, "success"))
      }
    }
  }

  val validateSchemaLogic = Endpoints.validateJson.serverLogic[IO] { input =>
    IO {
      val parseRes = parse(input.body)
      val id = input.id.id
      parseRes match {
        case Left(_) => Left(ErrorResponse("validateSchema", id, "error", "Invalid JSON"))
        case Right(value) =>
          val schema = schemas.get(id)
          schema match {
            case None => Left(ErrorResponse("validateSchema", id, "error", "schema not found"))
            case Some(schema) =>
              val loaded = Schema.load(schema)
              val validationResult = loaded.validate(value.deepDropNullValues)
              validationResult
                .bimap(
                  nel => ErrorResponse("validateSchema", id, "error", nel.toList.map(_.getMessage).mkString("\n")),
                  _ => SuccessResponse("validateSchema", id, "success")
                )
                .toEither
          }
      }
    }
  }
}
