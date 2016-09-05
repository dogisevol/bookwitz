package io.bookwitz.web.models.mongo

import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao._
import io.bookwitz.users.models.BasicUser
import io.bookwitz.web.models.mongo.mongoContext._
import play.api.Play.current
import play.api.libs.functional.syntax._
import play.api.libs.json._
import plugin.salat._

import scala.collection.mutable.ListBuffer

case class UserWords(
                      userId: String,
                      words: ListBuffer[String]
                    )

object MongoUserWords extends UserWordsDAO with UserWordsJson {
}

trait UserWordsDAO extends ModelCompanion[UserWords, String] {
  def collection = mongoCollection("user_words")

  val dao = new SalatDAO[UserWords, String](collection) {}

  // Indexes
  //TODO

  // Queries
  def findOneByUserId(userId: Long): Option[UserWords] = dao.findOne(MongoDBObject("userId" -> String.valueOf(userId)))

  def findOneByUserId(user: BasicUser): Option[UserWords] = {
    findOneByUserId(user.id)
  }

}

/**
  * Trait used to convert to and from json
  */
trait UserWordsJson {

  implicit val userJsonWrite = new Writes[UserWords] {
    def writes(u: UserWords): JsValue = {
      Json.obj(
        "userId" -> JsString(u.userId),
        "words" -> Json.toJson(u.words)
      )
    }
  }
  implicit val userJsonRead = (
    (__ \ 'userId).read[String] ~
      (__ \ 'words).read[ListBuffer[String]]
    ) (UserWords.apply _)
}