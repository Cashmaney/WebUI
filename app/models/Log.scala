package models

/**
  * Created by Cash on 3/1/2016.
  */
import play.api.db.slick._
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

case class Log(Level: Int, Code: Int)

/* Table mapping
 */
class LogTable(tag: Tag) extends Table[Log](tag, "LOG") {

  def level = column[Int]("level", O.PrimaryKey)
  def code = column[Int]("code", O.PrimaryKey)

  def * = (level, code) <> (Log.tupled, Log.unapply _)
}