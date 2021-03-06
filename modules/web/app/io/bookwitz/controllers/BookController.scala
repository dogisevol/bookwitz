package io.bookwitz.controllers

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import io.bookwitz.service.WordsService
import io.bookwitz.service.slick.SlickWordsService
import io.bookwitz.users.models.BasicUser
import io.bookwitz.web.models.BooksTableQueries.booksList
import io.bookwitz.web.models.{Book, UserWord}
import play.api.Logger
import play.api.Play.current
import play.api.db.DB
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{MaxSizeExceeded, Result}
import securesocial.core.{RuntimeEnvironment, SecureSocial}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.slick.driver.JdbcDriver.simple._

object BookController {
}


class BookController(override implicit val env: RuntimeEnvironment[BasicUser]) extends SecureSocial[BasicUser] {

  val SERVICES_URI: String = "https://dictwitz.herokuapp.com/"
  //val SERVICES_URI: String = "http://localhost:8080/"
  val PARSER_URI: String = SERVICES_URI + "bookUpload"
  val DICTIONARY_URI: String = SERVICES_URI + "dictionary"

  val logger = Logger(getClass)
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  //val wordsService: WordsService = new MongoWordsService
  val wordsService: WordsService = new SlickWordsService


  def books = SecuredAction { request => {
    Database.forDataSource(DB.getDataSource()) withSession { implicit session =>
      implicit val writes = Json.writes[Book]
      Ok(Json.stringify(Json.toJson(booksList.list)))
    }
  }
  }

  implicit val userWordJsonWrite = new Writes[UserWord] {
    def writes(u: UserWord): JsValue = {
      Json.obj(
        "word" -> JsString(u.word),
        "note" -> JsString(u.note.getOrElse(""))
      )
    }
  }

  def userWords() = SecuredAction.async { request => {
    val words = wordsService.getUserWords(request.user)
    words.onFailure {
      case result =>
        InternalServerError("failure");
    }
    words.map(
      message =>
        Ok(Json.toJson(message))
    )
  }
  }

  def book() = SecuredAction.async { request => {
    Future(Ok(wordsService.getBook(request.user)))
    Future(Ok(wordsService.addOrUpdateBook(removeKnownWords(wordsService.getBook(request.user), request.user), request.user).book))
  }
  }

  def contentUpload = SecuredAction.async(parse.json(maxLength = 1024 * 1024)) { request => {
    forwardUpload(request.body.\("content").as[String])
  }
  }

  def addUserWords = SecuredAction(parse.json(maxLength = 1024 * 1024)) { request => {
    request.body.\\("word").foreach(
      word =>
        try {
          //TODO note
          wordsService.addOrUpdateWord(word.as[String], "", request.user)
        } catch {
          case e: Exception => {
            logger.error("Cannot add user word", e)
            InternalServerError
          }
        }
    )
    Ok
  }
  }

  def updateUserWord = SecuredAction(parse.json(maxLength = 1024 * 1024)) { request => {
    try {
      //TODO note
      wordsService.addOrUpdateWord(request.body.\("word").as[String], request.body.\("note").as[String], request.user)
    } catch {
      case e: Exception => {
        logger.error("Cannot add user word", e)
        InternalServerError
      }
    }
    Ok
  }
  }

  def bookUpload = SecuredAction.async(parse.maxLength(1024 * 1024, parse.multipartFormData)) { request => {
    request.body match {
      case Left(MaxSizeExceeded(length)) => {
        logger.error("Progress response error. File is too large " + length)
        Future(BadRequest("Your file is too large, we accept just " + length + " bytes!"))
      }
        case Right(multipartForm) => {
        val (progressEnumerator, progressChannel) = Concurrent.broadcast[JsValue]
        val file = multipartForm.files.head.ref.file
        forwardUpload(scala.io.Source.fromFile(file).mkString)
      }
    }
  }
  }


  def bookProcessProgress(uuid: String) = SecuredAction.async { request => {

    val httpRequest = HttpRequest(method = HttpMethods.GET,
      uri = Uri(PARSER_URI).withQuery(Uri.Query("uuid" -> uuid)))
    val response = Http().singleRequest(httpRequest)
    response onFailure {
      case result =>
        logger.error("Progress response error", result)
        InternalServerError("failure")
    }
    response.flatMap(
      response => {
        val entity = response.entity.toStrict(5 seconds)
        entity onFailure {
          case result =>
            logger.error("Progress response error", result)
            InternalServerError("failure")
        }

        entity.flatMap(
          entity => {
            val text = entity.data.decodeString("UTF-8")
            logger.debug("Progress response " + text)
            try {
              val value = Json.parse(text)
              if ("done".equals(value.\("status").as[String])) {
                val result: String = removeKnownWords(value, request.user)
                Future(Ok(wordsService.addOrUpdateBook(removeKnownWords(value, request.user), request.user).book))
              } else {
                Future(Ok(text))
              }
            } catch {
              case e: Exception =>
                logger.error("cannot parse the response", e)
                Future(InternalServerError("failure"))
            }
          }
        )
      }
    )
  }
  }

  def removeKnownWords(json: String, user: BasicUser): String = {
    removeKnownWords(Json.parse(json), user)
  }

  def removeKnownWords(json: JsValue, user: BasicUser): String = {
    var value = json
    var list = ListBuffer[JsValue]()
    value.\("data").as[JsArray].value.map(
      item =>
        if (!wordsService.containsWord(user, item.as[JsObject].\("word").as[String])) {
          list += item
        }
    )

    value = value.transform((__).json.update(
      __.read[JsObject].map { o => o ++ Json.obj("data" -> JsArray(list)) }
    )).asOpt.get
    val result = Json.stringify(value)
    result
  }

  def forwardUpload(content: String): Future[Result] = {
    val httpRequest = HttpRequest(method = HttpMethods.POST, uri = PARSER_URI,
      entity = FormData("content" -> content).toEntity)
    val response = Http().singleRequest(httpRequest)
    response onFailure {
      case result =>
        logger.error("Progress response error ", result)
        InternalServerError("failure")
    }
    response.flatMap(
      response => {
        val entity = response.entity.toStrict(5 seconds)
        entity onFailure {
          case result =>
            logger.error("Progress response error ", result)
            InternalServerError("failure")
        }

        entity.flatMap(
          entity => {
            if (response.status.isSuccess()) {
              val uuid = entity.data.decodeString("UTF-8")
              logger.debug("Upload response " + uuid)
              Future(Ok(uuid))
            } else {
              logger.error("Progress response error " + response.status.reason())
              Future(InternalServerError("failure"))
            }
          }
        )
      }
    )
  }

  def wordDefinitions(word: String) = SecuredAction.async { request =>
    val httpRequest = HttpRequest(method = HttpMethods.GET, uri = Uri(DICTIONARY_URI + "?word=" + word))
    val response = Http().singleRequest(httpRequest)
    response onFailure {
      case result =>
        logger.error("Progress response error ", result)
        InternalServerError("failure")
    }
    response.flatMap(
      response => {
        val entity = response.entity.toStrict(5 seconds)
        entity onFailure {
          case result =>
            logger.error("Progress response error ", result)
            InternalServerError("failure")
        }

        entity.flatMap(
          entity => {
            if (response.status.isSuccess()) {
              val uuid = entity.data.decodeString("UTF-8")
              logger.debug("Upload response " + uuid)
              Future(Ok(uuid))
            } else {
              logger.error("Progress response error " + response.status.reason())
              Future(InternalServerError("failure"))
            }
          }
        )
      }
    )
  }
}