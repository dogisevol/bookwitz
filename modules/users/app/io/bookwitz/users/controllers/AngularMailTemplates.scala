package io.bookwitz.users.controllers

import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import play.api.templates.{Html}
import securesocial.controllers.MailTemplates
import securesocial.core.BasicProfile
import play.twirl.api.Txt

/**
  * @author Joseph Dessens
  * @since 2014-09-03
  */
object AngularMailTemplates extends MailTemplates {
  override def getSignUpEmail(token: String)(implicit request: RequestHeader, lang: Lang): (Option[Txt], Option[Html]) = {
    (None, Option(Html("Go to http://" + request.host + "/#/signup/" + token)))
  }

  override def getSendPasswordResetEmail(user: BasicProfile, token: String)(implicit request: RequestHeader, lang: Lang): (Option[Txt], Option[Html]) = {
    (None, Option(Html("Go to http://" + request.host + "/#/password/reset/" + token)))
  }

  override def getWelcomeEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang): (Option[Txt], Option[Html]) = {
    (None, Option(Html("Go to http://" + request.host)))
  }

  override def getAlreadyRegisteredEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang): (Option[Txt], Option[Html]) = {
    (None, None)
  }

  override def getUnknownEmailNotice()(implicit request: RequestHeader, lang: Lang): (Option[Txt], Option[Html]) = {
    (None, None)
  }

  override def getPasswordChangedNoticeEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang): (Option[Txt], Option[Html]) = {
    (None, None)
  }
}
