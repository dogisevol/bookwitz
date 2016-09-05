package io.bookwitz.service.mongo

import io.bookwitz.service.WordsService
import io.bookwitz.users.models.BasicUser
import io.bookwitz.web.models.mongo.{MongoUserWords, UserWords}
import play.api.Logger
import securesocial.core.providers.{UsernamePasswordProvider => UserPass}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

class MongoWordsService extends WordsService {
  val logger: Logger = Logger(this.getClass)

  override def addWord(word: String, user: BasicUser): Unit = {
    MongoUserWords.findOneByUserId(user) match {
      case None =>
      //TODO exception handling
      case Some(userWord) => {
        userWord.words += word
        MongoUserWords.save(userWord)
      }
    }
  }

  override def getUserWords(user: BasicUser): Future[List[String]] = Future successful {
    MongoUserWords.findOneByUserId(user) match {
      case None =>
        val userWords = UserWords(None, String.valueOf(user.id), ListBuffer[String]())
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
      case Some(userWord) => {
        userWord.words.contains(word)
      }
    }
  }
}
