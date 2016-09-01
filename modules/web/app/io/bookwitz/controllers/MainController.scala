package io.bookwitz.controllers


import io.bookwitz.users.models.BasicUser
import io.bookwitz.web.models.{Navigation, NavigationItem, NavigationMenu}
import play.api.Logger
import securesocial.core.{RuntimeEnvironment, SecureSocial}


class MainController(override implicit val env: RuntimeEnvironment[BasicUser]) extends SecureSocial[BasicUser] {
  val logger = Logger(getClass)


  def navigation() = SecuredAction { implicit request =>
    Ok(Navigation("default", menus = Seq(
      NavigationMenu(items = Seq(NavigationItem("Books", "#/books")), position = "left"),
      NavigationMenu(items = Seq(NavigationItem("UserWords", "#/userWords")), position = "left")
    )).json)
  }
}