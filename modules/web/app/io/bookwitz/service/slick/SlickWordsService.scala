package io.bookwitz.service.slick

import io.bookwitz.service.WordsService
import io.bookwitz.users.models.BasicUser
import io.bookwitz.web.models.BooksTableQueries.userWordsList
import play.api.Logger
import play.api.Play.current
import play.api.db.slick.DB
import securesocial.core.providers.{UsernamePasswordProvider => UserPass}

import scala.concurrent.Future
import scala.slick.driver.JdbcDriver.simple._

class SlickWordsService extends WordsService {
  val logger: Logger = Logger(this.getClass)

  override def getUserWords(user: BasicUser): Future[List[String]] = Future successful {
    DB withSession { implicit session =>
      userWordsList
        .filter(sp => sp.userId === user.main.userId).list.map(p => p.word)
    }
  }
}
