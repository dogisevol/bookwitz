import java.net.{HttpURLConnection, InetSocketAddress, Proxy, URL}
import java.sql.{Connection, DriverManager, ResultSet}

import org.sqlite.SQLiteConfig

import scala.reflect.io.File

/**
  * Created by AVEKAUA on 13/07/2016.
  */
object Util {
  def main(args: Array[String]): Unit = {
    val config = new SQLiteConfig()

    val sqlConnection = DriverManager.getConnection("jdbc:sqlite:db/../db/bookwitz.sqlite", config.toProperties)
    val resultSet: ResultSet = sqlConnection.createStatement().executeQuery("select id, word from dictionary order by id")
    val list = new RsIterator(resultSet).map(x => {
      (x.getString("word"), x.getLong("id"))
    }).toList
    var offset = 0
    var i = offset
    list.foreach(
      item => {
        i = i + 1
        if (i % 100 != 0) print(".") else println(i)
        if (i % 1000 == 0) offset = i / 1000
        if (i > 230911) {
          if (!connect(item._1, item._2, sqlConnection, offset)) {
            Thread sleep 5000
            connect(item._1, item._2, sqlConnection, offset)
          }
        }
      }
    )
  }

  def connect(word: String, wordId: Long, sqlConnection: Connection, offset: Int): Boolean = {
    try {
      val proxy: Proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8889))
      var connection: HttpURLConnection =
        new URL("http://api.wordnik.com:80/v4/word.json/" + word + "/definitions?api_key=a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5&limit=5")
          .openConnection().asInstanceOf[HttpURLConnection]
      connection.setRequestMethod("GET");
      connection.setRequestProperty("User-Agent", "Mozilla/5.0");
      if (200 == connection.getResponseCode()) {
        File("D:/work/dictionary/definitions/" + offset).appendAll("{\"wordId\":" + wordId + ", \"word\":\"" + word + "\", \"data\": " + scala.io.Source.fromInputStream(connection.getInputStream).mkString + "},\n")
        //      val result = Json.parse(scala.io.Source.fromInputStream(connection.getInputStream).mkString)
        //      (result \\ "text").foreach(
        //        text => {
        //          val w = text.toString().replaceAll("\\\\\"", "'");
        //          sqlConnection.prepareStatement("insert into dictDefinitions values (" + wordId + ", " + w + ", NULL)").executeUpdate()
        //        }
        //      )
      }

      connection =
        new URL("http://api.wordnik.com:80/v4/word.json/" + word + "/pronunciations?api_key=a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5&limit=5")
          .openConnection().asInstanceOf[HttpURLConnection]
      connection.setRequestMethod("GET");
      connection.setRequestProperty("User-Agent", "Mozilla/5.0");
      if (200 == connection.getResponseCode()) {
        File("D:/work/dictionary/pronunciations/" + offset).appendAll("{\"wordId\":" + wordId + ", \"word\":\"" + word + "\", \"data\": " + scala.io.Source.fromInputStream(connection.getInputStream).mkString + "},\n")
        //      val result = Json.parse(scala.io.Source.fromInputStream(connection.getInputStream).mkString)
        //      (result \\ "raw").foreach(
        //        text => {
        //          val w = text.toString().replaceAll("\\\\\"", "'");
        //          sqlConnection.prepareStatement("insert into dictPronunciations values (NULL, " + wordId + ", " + w + ")").executeUpdate()
        //        }
        //      )
      }

      connection =
        new URL("http://api.wordnik.com:80/v4/word.json/" + word + "/topExample?api_key=a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5&limit=5")
          .openConnection().asInstanceOf[HttpURLConnection]
      connection.setRequestMethod("GET");
      connection.setRequestProperty("User-Agent", "Mozilla/5.0");
      if (200 == connection.getResponseCode()) {
        File("D:/work/dictionary/examples/" + offset).appendAll("{\"wordId\":" + wordId + ", \"word\":\"" + word + "\", \"data\": " + scala.io.Source.fromInputStream(connection.getInputStream).mkString + "},\n")
        //      val result = Json.parse(scala.io.Source.fromInputStream(connection.getInputStream).mkString)
        //      (result \\ "text").foreach(
        //        text => {
        //          val w = text.toString().replaceAll("\\\\\"", "'");
        //          sqlConnection.prepareStatement("insert into dictExamples values (" + wordId + ", " + w + ")").executeUpdate()
        //        }
        //      )
      }
      true
    } catch {
      case e: Exception => false
    }
  }
}


class RsIterator(rs: ResultSet) extends Iterator[ResultSet] {
  def hasNext: Boolean = rs.next()

  def next(): ResultSet = rs
}

