package io.bookwitz.web.models

import scala.slick.driver.JdbcDriver.simple._

case class UserWord(userId: Long, word: String) {
}

class UserWords(tag: Tag) extends Table[UserWord](tag, "user_words") {
  def userId = column[Long]("user_id", O.PrimaryKey)

  def word = column[String]("word")

  def * = (userId, word) <> (UserWord.tupled, UserWord.unapply)
}
