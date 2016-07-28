package io.bookwitz.users.models.words

import io.bookwitz.users.models.WordTableQueries.wordsList
import play.api.libs.json.Json

import scala.slick.driver.JdbcDriver.simple._

case class Word(word: String, number: Long) {
  def basicWord(implicit session: Session): Word = {
    val main = wordsList.filter(_.word === word).first

    Word(main.word, main.number)
  }
}

class Words(tag: Tag) extends Table[Word](tag, "words") {
  def number = column[Long]("number", O.PrimaryKey)

  def word = column[String]("word")

  def * = (word, number) <>(Word.tupled, Word.unapply)
}
