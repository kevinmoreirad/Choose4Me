package controllers

import java.util.concurrent.Future

import dao.VoteDAO
import javax.inject.{Inject, Singleton}
import models.VoteModel
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.JWTUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class VoteController @Inject()(cc: ControllerComponents, voteDAO: VoteDAO) extends AbstractController(cc) {

  // Refer to the StudentsController class in order to have more explanations.
  implicit val voteToJson: Writes[VoteModel] = (
    (JsPath \ "id").write[Option[Long]] and
      (JsPath \ "sex").write[String] and
      (JsPath \ "age").write[Int] and
      (JsPath \ "city").write[String] and
      (JsPath \ "surveyPropositionId").write[Long]
    ) (unlift(VoteModel.unapply))

  implicit val jsonToVote: Reads[VoteModel] = (
    (JsPath \ "id").readNullable[Long] and
      (JsPath \ "sex").read[String] and
      (JsPath \ "age").read[Int] and
      (JsPath \ "city").read[String] and
      (JsPath \ "surveyPropositionId").read[Long]
    ) (VoteModel.apply _)

  def validateJson[A: Reads] = parse.json.validate(
    _.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

}