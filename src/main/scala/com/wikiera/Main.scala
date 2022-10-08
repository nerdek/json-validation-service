package com.wikiera

import cats.effect._
import com.wikiera.endpoints.Endpoints.swagger
import com.wikiera.endpoints.Routes._
import com.wikiera.log.Logging
import fs2.Stream._
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import sttp.tapir.server.http4s.Http4sServerInterpreter

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

object Main extends IOApp with Logging {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  case class MyFailure(msg: String)

  private val server: Http4sServerInterpreter[IO] = Http4sServerInterpreter[IO]()

  val serviceRoutes: HttpRoutes[IO] = server.toRoutes(List(addSchemaLogic, getSchemaLogic))
  val swaggerRoute: HttpRoutes[IO] = server.toRoutes(swagger)

  override def run(args: List[String]): IO[ExitCode] = {
    (for {
      _ <- eval(IO(logger.info("Starting json-validation-service")))
      _ <- BlazeServerBuilder[IO]
        .withExecutionContext(ec)
        .bindHttp(8080, "localhost")
        .withHttpApp(Router("/" -> serviceRoutes, "/" -> swaggerRoute).orNotFound)
        .serve
    } yield {}).compile.drain.as(ExitCode.Success).handleErrorWith { case NonFatal(e) =>
      IO(logger.error("Error in json-validator service", e)).as(ExitCode.Error)
    }
  }
}
