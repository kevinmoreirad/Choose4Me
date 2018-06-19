package dao

import models.{AccountModel, AnswerModel}
import play.api.db.slick.HasDatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait AnswerComponent extends AccountComponent with SurveyComponent{
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  // This class convert the database's account table in a object-oriented entity: the Answer model.
  class AnswerTable(tag: Tag) extends Table[AnswerModel](tag, "ANSWERS")
  {
    def id    = column[Long]("ID", O.PrimaryKey, O.AutoInc) // Primary key, auto-incremented
    def choice = column[Long]("CHOICE")
    def accountId = column[Long]("ACCOUNT_ID")
    def surveyId = column[Long]("SURVEY_ID")

    // Map the attributes with the model; the ID is optional.
    def * = (id.?, choice, accountId, surveyId) <> (AnswerModel.tupled, AnswerModel.unapply)
  }

}


@Singleton
class AnswerDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends AnswerComponent with HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  // Get the object-oriented list of courses directly from the query table.
  val answers = TableQuery[AnswerTable]

  /** Retrieve the list of answers sorted by id */
  def list(): Future[Seq[AnswerModel]] = {
    val query = answers.sortBy(s => s.id)
    db.run(query.result)
  }

  /** Retrieve list of answers of specified user id*/
  def findByAccountId(id: Long): Future[Option[AnswerModel]] =
    db.run(answers.filter(_.accountId === id).result.headOption)

  /** Retrieve list of answers of specified survey id*/
  def findBySurveyId(id: Long): Future[Option[AnswerModel]] =
    db.run(answers.filter(_.surveyId === id).result.headOption)


  /** Retrieve an answer by id. */
  def findById(id: Long): Future[Option[AnswerModel]] =
    db.run(answers.filter(_.id === id).result.headOption)

  /** Insert a new answer, then return it. */
  def insert(answer: AnswerModel): Future[AnswerModel] = {
    val insertQuery = answers returning answers.map(_.id) into ((account, id) => account.copy(Some(id)))
    db.run(insertQuery += answer)
  }

  /** Update a answer, then return an integer that indicates if the answer was found (1) or not (0). */
  def update(id: Long, answer: AnswerModel): Future[Int] = {
    val answerToUpdate: AnswerModel = answer.copy(Some(id))
    db.run(answers.filter(_.id === id).update(answerToUpdate))
  }

  /** Delete a answer, then return an integer that indicates if the answer was found (1) or not (0) */
  def delete(id: Long): Future[Int] =
    db.run(answers.filter(_.id === id).delete)
}
