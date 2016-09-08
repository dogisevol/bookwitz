package io.bookwitz.web.models

case class UserWord(userId: Option[Long], word: String, note: Option[String]) {

  override def equals(that: Any): Boolean =
    that match {
      case that: UserWord =>
        this.word.hashCode == that.word.hashCode
      case _ => false
    }

  override def hashCode: Int = {
    word.hashCode
  }
}
