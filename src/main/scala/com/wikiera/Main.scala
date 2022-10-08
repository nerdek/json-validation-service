package com.wikiera

import cats.effect._
import cats.syntax.all._
import com.wikiera.endpoints.Endpoints.swagger
import com.wikiera.log.Logging
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.blaze.server.BlazeServerBuilder
import sttp.tapir._
import sttp.tapir.server.http4s.Http4sServerInterpreter
import fs2.Stream._

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

object Main extends IOApp with Logging {
  val helloWorld: PublicEndpoint[String, Unit, String, Any] =
    endpoint.get.in("hello").in(query[String]("name")).out(stringBody)

  val helloWorldRoutes: HttpRoutes[IO] =
    Http4sServerInterpreter[IO]().toRoutes(helloWorld.serverLogic(name => IO(s"Hello, $name!".asRight[Unit])))

  val swaggerRoute = Http4sServerInterpreter[IO]().toRoutes(swagger)

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override def run(args: List[String]): IO[ExitCode] = {
    (for {
      _ <- eval(IO(logger.info("Starting json-validation-service")))
      _ <- BlazeServerBuilder[IO]
        .withExecutionContext(ec)
        .bindHttp(8080, "localhost")
        .withHttpApp(Router("/" -> helloWorldRoutes, "/" -> swaggerRoute).orNotFound)
        .serve
    } yield {}).compile.drain.as(ExitCode.Success).handleErrorWith { case NonFatal(e) =>
      IO(logger.error("Error in json-validator service", e)).as(ExitCode.Error)
    }
  }
}
