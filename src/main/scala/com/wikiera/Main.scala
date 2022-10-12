package com.wikiera

import cats.effect._
import com.wikiera.http.Routes
import com.wikiera.log.Logging
import fs2.Stream._
import org.http4s.blaze.server.BlazeServerBuilder
import sttp.tapir.server.http4s.Http4sServerInterpreter

import scala.util.control.NonFatal

object Main extends IOApp with Logging {

  private val server: Http4sServerInterpreter[IO] = Http4sServerInterpreter[IO]()
  private val routes: Routes                      = new Routes(server)

  override def run(args: List[String]): IO[ExitCode] =
    (for {
      _ <- eval(IO(logger.info("Starting json-validation-service")))
      _ <- BlazeServerBuilder[IO]
        .bindHttp(8080, "localhost")
        .withHttpApp(routes.combinedRoutes.orNotFound)
        .serve
    } yield {}).compile.drain.as(ExitCode.Success).handleErrorWith { case NonFatal(e) =>
      IO(logger.error("Error in json-validator service", e)).as(ExitCode.Error)
    }
}
