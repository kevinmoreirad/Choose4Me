package controllers

import dao.{AccountDAO, SurveyDAO, SurveyPropositionDAO}
import javax.inject.{Inject, Singleton}
import models.SurveyPropositionModel
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.JWTUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SurveyPropositionController @Inject()(cc: ControllerComponents, surveyPropositionDAO: SurveyPropositionDAO, surveyDAO: SurveyDAO, accountDAO: AccountDAO) extends AbstractController(cc) {

  // Refer to the StudentsController class in order to have more explanations.
  implicit val surveyPropositionToJson: Writes[SurveyPropositionModel] = (
    (JsPath \ "id").write[Option[Long]] and
      (JsPath \ "response").write[Option[String]] and
      (JsPath \ "choiceNumb").write[Long] and
      (JsPath \ "surveyId").write[Long] and
      (JsPath \ "image").write[Option[Array[Byte]]]
    )(unlift(SurveyPropositionModel.unapply))

  implicit val jsonToSurveyProposition: Reads[SurveyPropositionModel] = (
    (JsPath \ "id").readNullable[Long] and
      (JsPath \ "response").readNullable[String] and
      (JsPath \ "choiceNumb").read[Long] and
      (JsPath \ "surveyId").read[Long] and
      (JsPath \ "image").readNullable[Array[Byte]]
    )(SurveyPropositionModel.apply _)

  def validateJson[A : Reads] = parse.json.validate(
    _.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  def getSurveyPropositions(id: String) = Action.async {
    println(id)
    val surveyPropositionsList = surveyPropositionDAO.findBySurveyId(id.toLong)
    surveyPropositionsList map (c => Ok(Json.toJson(c)))

  }

  def postSurveyProposition(id: String) = Action.async(validateJson[SurveyPropositionModel]) { request =>
    val surveyProposition = request.body
    surveyDAO.findById(surveyProposition.surveyId).flatMap {
      case Some(survey) =>
        accountDAO.findById(survey.accountId).flatMap {
          case Some(account) =>
            println(account)
            val token = request.headers.get("token").fold("")(_.toString)
            val payload = JWTUtils.decodePayload("{ \""+token+"\" }").get
            if(payload.equals(account.username)){
              surveyPropositionDAO.insert(surveyProposition).map(c =>
                Ok(
                  Json.obj(
                    "status" -> "OK",
                    "id" -> c.id,
                    "message" -> ("SurveyProposition '" + c.id + "' saved.")
                  )
                )
              )
            }
            else {
              Future {
                NotFound(Json.obj(
                  "status" -> "Not Allowed",
                  "message" -> ("your token is not good or you don't have token in headers")
                ))}
            }
          case None =>
            Future {
              NotFound(Json.obj(
                "status" -> "Not Found",
                "message" -> ("This account username was not found")
              ))
            }
        }
      case None =>
        Future {
          NotFound(Json.obj(
            "status" -> "Not Found",
            "message" -> ("This survey was not found")
          ))
        }
    }
  }
}