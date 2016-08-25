package io.bookwitz.service

import io.bookwitz.users.models.BasicUser

import scala.concurrent.Future

trait WordsService {

  def addWord(word: String, user: BasicUser)

  def getUserWords(user: BasicUser): Future[List[String]]

  def containsWord(user: BasicUser, word: String): Boolean

}
