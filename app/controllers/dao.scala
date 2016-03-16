package dao

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile

import models.{LogTable, Log}
import play.api.{db, Play}
import slick.driver.JdbcProfile

import scala.concurrent.Future

import slick.driver.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig

/**
  * Created by Cash on 3/12/2016.
  */
class LogDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  private val Logs = TableQuery[LogTable]

  def all(): Future[Seq[Log]] = db.run(Logs.result)

  def insert(log: Log): Future[Unit] = db.run(Logs += log).map { _ => () }
  /* Table mapping
   */

}
