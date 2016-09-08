package io.bookwitz.web.models

import scala.slick.driver.JdbcDriver.simple._

class UserWords(tag: Tag) extends Table[UserWord](tag, "user_words") {
  def userId = column[Option[Long]]("user_id", O.PrimaryKey)

  def word = column[String]("word")

  def note = column[Option[String]]("note")

  def * = (userId, word, note) <> (UserWord.tupled, UserWord.unapply)
}
