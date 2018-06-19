package controllers

import dao.AccountDAO
import javax.inject.{Inject, Singleton}
import models.AccountModel
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.JWTUtils

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class AccountController @Inject()(cc: ControllerComponents, accountDAO: AccountDAO) extends AbstractController(cc) {

  implicit val accountToJson: Writes[AccountModel] = (
    (JsPath \ "id").write[Option[Long]] and
      (JsPath \ "username").write[String] and
      (JsPath \ "password").write[String] and
      (JsPath \ "email").write[String] and
      (JsPath \ "sex").write[String] and
      (JsPath \ "age").write[Int] and
      (JsPath \ "city").write[String]
    )(unlift(AccountModel.unapply))

  implicit val jsonToAccount: Reads[AccountModel] = (
    (JsPath \ "id").readNullable[Long] and
      (JsPath \ "username").read[String] and
      (JsPath \ "password").read[String] and
      (JsPath \ "email").read[String] and
      (JsPath \ "sex").read[String] and
      (JsPath \ "age").read[Int] and
      (JsPath \ "city").read[String]
    )(AccountModel.apply _)

  def validateJson[A : Reads] = parse.json.validate(
    _.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  def postAccount = Action.async(validateJson[AccountModel]) { request =>
    val account = request.body
    val createdAccount = accountDAO.insert(account)
    createdAccount.map(c =>
      Ok(
        Json.obj(
          "status" -> "OK",
          "id" -> c.id,
          "message" -> ("Account '" + c.username + "' saved.")
        )
      )
    )
  }

  def getAccount(username: String) = Action.async { request =>
    accountDAO.findByUsername(username).map {
      case Some(c)  =>
        val token = request.headers.get("token").fold("")(_.toString)
        val payload = JWTUtils.decodePayload(token).get
        if(payload.equals(username)){
          Ok(Json.toJson(c))
        }
        else {
          NotFound(Json.obj(
          "status" -> "Not Allowed",
          "message" -> ("your token is not good or you don't have token in headers")
        ))}

      case None =>
        NotFound(Json.obj(
          "status" -> "Not Found",
          "message" -> ("Account with username: " + username + " not found.")
        ))
    }
  }
}