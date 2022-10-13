package com.wikiera.http

import io.circe.Json
import io.circe.parser.parse

trait Fixtures {
  val nonExistingId: String = "non_existing_id"
  val existingId: String    = "existing_id"

  val properSchema =
    """
          |{
          |  "$schema": "http://json-schema.org/draft-04/schema#",
          |  "type": "object",
          |  "properties": {
          |    "source": {
          |      "type": "string"
          |    },
          |    "destination": {
          |      "type": "string"
          |    },
          |    "timeout": {
          |      "type": "integer",
          |      "minimum": 0,
          |      "maximum": 32767
          |    },
          |    "chunks": {
          |      "type": "object",
          |      "properties": {
          |        "size": {
          |          "type": "integer"
          |        },
          |        "number": {
          |          "type": "integer"
          |        }
          |      },
          |      "required": ["size"]
          |    }
          |  },
          |  "required": ["source", "destination"]
          |}
          |""".stripMargin

  val existingSchema: Json = parse(properSchema).toOption.get

  val properJson = """
      {
        "source": "/home/alice/image.iso",
        "destination": "/mnt/storage",
        "timeout": null,
        "chunks": {
          "size": 1024,
          "number": null
        }
      } """.stripMargin

  val jsonNotAlignedWithSchema = """
      {
        "source": 999,
        "destination": "/mnt/storage",
        "timeout": null,
        "chunks": {
          "size": 1024,
          "number": null
        }
      } """.stripMargin

}
