package io.bookwitz.web.models

import scala.slick.driver.JdbcDriver.simple._

class UserBooks(tag: Tag) extends Table[UserBook](tag, "user_books") {
  def userId = column[Option[Long]]("user_id", O.PrimaryKey)

  def word = column[String]("book")

  def note = column[Option[String]]("title")

  def * = (userId, word, note) <> (UserBook.tupled, UserBook.unapply)
}
