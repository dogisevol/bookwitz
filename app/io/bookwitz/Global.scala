package io.bookwitz

import java.lang.reflect.Constructor

import io.bookwitz.users.models.BasicUser
import io.bookwitz.users.service.slick.{SlickAuthenticatorStore, SlickUserService}
import securesocial.core.RuntimeEnvironment
import securesocial.core.authenticator.{CookieAuthenticatorBuilder, HttpHeaderAuthenticatorBuilder}
import securesocial.core.services.{AuthenticatorService, UserService}

object Global extends play.api.GlobalSettings {

  object MyRuntimeEnvironment extends RuntimeEnvironment.Default[BasicUser] {
    override val userService: UserService[BasicUser] = new SlickUserService
    override lazy val authenticatorService: AuthenticatorService[BasicUser] = new AuthenticatorService[BasicUser](
      new CookieAuthenticatorBuilder[BasicUser](new SlickAuthenticatorStore, idGenerator),
      new HttpHeaderAuthenticatorBuilder[BasicUser](new SlickAuthenticatorStore, idGenerator)
    )
  }

  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    val instance = controllerClass.getConstructors.find { c =>
      val params = c.getParameterTypes
      params.length == 1 && params(0) == classOf[RuntimeEnvironment[BasicUser]]
    }.map {
      _.asInstanceOf[Constructor[A]].newInstance(MyRuntimeEnvironment)
    }
    instance.getOrElse(super.getControllerInstance(controllerClass))
  }
}
