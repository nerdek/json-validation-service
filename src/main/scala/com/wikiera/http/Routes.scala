package com.wikiera.http

import cats.effect.IO
import cats.implicits._
import com.wikiera.db.repository.JsonSchemaRepository
import com.wikiera.http.Endpoints.swagger
import com.wikiera.log.Logging
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter

class Routes(server: Http4sServerInterpreter[IO])(repo: JsonSchemaRepository) extends Logging {
  private val get      = GetSchema(server)(repo.getSchema)
  private val add      = AddSchema(server)(repo.getSchema)(repo.addSchema)
  private val validate = ValidateSchema(server)(repo.getSchema)

  private lazy val publicRoutes: HttpRoutes[IO] = get <+> add <+> validate
  private lazy val swaggerRoute: HttpRoutes[IO] = server.toRoutes(swagger)

  def combinedRoutes: HttpRoutes[IO] = publicRoutes <+> swaggerRoute
}
