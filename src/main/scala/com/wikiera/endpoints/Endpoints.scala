package com.wikiera.endpoints

import cats.effect.IO
import com.wikiera.endpoints.Inputs._
import com.wikiera.endpoints.Outputs._
import io.circe.Json
import io.circe.generic.auto._
import sttp.capabilities.fs2.Fs2Streams
import sttp.model.StatusCode
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.{EndpointInput, PublicEndpoint, endpoint, path, statusCode, stringBody}

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

  val addSchema: PublicEndpoint[SchemaInput, ErrorResponse, SuccessResponse, Any] =
    baseEndpoint("schema")(idAndBodyInput).post
      .out(statusCode(StatusCode.Created))
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
  val ValidateSchema = "validateSchema"
  val UploadSchema = "uploadSchema"
  val DownloadSchema = "downloadSchema"
}
