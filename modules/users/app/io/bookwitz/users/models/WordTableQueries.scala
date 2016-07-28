package io.bookwitz.users.models

import io.bookwitz.users.models.words.Words

import scala.slick.lifted.TableQuery

object WordTableQueries {

  object wordsList extends TableQuery(new Words(_))

}
