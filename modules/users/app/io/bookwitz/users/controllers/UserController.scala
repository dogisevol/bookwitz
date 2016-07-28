package io.bookwitz.users.controllers

import io.bookwitz.users.models.BasicUser
import io.bookwitz.users.models.WordTableQueries.wordsList
import io.bookwitz.users.models.words.Word
import play.api.Logger
import play.api.Play.current
import play.api.db.DB
import play.api.libs.json.Json
import play.api.mvc.Action
import resource._
import securesocial.core.{RuntimeEnvironment, SecureSocial}

import scala.io.Source
import scala.slick.driver.JdbcDriver.simple._
import scala.util.Random

class UserController(override implicit val env: RuntimeEnvironment[BasicUser]) extends SecureSocial[BasicUser] {
  val logger = Logger(getClass)

  def generateName() = Action { implicit request =>
    Ok(Json.stringify(Json.obj(
      "firstName" -> getRandomName("/names/firstname.txt"),
      "lastName" -> getRandomName("/names/lastname.txt")
    )))
  }

  def getWords() = Action { implicit request =>
    Database.forDataSource(DB.getDataSource()) withSession { implicit session =>
        val test = wordsList.list
      implicit val writes = Json.writes[Word]
      Ok(Json.stringify(Json.toJson(test)))
    }
  }

  def getRandomName(path: String): String = {
    managedSource(path) acquireAndGet {
      sizeSource =>
        val size = sizeSource.getLines().size
        managedSource(path) acquireAndGet {
          nameSource =>
            nameSource.getLines().drop(Random.nextInt(size) - 1).next().capitalize
        }
    }
  }

  def managedSource(classpath: String): ManagedResource[Source] = {
    managed(Source.fromInputStream(getClass.getResourceAsStream(classpath)))
  }

  def home() = SecuredAction { implicit request =>
    Ok(request.user.main.fullName.getOrElse("Full Name"))
  }
}