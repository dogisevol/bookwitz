package io.bookwitz.users.models

import securesocial.core.{AuthenticationMethod, BasicProfile, UserProfile}
import io.bookwitz.users.models.UserTableQueries.{passwords, oauth2s, oauth1s}

import scala.slick.driver.JdbcDriver.simple._
/**
 * @author Joseph Dessens
 * @since 2014-09-01
 */
case class Profile(id: Option[Long] = None,
                        providerId: String,
                        userId: String,
                        firstName: Option[String] = None,
                        lastName: Option[String] = None,
                        fullName: Option[String] = None,
                        email: Option[String] = None,
                        avatarUrl: Option[String] = None,
                        authMethod: String,
                        oAuth1Id: Option[Long] = None,
                        oAuth2Id: Option[Long] = None,
                        passwordId: Option[Long] = None) extends UserProfile {
  def basicProfile(implicit session: Session): BasicProfile = {
    BasicProfile(
      providerId,
      userId,
      firstName,
      lastName,
      fullName,
      email,
      avatarUrl,
      authMethod match {
        case "oauth1" => AuthenticationMethod.OAuth1
        case "oauth2" => AuthenticationMethod.OAuth2
        case "openId" => AuthenticationMethod.OpenId
        case "userPassword" => AuthenticationMethod.UserPassword
      },
      oauth1s.filter(_.id === oAuth1Id).firstOption.map(o1 => o1.oAuth1Info),
      oauth2s.filter(_.id === oAuth2Id).firstOption.map(o2 => o2.oAuth2Info),
      passwords.filter(_.id === passwordId).firstOption.map(p => p.passwordInfo)
    )
  }
}

class Profiles(tag: Tag) extends Table[Profile](tag, "profile") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def providerId = column[String]("provider_id")
  def userId = column[String]("user_id")
  def firstName = column[Option[String]]("first_name")
  def lastName = column[Option[String]]("last_name")
  def fullName = column[Option[String]]("full_name")
  def email = column[Option[String]]("email")
  def avatarUrl = column[Option[String]]("avatar_url")
  def authMethod = column[String]("auth_method")
  def oAuth1Id = column[Option[Long]]("oauth1_id")
  def oAuth2Id = column[Option[Long]]("oauth2_id")
  def passwordId = column[Option[Long]]("password_id")

  def * = (
    id.?,
    providerId,
    userId,
    firstName,
    lastName,
    fullName,
    email,
    avatarUrl,
    authMethod,
    oAuth1Id,
    oAuth2Id,
    passwordId
    ) <> (Profile.tupled, Profile.unapply)

  def idk = index("profile_idx", (providerId, userId))
}

