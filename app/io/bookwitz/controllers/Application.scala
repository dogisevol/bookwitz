package io.bookwitz.controllers

import play.api.Play.current
import io.bookwitz.users.models.BasicUser
import io.bookwitz.web.models.{Navigation, NavigationItem, NavigationMenu}
import play.api.Logger
import play.api.cache.Cached
import play.api.mvc.Action
import securesocial.core.{RuntimeEnvironment, SecureSocial}


class Application(override implicit val env: RuntimeEnvironment[BasicUser]) extends SecureSocial[BasicUser] {
  val logger = Logger(getClass)

  def requireJsConfig = Cached("require_js_config") {
    Action {
      Ok(views.html.requireJsConfig()).as("application/javascript")
    }
  }

  def index = Action {
    Ok(views.html.index())
  }

  def navigation() = UserAwareAction { implicit request =>
    request.user match {
      case Some(user) => {
        Ok(Navigation("default", menus = Seq(
          NavigationMenu(items = Seq(NavigationItem("Books", "#/books")), position = "left"),
          NavigationMenu(items = Seq(NavigationItem("Profile", "#/password")), position = "left"),
          NavigationMenu(items = Seq(NavigationItem("Sign Out", "#/users/logout")), position = "right")
        )).json)
      }
      case _ => {
        Ok(Navigation("default", menus = Seq(
          NavigationMenu(
            items = Seq(NavigationItem("Sign In", "#/login"), NavigationItem("Sign Up", "#/signup")),
            position = "right"
          )
        )).json)
      }
    }
  }
}
