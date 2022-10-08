package com.wikiera.endpoints

import cats.effect.IO
import io.circe.Json
import io.circe.generic.auto._
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.{EndpointInput, PublicEndpoint, endpoint, path}

object Endpoints {

  private val idAndBodyInput: EndpointInput[SchemaInput] =
    path[SchemaId]("schemaId")
      .and(jsonBody[Json].description("input Json body which is Json schema or Json to be validated"))
      .mapTo[SchemaInput]

  private val idInput = path[SchemaId]("schemaId")

  private def baseEndpoint[T](path: String)(input: EndpointInput[T]) = endpoint
    .in(path)
    .in(input)
    .errorOut(jsonBody[ErrorResponse])

  val addSchema: PublicEndpoint[SchemaInput, ErrorResponse, SuccessResponse, Any] =
    baseEndpoint("schema")(idAndBodyInput).post
      .out(jsonBody[SuccessResponse])

  val getSchema: PublicEndpoint[SchemaId, ErrorResponse, Json, Any] =
    baseEndpoint("schema")(idInput).get
      .out(jsonBody[Json])

  val validateJson: PublicEndpoint[SchemaInput, ErrorResponse, SuccessResponse, Any] =
    baseEndpoint("validate")(idAndBodyInput).post
      .out(jsonBody[SuccessResponse])

  val swagger: List[ServerEndpoint[Fs2Streams[IO], IO]] =
    SwaggerInterpreter()
      .fromEndpoints[IO](List(getSchema, addSchema, validateJson), "json-validation-service", "1.0.0")
}

case class SchemaId(id: String) extends AnyVal
case class SchemaInput(id: SchemaId, body: Json)
case class SuccessResponse(action: String, id: String, status: String)
case class ErrorResponse(action: String, id: String, status: String, message: String)
