package controllers.api

import com.fasterxml.jackson.annotation.JsonValue
import com.google.inject.Inject
import dao.{LaboratoryDAO, UserDAO}
import jp.t2v.lab.play2.auth.OptionalAuthElement
import model._
import model.json.ModelWrites._
import model.form.data.LoginFormData
import play.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.{Action, Controller}
import services.LaboratoryService
import views.html._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Camilo Sampedro <camilo.sampedro@udea.edu.co>
  */
class LaboratoryController @Inject()(userDAO: UserDAO, laboratoryService: LaboratoryService, val messagesApi: MessagesApi) extends Controller with I18nSupport {



  //override def resolveUser(id: LoginFormData)(implicit context: ExecutionContext): Future[Option[User]] = userDAO.get(id)

  def convertToJson(laboratoryObject: Laboratory, roomsWithComputers: Map[Option[Room], Seq[(Computer, Option[(ComputerState, Seq[ConnectedUser])])]]): JsValue = {
    val roomsConverted = roomsWithComputers.toSeq
    val grouped = roomsConverted.groupBy(_._1)
    val hasRooms = grouped.filter(_._1.isDefined)
    val resultRooms: Seq[(Room, Seq[(Computer, Option[(ComputerState, Seq[ConnectedUser])])])] = hasRooms.map(filtered=>(filtered._1.get,filtered._2.map(_._2).head)).toSeq
    Json.toJson((laboratoryObject,resultRooms))
  }

  def get(id: Long) = Action.async { implicit request =>
    Logger.debug("Petición de listar el laboratory " + id + " [API] respondida.")
    implicit val username = Some("")
    laboratoryService.get(id).map {
      case Some((laboratoryObject, roomsWithComputers)) => Ok(convertToJson(laboratoryObject, roomsWithComputers))
      case _ => NotFound(Json.parse(
        """
          |{
          |  "answer"->"no encontrado"
          |}
        """))
    }
  }

  def listAll() = Action.async {implicit request=>
    laboratoryService.listAll.map(result => {
      Ok(Json.toJson(result))
    })
  }
}
