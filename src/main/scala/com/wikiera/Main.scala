package com.wikiera

import cats.effect._
import com.wikiera.config.AppConfig
import com.wikiera.http.Routes
import com.wikiera.log.Logging
import fs2.Stream._
import org.http4s.blaze.server.BlazeServerBuilder
import pureconfig.ConfigSource
import sttp.tapir.server.http4s.Http4sServerInterpreter
import pureconfig.module.catseffect.syntax._
import pureconfig.generic.auto._

import scala.util.control.NonFatal

object Main extends IOApp with Logging {

  private val server: Http4sServerInterpreter[IO] = Http4sServerInterpreter[IO]()
  private val routes: Routes                      = new Routes(server)

  override def run(args: List[String]): IO[ExitCode] =
    (for {
      _      <- eval(IO(logger.info("Starting json-validation-service")))
      config <- eval(ConfigSource.default.loadF[IO, AppConfig]())
      _      <- eval(IO(logger.info(config.toString)))

      _ <- BlazeServerBuilder[IO]
        .bindHttp(config.http.port, config.http.host)
        .withHttpApp(routes.combinedRoutes.orNotFound)
        .serve
    } yield {}).compile.drain.as(ExitCode.Success).handleErrorWith { case NonFatal(e) =>
      IO(logger.error("Error in json-validator service", e)).as(ExitCode.Error)
    }
}
