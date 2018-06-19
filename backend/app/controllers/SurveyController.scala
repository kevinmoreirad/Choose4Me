package controllers

import dao.{AccountDAO, SurveyDAO}
import javax.inject.{Inject, Singleton}
import models.SurveyModel
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.JWTUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SurveyController @Inject()(cc: ControllerComponents, surveyDAO: SurveyDAO, accountDAO: AccountDAO) extends AbstractController(cc) {

  implicit val surveyToJson: Writes[SurveyModel] = (
    (JsPath \ "id").write[Option[Long]] and
      (JsPath \ "question").write[String] and
      (JsPath \ "accountId").write[Long]
    )(unlift(SurveyModel.unapply))

  implicit val jsonToSurvey: Reads[SurveyModel] = (
    (JsPath \ "id").readNullable[Long] and
      (JsPath \ "question").read[String] and
        (JsPath \ "accountId").read[Long]
    )(SurveyModel.apply _)

  def validateJson[A : Reads] = parse.json.validate(
    _.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  def getSurveys(username: String) = Action.async { request =>
    accountDAO.findByUsername(username).flatMap {
      case Some(c)  =>
        val token = request.headers.get("token").fold("")(_.toString)
        val payload = JWTUtils.decodePayload(token).get
        if(payload.equals(username)){
          surveyDAO.findByAccountId(c.id.fold(-1.toLong)(numb => numb)) map( survey =>
            Ok(Json.toJson(survey))
          )
        }
        else {
          Future {
            NotFound(Json.obj(
              "status" -> "Not Allowed",
              "message" -> ("your token is not good or you don't have token in headers")
            ))
          }
        }

      case None =>
        Future {
          NotFound(Json.obj(
            "status" -> "Not Found",
            "message" -> ("Account with username: " + username + " not found.")
          ))
        }
    }
  }

  def postSurvey(username: String) = Action.async(validateJson[SurveyModel]) { request =>
    val token = request.headers.get("token").fold("")(_.toString)
    val payload = JWTUtils.decodePayload(token).get
    if(payload.equals(username)) {
      val survey = request.body
      val createdSurvey = surveyDAO.insert(survey)

      createdSurvey.map(c =>
        Ok(
          Json.obj(
            "status" -> "OK",
            "id" -> c.id,
            "message" -> ("Survey '" + c.id + "' saved.")
          )
        )
      )
    }
    else {
      Future {
        NotFound(Json.obj(
          "status" -> "Not allowed",
          "message" -> ("your token is not good or you don't have token in headers")
        ))
      }
    }
  }

  def getSurveyById(id: String) = Action.async {
    val optionalSurvey = surveyDAO.findById(id.toLong)

    optionalSurvey.map {
      case Some(c) => Ok(Json.toJson(c))
      case None =>
        NotFound(Json.obj(
          "status" -> "Not Found",
          "message" -> ("Survey #" + id + " not found.")
        ))
    }
  }

  def deleteSurvey(username: String, id: String) = Action.async { request =>
    val token = request.headers.get("token").fold("")(_.toString)
    val payload = JWTUtils.decodePayload(token).get
    if(payload.equals(username)){
      surveyDAO.delete(id.toLong).map {
        case 1 => Ok(
          Json.obj(
            "status"  -> "OK",
            "message" -> ("Survey #" + id + " deleted.")
          )
        )
        case 0 => NotFound(Json.obj(
          "status" -> "Not Found",
          "message" -> ("Survey #" + id + " not found.")
        ))
      }
    }
    else {
      Future {
        NotFound(Json.obj(
          "status" -> "Not allowed",
          "message" -> ("your token is not good or you don't have token in headers")
        ))
      }
    }
  }

}