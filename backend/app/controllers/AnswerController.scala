package controllers

import dao._
import javax.inject.{Inject, Singleton}
import models.{AccountModel, AnswerModel, SurveyModel, VoteModel}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.JWTUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

@Singleton
class AnswerController @Inject()(cc: ControllerComponents, answerDAO: AnswerDAO, accountDAO: AccountDAO, surveyPropositionDAO: SurveyPropositionDAO, voteDAO: VoteDAO) extends AbstractController(cc) {

  // Refer to the StudentsController class in order to have more explanations.
  implicit val answerToJson: Writes[AnswerModel] = (
    (JsPath \ "id").write[Option[Long]] and
      (JsPath \ "choice").write[Long] and
      (JsPath \ "accountId").write[Long] and
      (JsPath \ "surveyId").write[Long]
    )(unlift(AnswerModel.unapply))

  implicit val jsonToAnswer: Reads[AnswerModel] = (
    (JsPath \ "id").readNullable[Long] and
      (JsPath \ "choice").read[Long] and
      (JsPath \ "accountId").read[Long] and
      (JsPath \ "surveyId").read[Long]
    )(AnswerModel.apply _)

  def validateJson[A : Reads] = parse.json.validate(
    _.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  def postAnswer(username: String) = Action.async(validateJson[AnswerModel]) { request =>
    val token = request.headers.get("token").fold("")(_.toString)
    val payload = JWTUtils.decodePayload(token).get
    accountDAO.findByUsername(username).flatMap {
      case Some(account) =>
        println(request.body.surveyId+" "+request.body.choice)
        surveyPropositionDAO.findBySurveyIdAndChoiceNumber(request.body.surveyId, request.body.choice).flatMap {
          case Some(surveyProposition) =>
            if(surveyProposition.id.isDefined) {
              val id = surveyProposition.id.fold(0.toLong)(x => x)
                if(payload.equals(username)) {
                  println("adding vote")
                  voteDAO.insert(VoteModel(None,account.sex, account.age,account.city, id))
                  val answer = request.body
                  val createdAnswer = answerDAO.insert(answer)
                  createdAnswer.map(c =>
                    Ok(
                      Json.obj(
                        "status" -> "OK",
                        "id" -> c.id,
                        "message" -> ("Answer '" + c.id + "' saved.")
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
            else {
              Future {
                NotFound(Json.obj(
                  "status" -> "No SurveyProposition",
                  "message" -> ("wrong surveyproposition")
                ))
              }
            }
          case None =>
            Future {
              NotFound(Json.obj(
                "status" -> "No SurveyProposition",
                "message" -> ("wrong surveyproposition")
              ))
            }
        }
      case None =>
        Future {
          NotFound(Json.obj(
            "status" -> "No Username",
            "message" -> ("wrong username")
          ))
        }

  }
  }
}