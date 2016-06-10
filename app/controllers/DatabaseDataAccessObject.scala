package controllers

import javax.inject.Inject

import models.{Log, LogTable, StatsMsg, StatsTable}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future


/**
  * Created by Cash on 3/12/2016.
  */
class DatabaseDataAccessObject @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  object DataTypes extends Enumeration {
    type DataTypes = Value
    val Log, Stats = Value
  }

  private val Logs = TableQuery[LogTable]
  private val Stats = TableQuery[StatsTable]

  def all(datatype: DataTypes.Value): Future[Seq[Any]] = datatype match {
    case DataTypes.Log   => db.run(Logs.result)
    case DataTypes.Stats => db.run(Stats.result)
  }

  def insert(log: Log): Future[Unit] = db.run(Logs += log).map { _ => () }
  def insert(stat: StatsMsg): Future[Unit] = db.run(Stats += stat).map { _ => () }
  //def remove(log: Log): Future[Unit] = db.run(Logs -= log).map { _ => () }
  /* Table mapping
   */

}
