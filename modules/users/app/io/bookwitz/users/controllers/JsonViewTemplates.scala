package io.bookwitz.users.controllers

import play.api.data.Form
import play.api.i18n.Lang
import play.api.libs.json.Json
import play.api.libs.json.Json.JsValueWrapper
import play.api.mvc.RequestHeader
import securesocial.controllers.{ChangeInfo, RegistrationInfo, ViewTemplates}
import play.twirl.api.Html


object JsonViewTemplates extends ViewTemplates {
  def toHtml(fields: (String, JsValueWrapper)): Html = {
    Html(Json.stringify(Json.obj(fields)))
  }

  def toHtmlRep(fields: (String, JsValueWrapper)): play.twirl.api.Html = {
    play.twirl.api.Html(Json.stringify(Json.obj(fields)))
  }

  def processForm[T](form: Form[T]): Html = {
    if (form.hasErrors) Html(form.errorsAsJson.toString()) else Html("")
  }

  override def getLoginPage(form: Form[(String, String)], msg: Option[String] = None)(implicit request: RequestHeader, lang: Lang): Html = {
    processForm(form)
  }

  override def getSignUpPage(form: Form[RegistrationInfo], token: String)(implicit request: RequestHeader, lang: Lang): Html = {
    processForm(form)
  }

  override def getStartSignUpPage(form: Form[String])(implicit request: RequestHeader, lang: Lang): Html = {
    processForm(form)
  }

  override def getStartResetPasswordPage(form: Form[String])(implicit request: RequestHeader, lang: Lang): Html = {
    toHtml("email" -> form.value)
  }

  override def getResetPasswordPage(form: Form[(String, String)], token: String)(implicit request: RequestHeader, lang: Lang): Html = {
    processForm(form)
  }

  override def getPasswordChangePage(form: Form[ChangeInfo])(implicit request: RequestHeader, lang: Lang): Html = {


    processForm(form)
  }

  def getNotAuthorizedPage(implicit request: RequestHeader, lang: Lang): Html = {
    Html("404")
  }
}
