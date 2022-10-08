package com.wikiera.log

import com.typesafe.scalalogging
import com.typesafe.scalalogging.Logger

trait Logging {
  lazy val logger: scalalogging.Logger = Logger(getClass.getName)
}
