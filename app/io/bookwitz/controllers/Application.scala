package io.bookwitz.controllers

import javax.inject.Inject

import io.bookwitz.users.models.BasicUser
import io.bookwitz.web.models.{Navigation, NavigationItem, NavigationMenu}
import play.api.Logger
import play.api.mvc.Action
import securesocial.core.{RuntimeEnvironment, SecureSocial}


class Application(override implicit val env: RuntimeEnvironment[BasicUser]) extends SecureSocial[BasicUser] {
  val logger = Logger(getClass)


  def index = Action {
    Ok(views.html.index())
  }

  def navigation() = UserAwareAction { implicit request =>
    request.user match {
      case Some(user) => {
        Ok(Navigation("default", menus = Seq(
          NavigationMenu(items = Seq(NavigationItem("Change Password", "#/password")), position = "left"),
          NavigationMenu(items = Seq(NavigationItem("BookParser", "#/test")), position = "left"),
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
