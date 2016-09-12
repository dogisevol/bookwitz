package io.bookwitz.web.models

import scala.slick.lifted.TableQuery

object BooksTableQueries {

  object booksList extends TableQuery(new Books(_))

  object userWordsList extends TableQuery(new UserWords(_))

  object userBooksList extends TableQuery(new UserBooks(_))

  object dictionaryWordsList extends TableQuery(new WordDictionaries(_))

  object wordDefinitionsList extends TableQuery(new WordDefinitions(_))

  object wordPronunciationList extends TableQuery(new WordPronunications(_))

  object wordExamplesList extends TableQuery(new WordExamples(_))

}
