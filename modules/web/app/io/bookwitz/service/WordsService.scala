package io.bookwitz.service

import io.bookwitz.users.models.{BasicUser, User}

import scala.concurrent.Future

trait WordsService {

  def getUserWords(user: BasicUser): Future[List[String]]

  def containsWord(user: BasicUser, word: String): Boolean

}
