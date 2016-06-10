package models



/**
  * Created by Cash on 3/1/2016.
  */

import play.api.libs.json._
import slick.driver.MySQLDriver.api._

case class Log(Level: Int, Code: Int)
{
  def toXml: xml.Elem = {
    <Log>
      <Level>{Level}</Level>
      <Code>{Code}</Code>
    </Log>
  }

  def toJSON: JsObject = JsObject(Seq(
    "level" -> JsNumber(Level),
    "code" -> JsNumber(Code)
  ))

}
/* Table mapping
 */
class LogTable(tag: Tag) extends Table[Log](tag, "LOG") {

  def level = column[Int]("level", O.PrimaryKey)
  def code = column[Int]("code", O.PrimaryKey)

  def * = (level, code) <> (Log.tupled, Log.unapply _)
}

case class StatsMsg(Name: String, Value: String, Time: String)
{
  def toXml: xml.Elem = {
    <Stats>
      <Name>{Name}</Name>
      <Value>{Value}</Value>
      <Time>{Time}</Time>
    </Stats>
  }

  def toJSON: JsObject = JsObject(Seq(
      "name" -> JsString(Name),
      "value" -> JsString(Value),
      "time" -> JsString(Time)
  ))




}
/* Table mapping
 */
class StatsTable(tag: Tag) extends Table[StatsMsg](tag, "Stats") {

  def name = column[String]("Name", O.PrimaryKey)
  def value = column[String]("Value")
  def time = column[String]("Time")
  def * = (name, value, time) <> (StatsMsg.tupled, StatsMsg.unapply _)
}