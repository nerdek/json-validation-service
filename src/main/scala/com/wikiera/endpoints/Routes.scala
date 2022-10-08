package com.wikiera.endpoints

import cats.effect.IO
import com.wikiera.log.Logging
import io.circe.Json
import io.circe.parser._

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
      schemas += (input.id.id -> input.body)
      Right(SuccessResponse("uploadSchema", input.id.id, "success"))
    }
  }
}
