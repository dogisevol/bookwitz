package test

object Test {

  case class Item(name: String, note: String)

  def main(args: Array[String]): Unit = {
    val list = List[Item](Item("test", "test"), Item("test1", "test1"))
    val item = list.find(_.name == "test").getOrElse(Item("test4", "test4"))
    val list1= list.updated(list.indexOf(item), Item(item.name, "test3"))
    print(list1)
  }
}
