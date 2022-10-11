//package com.wikiera.endpoints
//
//import cats.effect.IO
//import org.scalatest.flatspec.AsyncFlatSpec
//import org.scalatest.matchers.should.Matchers
//import sttp.capabilities.WebSockets
//import sttp.client3._
//import sttp.client3.testing.SttpBackendStub
//import sttp.monad.MonadError
//import sttp.tapir.server.stub.TapirStubInterpreter
//import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
//
//import scala.concurrent.Future
//
//class RoutesTest extends AsyncFlatSpec with Matchers {
//  private val catsSttpBackend: IO[SttpBackend[IO, Any]] = AsyncHttpClientCatsBackend[IO]()
//
//  val xd = SttpBackendStub.synchronous
//  it should "work" in {
//    // given
//
//    catsSttpBackend.flatMap(b=>TapirStubInterpreter(b))
//
//      TapirStubInterpreter(xd)
//        .whenServerEndpoint(Routes.getSchemaLogic[Identity])
//        .thenRunLogic()
//        .backend()
//    )
//    val backendStub: SttpBackend[IO, Any] = TapirStubInterpreter(catsSttpBackend)
//      .whenServerEndpoint(Routes.getSchemaLogic)
//      .thenRunLogic()
//      .backend()
//
//    // when
//    val response = basicRequest
//      .get(uri"http://test.com/api/users/greet")
//      .header("Authorization", "Bearer password")
//      .send(backendStub)
//
//    // then
//    response.map(_.body shouldBe Right("hello user123"))
//  }
//}
