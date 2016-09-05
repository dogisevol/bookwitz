package io.bookwitz.service.mongo

import io.bookwitz.service.WordsService
import io.bookwitz.users.models.BasicUser
import io.bookwitz.web.models.mongo.{MongoUserWords, UserWords}
import play.api.Logger
import securesocial.core.providers.{UsernamePasswordProvider => UserPass}

import scala.concurrent.Future

class MongoWordsService extends WordsService {
  val logger: Logger = Logger(this.getClass)

  override def addWord(word: String, user: BasicUser): Unit = {
    MongoUserWords.findOneByUserId(user) match {
      case None =>
        logger.error("User words: found nothing")
      //TODO exception handling
      case Some(userWord) => {
        //TODO 
        logger.error("found userWord: " + userWord._id)
        logger.error("userWordList: " + userWord.words)
        val buffer = userWord.words.toBuffer
        buffer += word
        logger.error("buffer after word adding: " + buffer)
        MongoUserWords.save(UserWords(userWord._id, userWord.userId, buffer.toList))
      }
    }
  }

  override def getUserWords(user: BasicUser): Future[List[String]] = Future successful {
    MongoUserWords.findOneByUserId(user) match {
      case None =>
        val userWords = UserWords(None, String.valueOf(user.id), Seq[String]())
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
