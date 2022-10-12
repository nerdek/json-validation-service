package com.wikiera.http

import cats.effect.IO
import com.wikiera.http.Inputs._
import com.wikiera.http.Outputs._
import io.circe.Json
import io.circe.generic.auto._
import sttp.capabilities.fs2.Fs2Streams
import sttp.model.StatusCode
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.{endpoint, path, statusCode, stringBody, EndpointInput, PublicEndpoint}

object Endpoints {

  private val idAndBodyInput: EndpointInput[SchemaInput] =
    path[SchemaId]("schemaId")
      .and(stringBody.description("input Json body which is Json schema or Json to be validated"))
      .mapTo[SchemaInput]

  private val idInput = path[SchemaId]("schemaId")

  private def baseEndpoint[T](path: String)(input: EndpointInput[T]) = endpoint
    .in(path)
    .in(input)
    .errorOut(jsonBody[ErrorResponse])

  lazy val addSchemaEndpoint: PublicEndpoint[SchemaInput, ErrorResponse, SuccessResponse, Any] =
    baseEndpoint("schema")(idAndBodyInput).post
      .out(statusCode(StatusCode.Created))
      .out(jsonBody[SuccessResponse])

  lazy val getSchemaEndpoint: PublicEndpoint[SchemaId, ErrorResponse, Json, Any] =
    baseEndpoint("schema")(idInput).get
      .out(jsonBody[Json])

  lazy val validateSchemaEndpoint: PublicEndpoint[SchemaInput, ErrorResponse, SuccessResponse, Any] =
    baseEndpoint("validate")(idAndBodyInput).post
      .out(jsonBody[SuccessResponse])

  lazy val swagger: List[ServerEndpoint[Fs2Streams[IO], IO]] =
    SwaggerInterpreter()
      .fromEndpoints[IO](
        List(getSchemaEndpoint, addSchemaEndpoint, validateSchemaEndpoint),
        "json-validation-service",
        "1.0.0"
      )
}

object Inputs {
  case class SchemaId(id: String) extends AnyVal
  case class SchemaInput(id: SchemaId, body: String)
}

object Outputs {
  case class SuccessResponse(action: String, id: String, status: String = "success")
  case class ErrorResponse(action: String, id: String, status: String = "error", message: String)

  object ErrorResponse {
    def invalidJson(action: String, id: String): ErrorResponse = ErrorResponse(action, id, message = "Invalid JSON")

    def schemaNotFound(action: String, id: String): ErrorResponse =
      ErrorResponse(action, id, message = "Schema not found")
  }
}

object Actions {
  val Validate       = "validateSchema"
  val UploadSchema   = "uploadSchema"
  val DownloadSchema = "downloadSchema"
}
