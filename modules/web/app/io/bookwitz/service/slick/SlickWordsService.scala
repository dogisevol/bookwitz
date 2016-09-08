package io.bookwitz.service.slick

import io.bookwitz.service.WordsService
import io.bookwitz.users.models.BasicUser
import io.bookwitz.web.models.BooksTableQueries.userWordsList
import io.bookwitz.web.models.UserWord
import play.api.Logger
import play.api.Play.current
import play.api.db.slick.DB
import securesocial.core.providers.{UsernamePasswordProvider => UserPass}

import scala.concurrent.Future
import scala.slick.driver.JdbcDriver.simple._

class SlickWordsService extends WordsService {
  val logger: Logger = Logger(this.getClass)

  override def addWord(word: String, note: String, user: BasicUser) {
    DB withSession { implicit session =>
      userWordsList += UserWord(Option.apply(user.id), word, Option.apply(note))
    }
  }

  override def updateWord(word: String, note: String, user: BasicUser) {
    DB withSession { implicit session =>
      userWordsList
        .filter(sp => sp.word === word && sp.userId === user.id).update(UserWord(Option.apply(user.id), word, Option.apply(note)))
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
}
