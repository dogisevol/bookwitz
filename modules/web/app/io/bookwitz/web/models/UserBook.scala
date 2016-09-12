package io.bookwitz.web.models

case class UserBook(userId: Option[Long], book: String, title: Option[String])
