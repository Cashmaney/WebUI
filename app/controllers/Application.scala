package controllers

import models._
import dao.LogDAO

import java.io.File
import javax.inject.{Singleton, Inject}
import akka.event.Logging.LogLevel

import play.api.Play
import play.api.Play.current

import org.slf4j.{LoggerFactory, Logger}
import play.api.mvc._

import play.api.libs.ws._
import play.api.libs.ws.ning.NingAsyncHttpClientConfigBuilder
import scala.concurrent.Future
import scala.xml.Elem
import scala.xml.NodeSeq
import com.github.nscala_time.time.Imports._
import play.api.db.slick._

import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import play.api.data._
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig

import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms._

@Singleton
class Application @Inject() (logdao: LogDAO) extends Controller {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[Application])
  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  private val Logs = TableQuery[LogTable]

  val logForm = Form(
    mapping(
      "level" -> number,
      "code" -> number
    )(Log.apply)(Log.unapply)
  )

  def page1 = Action.async {
    logdao.all().map {case (logs) => Ok(views.html.page1(logs)) }
  }



  def insertLog = Action.async { implicit request =>
    val log: Log = logForm.bindFromRequest.get
    logdao.insert(log).map(_ => Redirect(routes.Application.page1))
  }

  def index = Action {
    logger.info("Serving index page...")

    Ok(views.html.index())
  }

  def sayHello = Action(parse.xml) { request =>
    (request.body \\ "name" headOption).map(_.text).map { name =>
      Ok(<message status="OK">Hello {name}</message>)
    }.getOrElse {
      BadRequest(<message status="KO">Missing parameter [name]</message>)
    }
  }

  def makereq = Action.async { request =>
    val data = <name>Steve</name>
    val futureResult: Future[NodeSeq] = WS.url("http://localhost:9000/hello").post(data).map { response =>
      scala.xml.XML.loadString(response.body)}

    futureResult.map { message =>
      Ok(message)
    }

  }

  def MakeLogRes = Action(parse.xml) { request =>
    val data = CreateLogResponse(DateTime.now(), LogListXML).toXml
    Ok(data.toString())
  }


  def MakeLogReq = Action.async { request =>
    val data = CreateLogRequest(DateTime.now()).toXml
    //println(data.toString())
    val futureResult: Future[NodeSeq] = WS.url("http://localhost:9000/LogService").post(data).map { response =>
      println(response.body)
      scala.xml.XML.loadString(response.body)}

    futureResult.map { message =>
      Ok(message)
    }

  }

  object Log_Level extends Enumeration {
    val Warning, Error, Debug = Value
  }


  val LogListXML = List(
      new LogXML(DateTime.now(), Log_Level.Warning, 16),
      new LogXML(DateTime.now(), Log_Level.Error, 28)
    )

  case class LogXML(val time: DateTime, val loglevel: Log_Level.Value, val errorcode: Int) {
    def toXml() =
      <Time>{ time }</Time>
      <Level> { loglevel } </Level>
      <Code> { errorcode } </Code>
  }

  case class CreateLogRequest(val time: DateTime) {
    def toXml: xml.Elem =
      <Message><Type>Request</Type><Subtype>Log</Subtype><RequestTime>{ time }</RequestTime></Message>
  }

  case class CreateLogResponse(val time: DateTime, val data:List[LogXML]) {
    def toXml: xml.Elem =
      <Message>
        <Type>Response</Type>
        <Subtype>Log</Subtype>
        <ResponseTime>{ time }</ResponseTime>
        { LoglistToXml(data) }
      </Message>
  }

  def LoglistToXml(LogListXML: List[LogXML]): xml.Elem = {
    <LogList>{
      for {singlelog <- LogListXML }
        yield singlelog.toXml()
      }</LogList>
  }


}
