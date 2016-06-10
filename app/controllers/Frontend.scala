package controllers

import javax.inject.{Inject, Singleton}

import com.github.nscala_time.time.Imports._
import models._
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.xml.NodeSeq





@Singleton
class Frontend @Inject() (logdao: DatabaseDataAccessObject, WS: WSClient) extends Controller {
 // val ApplicationLogger = Logger(this.getClass)
  val BackendAddress = "http://localhost:9000"
  val FrontendLogger: Logger = Logger("Frontend")
  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  //Main Actions (A.K.A HTTP Methods) ********************************

  //Returns Data in JSON format. Either statistic data or log data.
  //This function is the connector to Angular-JS - it queries this method to get the data.
  def ReturnData(reqtype: String) = Action { _ =>
    FrontendLogger.debug("ReturnData: called" + "; param=\"" + reqtype + "\"")
    Ok(DataListJSON(StringToRequestType(reqtype)))
  }

  def main = Action {
    FrontendLogger.info("main: called")
    Ok(views.html.indexnew() )
  }




  def index = Action {
    FrontendLogger.info("index: called")
    val logsresult = Await.result(logdao.all(logdao.DataTypes.Log),Duration.Inf)
    val statsresult =  Await.result(logdao.all(logdao.DataTypes.Stats),Duration.Inf)
    Ok(views.html.index(logsresult.asInstanceOf[Seq[Log]], statsresult.asInstanceOf[Seq[StatsMsg]] ) )
    //logdao.all().map {case (logs) => Ok(views.html.index(logs) }
  }

  // *************************************************


  def GetStatsFromBackend: Seq[StatsMsg] =
  {
    FrontendLogger.debug("GetStatsFromBackend: called")
    val data = CreateLogRequest(DateTime.now(), StringToRequestType("Stats")).toXml
    //println(data.toString())
    val futureResult: Future[NodeSeq] = WS.url(BackendAddress + "/DataService").post(data).map { response =>
      //println(response.body)
      scala.xml.XML.loadString(response.body)}

    val data_from_backend = Await.result(futureResult, Duration.Inf)
    FrontendLogger.debug("GetStatsFromBackend, data_from_backend =" + data_from_backend)

    val logs:Seq[StatsMsg] =
      for { x <- data_from_backend \ "Datalist"  \ "Stats"
      } yield StatsMsg((x \ "Name").text, (x \ "Value").text, (x \ "Time").text)

    logs
  }

  def GetLogsFromBackend: Seq[Log] =
  {
    FrontendLogger.info("GetLogsFromBackend: called")
    val data = CreateLogRequest(DateTime.now(), StringToRequestType("Log")).toXml
    //println(data.toString())
    val futureResult: Future[NodeSeq] = WS.url(BackendAddress+"/DataService").post(data).map { response =>
      //println(response.body)
      scala.xml.XML.loadString(response.body)}

    val data_from_backend = Await.result(futureResult, Duration.Inf)

    FrontendLogger.trace("GetLogsFromBackend, data_from_backend =" + data_from_backend)
    val logs:Seq[Log] =
      for { x <- data_from_backend \ "Datalist" \"Log"
      } yield Log((x \ "Level").text.toInt, (x \ "Code").text.toInt)

    logs
  }

  //creates the LogRequest XML type. Should probably be in models...
  case class CreateLogRequest(time: DateTime, reqtype: RequestType.Value) {
    def toXml: xml.Elem =
      <Message>
        <Type>Request</Type>
        <Subtype>{reqtype}</Subtype>
        <RequestTime>{time}</RequestTime>
      </Message>
  }

//Backend connector. Get[Logs/Stats]FromBackend creates the POST request and returns the data in sexy
// Log/StatsMsg form...
  def DataListJSON(reqtype: RequestType.Value): JsArray = {
  FrontendLogger.debug("DataListJSON: called" + "; param=\"" + reqtype + "\"")
    val list:JsArray = reqtype match
    {
      case RequestType.Log =>
        //val loglist = Await.result(GetLogsFromDB(),Duration.Inf)

        val loglist = GetLogsFromBackend
        val jsonlist: Seq[JsObject] = for { x <- loglist
      } yield x.toJSON
        jsonlist.foldLeft(JsArray())((acc, x) => acc ++ Json.arr(x))

      case RequestType.Stats =>
        val loglist = GetStatsFromBackend
        val jsonlist: Seq[JsObject] = for { x <- loglist
        } yield x.toJSON
        jsonlist.foldLeft(JsArray())((acc, x) => acc ++ Json.arr(x))
    }
    list
  }

  object LogLevel extends Enumeration {
    type LogLevel = Value
    val Warning, Error, Debug = Value
  }

  object RequestType extends Enumeration {
    type RequestType = Value
    val Log, Stats = Value
  }
  //converts a string to a RequestType. There is probably a better way of doing this, but whatever
  def StringToRequestType(reqtype: String): RequestType.Value = reqtype match {
    case "Log" => RequestType.Log
    case "Stats" => RequestType.Stats
    case _ => RequestType.Log
  }
}

//def RequestXML (val reqtype: String, ) {
//}
