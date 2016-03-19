package controllers

import models._
import dao.LogDAO
import java.io.File
import javax.inject.{Inject, Singleton}

import akka.event.Logging.LogLevel
import play.api.Play
import play.api.Play.current
import org.slf4j.{Logger, LoggerFactory}
import play.api.mvc._
import play.api.libs.ws._

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.duration.Deadline

import scala.xml.Elem
import scala.xml.NodeSeq
import com.github.nscala_time.time.Imports._
import com.sun.org.glassfish.external.statistics.Stats
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
class Application @Inject() (logdao: LogDAO, WS: WSClient) extends Controller {

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



  def insertLog() = Action.async { implicit request =>
    val log: Log = logForm.bindFromRequest.get
    logdao.insert(log).map(_ => Redirect(routes.Application.page1()))
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
    val futureResult: Future[NodeSeq] = WS.url("http://192.168.248.131/hello").post(data).map { response =>
      scala.xml.XML.loadString(response.body)}

    futureResult.map { message =>
      Ok(message)
    }

  }

  def MakeLogRes = Action(parse.xml) { request =>
    val receivedtype = request.body \\ "Subtype"

    val data = receivedtype.text match {
      case "Log" => CreateLogResponse(DateTime.now(), LogListXML, RequestType.Log).toXml
      case "Stats" => CreateLogResponse(DateTime.now(), LogListXML, RequestType.Stats).toXml
      case _ =>  <Error>Invalid Request</Error>
    }
    println(receivedtype.text)

    Ok(data.toString())
  }

  def ToRequestType(reqtype: String): RequestType.Value = reqtype match {
    case "Log" => RequestType.Log
    case "Stats" => RequestType.Stats
    case _ => RequestType.Log
  }

  def MakeLogReq(reqtype: String) = Action.async { request =>
    val data = CreateLogRequest(DateTime.now(), ToRequestType(reqtype)).toXml
    //println(data.toString())
    val futureResult: Future[NodeSeq] = WS.url("http://192.168.248.131/LogService").post(data).map { response =>
      //println(response.body)
      scala.xml.XML.loadString(response.body)}

    futureResult.map { message =>
      Ok(message)
    }

  }

  val LogListXML = List(
      new LogXML(DateTime.now(), LogLevel.Warning, 16),
      new LogXML(DateTime.now(), LogLevel.Error, 28)
    )

  case class LogXML(time: DateTime, loglevel: LogLevel.Value, errorcode: Int) {
    def toXml =
      <Time>{ time }</Time>
      <Level> { loglevel } </Level>
      <Code> { errorcode } </Code>

  }

  case class CreateLogRequest(time: DateTime, reqtype: RequestType.Value) {
    def toXml: xml.Elem =
      <Message>
        <Type>Request</Type>
        <Subtype>{reqtype}</Subtype>
        <RequestTime>{time}</RequestTime>
      </Message>
  }

  case class CreateLogResponse(time: DateTime, data:List[LogXML], reqtype: RequestType.Value) {
    def toXml: xml.Elem = reqtype match {
      case RequestType.Log =>  {
         <Message>
            <Type>Response</Type>
            <Subtype>Log</Subtype>
            <ResponseTime>{ time }</ResponseTime>
            { LoglistToXml(data) }
          </Message>
        }
      case RequestType.Stats => {
         <Message>
            <Type>Response</Type>
            <Subtype>Stats</Subtype>
            <ResponseTime>{ time }</ResponseTime>
            <Placeholder> Stats </Placeholder>
          </Message>
        }
      case _ => {
        <Error>Invalid Request</Error>
      }

    }

  }

  def GetLogsFromDB(): Future[Seq[Log]] = {
    for { singlelog <-  logdao.all()
    } yield singlelog
  }


  def LoglistToXml(LogListXML: List[LogXML]): xml.Elem = {
    val list = Await.result(GetLogsFromDB(),Duration.Inf)
    <Loglist>
      { for { singlelog <- list
    } yield singlelog.toXml }
    </Loglist>
  }

  object LogLevel extends Enumeration {
    type LogLevel = Value
    val Warning, Error, Debug = Value
  }

  object RequestType extends Enumeration {
    type RequestType = Value
    val Log, Stats = Value
  }

}

//def RequestXML (val reqtype: String, ) {
//}
