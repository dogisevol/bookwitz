package io.bookwitz.web.models

import scala.slick.driver.JdbcDriver.simple._

case class UserWord(userId: String, word: String) {
}

class UserWords(tag: Tag) extends Table[UserWord](tag, "user_words") {
  def userId = column[String]("userId", O.PrimaryKey)

  def word = column[String]("word")

  def * = (userId, word) <> (UserWord.tupled, UserWord.unapply)
}
