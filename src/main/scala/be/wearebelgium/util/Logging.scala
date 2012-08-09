package be.wearebelgium.util

import grizzled.slf4j.Logger

trait Logging {

  @transient protected lazy val logger = Logger(getClass)
}
