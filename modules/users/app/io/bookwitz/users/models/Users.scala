package io.bookwitz.users.models

import io.bookwitz.users.models.UserTableQueries.profiles

import scala.slick.driver.JdbcDriver.simple._

/**
  * @author Joseph Dessens
  * @since 2014-09-01
  */
case class User(id: String, mainId: Long) {
  def basicUser(implicit session: Session): BasicUser = {
    val main = profiles.filter(_.id === mainId).first
    val identities = profiles.filter(p => p.userId === id && p.id =!= mainId).list

    BasicUser(main.basicProfile, identities.map(i => i.basicProfile))
  }
}

class Users(tag: Tag) extends Table[User](tag, "user") {
  def id = column[String]("id", O.PrimaryKey)

  def mainId = column[Long]("main_id")

  def * = (id, mainId) <>(User.tupled, User.unapply)
}
