package com.wikiera.db

import cats.effect._

import doobie.Transactor

object PostgresTransactor {
  def transactor(
      config: PostgresConfig
  ): doobie.util.transactor.Transactor[IO] =
    Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      s"jdbc:postgresql:${config.db}",
      config.user,
      config.password
    )
}
