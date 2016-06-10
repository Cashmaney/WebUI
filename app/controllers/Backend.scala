package controllers

import javax.inject.{Inject, Singleton}

import com.github.nscala_time.time.Imports._
import models._
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.libs.ws._
import play.api.mvc._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration




@Singleton
class Backend @Inject() (logdao: DatabaseDataAccessObject, WS: WSClient) extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global
  val BackendLogger: Logger = Logger("Backend")
  //  private val Logs = TableQuery[LogTable]
  val logForm = Form(
    mapping(
      "level" -> number,
      "code" -> number
    )(Log.apply)(Log.unapply)
  )

  val StatsForm = Form(
    mapping(
      "Name" -> text,
      "Value" -> text,
      "Time" -> text
    )(StatsMsg.apply)(StatsMsg.unapply)
  )

  /*  def page1 = Action.async {
      val x = logdao.DataTypes.Log
      val y = logdao.DataTypes.Stats
      logdao.all(x).map {case (logs) => Ok(views.html.page1(logs.asInstanceOf[Seq[Log]]) ) }
    }*/



  def insertLog(rec_type_as_string: String) = Action.async { implicit request =>
    val reqtype = StringToRequestType(rec_type_as_string)
    reqtype match {
      case RequestType.Log =>
        val log: Log = logForm.bindFromRequest.get
        logdao.insert(log).map(_ => Redirect(routes.Frontend.index()))

      case RequestType.Stats =>
        val stat: StatsMsg = StatsForm.bindFromRequest.get
        logdao.insert(stat).map(_ => Redirect(routes.Frontend.index()))


    }

  }

  def index = Action {
    BackendLogger.info("index: called")
    //logger.info("Serving index page...")
    val logsresult = Await.result(logdao.all(logdao.DataTypes.Log),Duration.Inf)
    val statsresult =  Await.result(logdao.all(logdao.DataTypes.Stats),Duration.Inf)
    Ok(views.html.index(logsresult.asInstanceOf[Seq[Log]], statsresult.asInstanceOf[Seq[StatsMsg]] ) )
    //logdao.all().map {case (logs) => Ok(views.html.index(logs) }
  }


  //Gets a request containing a request type. Returns XML response: either logs or stats
  def MakeLogRes = Action(parse.xml) { request =>
    try {
      BackendLogger.info("MakeLogRes: called")
      if ((request.body \ "Type").text != "Request") throw new IllegalArgumentException("Type:" + (request.body \ "Type").text)

      val receivedtype = request.body \\ "Subtype"

      val data = receivedtype.text match {
        case "Log" => CreateLogResponse(DateTime.now(), RequestType.Log).toXml
        case "Stats" => CreateLogResponse(DateTime.now(), RequestType.Stats).toXml
        case _ => throw new IllegalArgumentException("Subtype:" + receivedtype.text)
      }

      Ok(data.toString())
    }
    catch {
      case t: IllegalArgumentException =>
        // Log error with message and Throwable.
        BackendLogger.info("Malformed Request, illegal argument exception", t)
        InternalServerError("Malformed Request")
    }

  }

  //converts a string to a RequestType. There is probably a better way of doing this, but whatever
  def StringToRequestType(reqtype: String): RequestType.Value = reqtype match {
    case "Log" => RequestType.Log
    case "Stats" => RequestType.Stats
    case _ => RequestType.Log
  }


  //LogXML class. Should probably be in the models...
  case class LogXML(time: DateTime, loglevel: LogLevel.Value, errorcode: Int) {
    def toXml =
      <Time>{ time }</Time>
        <Level> { loglevel } </Level>
        <Code> { errorcode } </Code>
  }

  //creates the LogResponse XML type. Should probably be in models...
  case class CreateLogResponse(time: DateTime, reqtype: RequestType.Value) {
    BackendLogger.info("CreateLogResponse: called")
    def toXml: xml.Elem = reqtype match {
      case RequestType.Log =>
        <Message>
          <Type>Response</Type>
          <Subtype>Log</Subtype>
          <ResponseTime>{ time }</ResponseTime>
          { LoglistToXml }
        </Message>

      case RequestType.Stats =>
        <Message>
          <Type>Response</Type>
          <Subtype>Stats</Subtype>
          <ResponseTime>{ time }</ResponseTime>
          { StatslistToXml }
        </Message>

      case _ =>
        <Error>Invalid Request</Error>


    }

  }
  object LogLevel extends Enumeration {
    type LogLevel = Value
    val Warning, Error, Debug = Value
  }

  object RequestType extends Enumeration {
    type RequestType = Value
    val Log, Stats = Value
  }


  //Backend function. Gets all logs of type[whatever] from the database.
  def GetLogsFromDB(): Future[Seq[Log]] = {
    BackendLogger.info("GetLogsFromDB: called")
    for { singlelog <-  logdao.all(logdao.DataTypes.Log)
    } yield singlelog.asInstanceOf[Seq[Log]]
  }

  //Backend function. Gets all logs of type[whatever] from the database.
  def GetStatsFromDB(): Future[Seq[StatsMsg]] = {
    BackendLogger.info("GetStatsFromDB: called")
    for { singlelog <-  logdao.all(logdao.DataTypes.Stats)
    } yield singlelog.asInstanceOf[Seq[StatsMsg]]
  }

  //Converts LogListXML to an actualy XML payload before sending it. LogListXML a list of type [Log] which is the
  //type stored in the DB (well, one of them anyway).
  //This is used usually after getting the data from the database.
  def LoglistToXml: xml.Elem = {
    BackendLogger.info("LoglistToXml: called")
    val list = Await.result(GetLogsFromDB(),Duration.Inf)
    <Datalist>
      { for { singlelog <- list
    } yield singlelog.toXml }
    </Datalist>
  }



  //Converts LogListXML to an actualy XML payload before sending it. LogListXML a list of type [Log] which is the
  //type stored in the DB (well, one of them anyway).
  //This is used usually after getting the data from the database.
  def StatslistToXml: xml.Elem = {
    BackendLogger.info("StatslistToXml: called")
    val list = Await.result(GetStatsFromDB(),Duration.Inf)
    <Datalist>
      { for { singlelog <- list
    } yield singlelog.toXml }
    </Datalist>
  }
}
