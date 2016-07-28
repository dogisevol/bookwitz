package io.bookwitz.users.service.slick

import io.bookwitz.users.models.WordTableQueries.wordsList
import play.api.Logger
import play.api.db.slick._
import securesocial.core.BasicProfile
import securesocial.core.providers.{UsernamePasswordProvider => UserPass}

import scala.concurrent.Future

class SlickWordsService {
  val logger: Logger = Logger(this.getClass)
}
