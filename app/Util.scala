import java.net.{HttpURLConnection, URL}
import java.sql.{DriverManager, ResultSet}

import org.sqlite.SQLiteConfig
import play.api.libs.json.Json

/**
  * Created by AVEKAUA on 13/07/2016.
  */
object Util {
  def main(args: Array[String]): Unit = {
    println("hello")
    val config = new SQLiteConfig();

    val connection = DriverManager.getConnection("jdbc:sqlite:db/../db/bookwitz.sqlite", config.toProperties)
    val resultSet: ResultSet = connection.prepareStatement("select * from words").executeQuery()
    val list = new RsIterator(resultSet).map(x => {
      (x.getString("word"))
    }).toList

    //    list.foreach(
    //      item =>
    //        print(item)
    //    )

    connect("test")

  }

  def connect(word: String): Unit = {
    var connection: HttpURLConnection =
      new URL("http://api.wordnik.com:80/v4/word.json/" + word + "/definitions?api_key=a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5&limit=5")
        .openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET");
    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
    connection.getResponseCode();
    var result = Json.parse(scala.io.Source.fromInputStream(connection.getInputStream).mkString)
    (result \\ "text").foreach(
      text =>
        println(text)
    )

    connection =
      new URL("http://api.wordnik.com:80/v4/word.json/" + word + "/definitions?api_key=a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5&limit=5")
        .openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET");
    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
    connection.getResponseCode();
    result = Json.parse(scala.io.Source.fromInputStream(connection.getInputStream).mkString)
    (result \\ "text").foreach(
      text =>
        println(text)
    )

    connection =
      new URL("http://api.wordnik.com:80/v4/word.json/" + word + "/definitions?api_key=a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5&limit=5")
        .openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET");
    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
    connection.getResponseCode();
    result = Json.parse(scala.io.Source.fromInputStream(connection.getInputStream).mkString)
    (result \\ "text").foreach(
      text =>
        println(text)
    )
  }
}


class RsIterator(rs: ResultSet) extends Iterator[ResultSet] {
  def hasNext: Boolean = rs.next()

  def next(): ResultSet = rs
}

