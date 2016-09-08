package io.bookwitz.service

import io.bookwitz.users.models.BasicUser
import io.bookwitz.web.models.UserWord

import scala.concurrent.Future

trait WordsService {

  def addWord(word: String, note: String, user: BasicUser)

  def updateWord(word: String, note: String, user: BasicUser)

  def getUserWords(user: BasicUser): Future[List[UserWord]]

  def containsWord(user: BasicUser, word: UserWord): Boolean

  def containsWord(user: BasicUser, word: String): Boolean

}
