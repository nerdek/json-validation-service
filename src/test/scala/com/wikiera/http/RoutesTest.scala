package com.wikiera.http

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.http4s.Method.GET
import org.http4s.Request
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import sttp.tapir.server.http4s.Http4sServerInterpreter
import org.http4s.syntax.all._

class RoutesTest extends AsyncWordSpec with AsyncIOSpec with Matchers with OptionValues {

  private val server: Http4sServerInterpreter[IO] = Http4sServerInterpreter[IO]()
  private val routes: Routes                      = new Routes(server)

  val request =
    Request[IO](method = GET, uri = uri"/schema/25")

  "it" should {
    "work" in {

      routes.combinedRoutes.run(request).value.map { response =>
        response.value.status.code shouldBe 400
      }

    }
  }
}
