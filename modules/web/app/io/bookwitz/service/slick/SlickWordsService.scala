package io.bookwitz.service.slick

import io.bookwitz.service.WordsService
import io.bookwitz.users.models.BasicUser
import io.bookwitz.web.models.BooksTableQueries.{userBooksList, userWordsList}
import io.bookwitz.web.models.{UserBook, UserWord}
import play.api.Logger
import play.api.Play.current
import play.api.db.slick.DB
import securesocial.core.providers.{UsernamePasswordProvider => UserPass}

import scala.concurrent.Future
import scala.slick.driver.JdbcDriver.simple._

class SlickWordsService extends WordsService {
  val logger: Logger = Logger(this.getClass)

  override def addOrUpdateWord(word: String, note: String, user: BasicUser) {
    val result = UserWord(Option.apply(user.id), word, Option.apply(note))
    DB withSession { implicit session =>
      userWordsList
        .filter(sp => sp.word === word && sp.userId === user.id).firstOption match {
        case Some(p) =>
          userWordsList.update(result)
        case None =>
          userWordsList += result
      }
    }
  }


  override def getUserWords(user: BasicUser): Future[List[UserWord]] = Future successful {
    DB withSession { implicit session =>
      userWordsList
        .filter(sp => sp.userId === user.id).list
    }
  }

  override def containsWord(user: BasicUser, word: String): Boolean = {
    DB withSession { implicit session =>
      userWordsList
        .filter(sp => sp.word === word && sp.userId === user.id).list.length == 1
    }
  }

  override def containsWord(user: BasicUser, word: UserWord): Boolean = {
    containsWord(user, word.word)
  }

  override def getBook(user: BasicUser): String = {
    DB withSession { implicit session =>
      if (userBooksList.length.run > 0) {
        userBooksList.first.book
      } else {
        ""
      }
    }
  }

  override def addOrUpdateBook(book: String, user: BasicUser): UserBook = {
    DB withSession { implicit session =>
      val result = UserBook(Option.apply(user.id), book, None)
      userBooksList
        .filter(p => p.userId === user.id)
        .firstOption match {
        case Some(p) =>
          userBooksList.update(result)
        case None =>
          userBooksList += result
      }
      result
    }
  }

}
