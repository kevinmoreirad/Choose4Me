package controllers

import dao._
import javax.inject.{Inject, Singleton}
import models.SurveyModel
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.JWTUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

@Singleton
class ApiController @Inject()(cc: ControllerComponents, surveyDAO: SurveyDAO, accountDAO: AccountDAO, surveyController: SurveyController,
                              answerDAO: AnswerDAO, surveyPropositionController: SurveyPropositionController, voteDAO: VoteDAO, surveyPropositionDAO: SurveyPropositionDAO) extends AbstractController(cc) {

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


  def getStats(id: String) = Action.async {
    surveyPropositionDAO.findBySurveyId(id.toLong).flatMap { listSurveyProposition =>
      val idSurveyProposition1 = listSurveyProposition.head.id.get
      val idSurveyProposition2 = listSurveyProposition.last.id.get
println("surv id "+idSurveyProposition1+" "+idSurveyProposition2)
      voteDAO.findBySurveyPropositionIds(idSurveyProposition1, idSurveyProposition2).map { listVotes =>
        println(listVotes)
        var cityMost = "No votes For now!"
        val cityMostList = listVotes.groupBy(_.city).mapValues(_.size).toSeq.sortBy(_._2)
        if(cityMostList.size != 0)
          cityMost = cityMostList.head._1

        val nbVotes = listVotes.size
        val nbChoice1 = listVotes.filter(_.surveyPropositionId == idSurveyProposition1).size

        val nbWomen = listVotes.filter(_.sex == "female")
        val nbWomenChoice1 = nbWomen.filter(_.surveyPropositionId == idSurveyProposition1).size

        val nbMen = listVotes.filter(_.sex == "male")
        val nbMenChoice1 = nbWomen.filter(_.surveyPropositionId == idSurveyProposition1).size

        val nb1To18 = listVotes.filter(_.age <= 18)
        val nb1To18Choice1 = nb1To18.filter(_.surveyPropositionId == idSurveyProposition1).size

        val nb19To35 = listVotes.filter(_.age <= 35).filter(_.age > 18)
        val nb19To35Choice1 = nb19To35.filter(_.surveyPropositionId == idSurveyProposition1).size

        val nb36To55 = listVotes.filter(_.age <= 55).filter(_.age > 35)
        val nb36To55Choice1 = nb36To55.filter(_.surveyPropositionId == idSurveyProposition1).size

        val nb56To80 = listVotes.filter(_.age <= 80).filter(_.age > 55)
        val nb56To80Choice1 = nb56To80.filter(_.surveyPropositionId == idSurveyProposition1).size

        val nb81More = listVotes.filter(_.age > 80)
        val nb81MoreChoice1 = nb81More.filter(_.surveyPropositionId == idSurveyProposition1).size

        var percMen = 0.5
        if(nbMen.size != 0)
          percMen = nbMenChoice1 / nbMen.size
        var percWomen = 0.5
        if(nbWomen.size != 0)
          percWomen = (nbWomenChoice1 / nbWomen.size)
        var perc1To18 = 0.5
        if(nb1To18.size != 0)
          perc1To18 = (nb1To18Choice1 / nb1To18.size)
        var perc19To35 = 0.5
        if(nb19To35.size != 0)
          perc19To35 = (nb19To35Choice1 / nb19To35.size)
        var perc36To55 = 0.5
        if(nb36To55.size != 0)
          perc36To55 = (nb36To55Choice1 / nb36To55.size)
        var perc56To80 = 0.5
        if(nb56To80.size != 0)
          perc56To80 = (nb56To80Choice1 / nb56To80.size)
        var perc81More = 0.5
        if(nb81More.size != 0)
          perc81More = (nb81MoreChoice1 / nb81More.size)
        var percTotal = 0.5
        if(nbVotes != 0)
          percTotal = (nbChoice1 / nbVotes)
        val jsonStats = "{ \"nbVotes\":" + nbVotes + "," +
          "\"cityMost\":\"" + cityMost + "\"," +
          "\"percentageTotalCh1\":" + percTotal + "," +
          "\"percentageMenCh1\":" + percMen + "," +
          "\"percentageWomenCh1\":" + percWomen + "," +
          "\"percentage1To18Ch1\":" + perc1To18 + "," +
          "\"percentage19To35Ch1\":" + perc19To35 + "," +
          "\"percentage36To55Ch1\":" + perc36To55 + "," +
          "\"percentage56To80Ch1\":" + perc56To80 + "," +
          "\"percentage80MoreCh1\":" + perc81More +
          "}"

        Ok(jsonStats)
      }
    }

  }

  def getToken(username: Option[String], password: Option[String]) = Action.async {
    accountDAO.findByUsername(username.fold("")(_.toString)).map {
      case Some(c) =>
        if (c.password.equals(password.fold("")(_.toString))) {
          Ok(Json.toJson(JWTUtils.createToken(c.username)))
        }
        else {
          NotFound(Json.obj(
            "status" -> "Not Allowed",
            "message" -> ("your your username doesn't match with your password, or you forget one of those")
          ))
        }

      case None =>
        NotFound(Json.obj(
          "status" -> "Not Found",
          "message" -> ("You don't have query parameters username and password.")
        ))
    }
  }

  def getNewQuestion(username: String) = Action.async { request =>
    accountDAO.findByUsername(username).flatMap {
      case Some(c) =>
        val token = request.headers.get("token").fold("")(_.toString)
        val payload = JWTUtils.decodePayload(token).get
        if (payload.equals(username)) {
          surveyDAO.list().flatMap { listSurveys =>
            answerDAO.list().map { listAnswer =>
              val listSurveysDone = listAnswer.filter(_.accountId == c.id.fold(-1.toLong)(numb => numb)).map(answer => answer.surveyId)
              var listSur = listSurveys.filter(_.accountId != c.id.fold(-1.toLong)(numb => numb))
              listSurveysDone.foreach(surveyIdDone => listSur = listSur.filter(_.id.fold(-1.toLong)(numb => numb) != surveyIdDone))
              if(listSur.size > 0) {
                val surveyToSend = listSur(Random.nextInt(listSur.size))
                Ok(Json.toJson(surveyToSend))
              }
              else {
                  NotFound(Json.obj(
                    "status" -> "Not Found",
                    "message" -> ("Account with username: " + username + " not found.")
                  ))
              }
            }
          }
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
}
