package com.wikiera.db.repository

import cats.effect.IO
import com.wikiera.db.{PostgresConfig, PostgresTransactor}
import com.wikiera.http.Inputs.{JsonSchema, SchemaId, SchemaInput}
import io.circe.Json
import doobie.implicits._
import doobie.postgres.circe.json.implicits._

trait JsonSchemaRepository {
  def getSchema(schemaId: SchemaId): IO[Option[Json]]
  def addSchema(schemaInput: JsonSchema): IO[Unit]
}
// Used only for testing locally and being independent from DB. Similar implementation could be passed to tests
// Obviously using Map for production code doesn't make sense and it's for spinning up service quickly without any dependencies.
class InMemoryJsonSchemeRepository extends JsonSchemaRepository {
  private val schemas = scala.collection.mutable.Map[String, Json]()

  override def getSchema(id: SchemaId): IO[Option[Json]] = IO(schemas.get(id.id))
  override def addSchema(input: JsonSchema): IO[Unit]    = IO(schemas += (input.id.id -> input.schema))
}

class PostgresJsonSchemaRepository(config: PostgresConfig) extends JsonSchemaRepository {
  private val transactor = PostgresTransactor.transactor(config)

  override def getSchema(schemaId: SchemaId): IO[Option[Json]] =
    sql"""
          SELECT SCHEMA 
          FROM JSON_SCHEMAS
          WHERE ID = ${schemaId.id}
         """.query[Json].option.transact(transactor)

  override def addSchema(input: JsonSchema): IO[Unit] =
    sql"""
          INSERT INTO JSON_SCHEMAS(ID, SCHEMA) 
          VALUES (${input.id}, ${input.schema})
         """.update.run.transact(transactor).void
}
