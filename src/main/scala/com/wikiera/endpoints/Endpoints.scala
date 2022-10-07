package com.wikiera.endpoints

import cats.effect.IO
import io.circe.Json
import io.circe.generic.auto._
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.{PublicEndpoint, endpoint, path, query, stringBody}

object Endpoints {

  val helloWorld: PublicEndpoint[String, Unit, String, Any] =
    endpoint.get.in("hello").in(query[String]("name")).out(stringBody)

  val addSchema: PublicEndpoint[SchemaId, EndpointResponse, EndpointResponse, Any] =
    endpoint.post
      .in("schema")
      .in(path[SchemaId]("schemaId"))
      .out(jsonBody[EndpointResponse])
      .errorOut(jsonBody[EndpointResponse])

  val getSchema: PublicEndpoint[SchemaId, EndpointResponse, Json, Any] =
    endpoint.get
      .in("schema")
      .in(path[SchemaId]("schemaId"))
      .out(jsonBody[Json])
      .errorOut(jsonBody[EndpointResponse])

  val swagger: List[ServerEndpoint[Fs2Streams[IO], IO]] =
    SwaggerInterpreter().fromEndpoints[IO](List(helloWorld), "json-validation-service", "1.0.0")
}

case class SchemaId(id: String) extends AnyVal
case class EndpointResponse(action: String, id: String, status: String, message: Option[String] = None)
