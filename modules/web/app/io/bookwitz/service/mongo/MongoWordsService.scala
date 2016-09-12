package io.bookwitz.service.mongo

import io.bookwitz.service.WordsService
import io.bookwitz.users.models.BasicUser
import io.bookwitz.web.models.{UserBook, UserWord}
import io.bookwitz.web.models.mongo.{MongoUserWords, UserWords}
import play.api.Logger
import securesocial.core.providers.{UsernamePasswordProvider => UserPass}

import scala.concurrent.Future

class MongoWordsService extends WordsService {
  val logger: Logger = Logger(this.getClass)

  override def addWord(word: String, note: String, user: BasicUser): Unit = {
    MongoUserWords.findOneByUserId(user) match {
      case None =>
        logger.error("User words: found nothing")
      //TODO exception handling
      case Some(userWords) => {
        //TODO
        val buffer = userWords.words.toBuffer
        buffer += UserWord(Option.apply(user.id), word, Option.apply(note))
        MongoUserWords.save(UserWords(userWords._id, userWords.userId, buffer.toList))
      }
    }
  }

  override def getUserWords(user: BasicUser): Future[List[UserWord]] = Future successful {
    MongoUserWords.findOneByUserId(user) match {
      case None =>
        val userWords = UserWords(None, String.valueOf(user.id), Seq[UserWord]())
        MongoUserWords.save(userWords)
        userWords.words.to[List]
      case Some(userWord) => {
        userWord.words.to[List]
      }
    }
  }

  override def containsWord(user: BasicUser, word: String): Boolean = {
    MongoUserWords.findOneByUserId(user) match {
      case None =>
        //TODO exception handling
        false
      case Some(userWords) => {
        userWords.words.contains(UserWord(Option.apply(user.id), word, None))
      }
    }
  }

  override def containsWord(user: BasicUser, word: UserWord): Boolean = {
    containsWord(user, word.word)
  }

  override def updateWord(word: String, note: String, user: BasicUser): Unit = {
    MongoUserWords.findOneByUserId(user) match {
      case None =>
        logger.error("User words: found nothing")
      //TODO exception handling
      case Some(userWords) => {
        //TODO find a right way to do it
        val userWord = userWords.words.find(_.word == word).getOrElse(
          throw new Exception("Couldn't find userWord for update")
        )
        MongoUserWords.save(UserWords(userWords._id, userWords.userId, userWords.words.updated(userWords.words.indexOf(userWord), userWord)))
      }
    }
  }

  override def getBook(user: BasicUser): String = ???

  override def addOrUpdateBook(book: String, user: BasicUser): UserBook = ???
}
