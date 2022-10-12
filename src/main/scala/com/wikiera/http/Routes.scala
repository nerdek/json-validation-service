package com.wikiera.http

import cats.effect.IO
import cats.implicits._
import com.wikiera.http.Endpoints.swagger
import com.wikiera.log.Logging
import io.circe.Json
import io.circe.parser._
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter

class Routes(server: Http4sServerInterpreter[IO]) extends Logging {
  var schemas = scala.collection.mutable.Map("1" -> parse("{}").getOrElse(Json.Null))

  def combinedRoutes: HttpRoutes[IO] = publicRoutes <+> swaggerRoute

  val get      = GetSchema(server)(id => IO(schemas.get(id.id)))
  val add      = AddSchema(server)(input => IO(schemas += (input.id.id -> parse(input.body).toOption.get)))
  val validate = ValidateSchema(server)(id => IO(schemas.get(id.id)))

  private lazy val publicRoutes: HttpRoutes[IO] = get <+> add <+> validate
  private lazy val swaggerRoute: HttpRoutes[IO] = server.toRoutes(swagger)
}
