package com.wikiera.config

import com.wikiera.db.PostgresConfig
import com.wikiera.http.HttpConfig

case class AppConfig(postgres: PostgresConfig, http: HttpConfig)
