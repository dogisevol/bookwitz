package test

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

object Test {

  case class Item(definitions: ListBuffer[String], pronunciations: ListBuffer[String], examples: ListBuffer[String])

  def main(args: Array[String]): Unit = {

    val a = Await.result(getDictionaryEntry("test"), 2 second)
    print(a)

  }

  def getDefinitions(word: String): Future[String] = Future.successful {
    Thread.sleep(1000)
    "def"
  }

  def getTopExample(word: String): Future[String] = Future.successful {
    Thread.sleep(1000)
    "exmpl"
  }

  def getPronunciations(word: String): Future[String] = Future.successful {
    Thread.sleep(1000)
    "prn"
  }

  def getDictionaryEntry(word: String): Future[Item] = {
    val result = Item(ListBuffer[String](), ListBuffer[String](), ListBuffer[String]())
    getDefinitions(word).flatMap(definition => {
      result.definitions += definition
      getPronunciations(word).flatMap(wordPronunciation => {
        result.pronunciations += wordPronunciation
        getTopExample(word).map(wordExample => {
          result.examples += wordExample
          result
        }).recover {
          case e: Exception =>
            throw e
        }
      }
      )
    }
    )
  }

}
