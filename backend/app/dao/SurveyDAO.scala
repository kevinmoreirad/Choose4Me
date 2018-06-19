package dao

import models.{SurveyModel}
import scala.concurrent.{ExecutionContext, Future}
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait SurveyComponent extends AccountComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  // This class convert the database's survey table in a object-oriented entity: the survey model.
  class SurveyTable(tag: Tag) extends Table[SurveyModel](tag, "SURVEYS")
  {
    def id        = column[Long]("ID", O.PrimaryKey, O.AutoInc) // Primary key, auto-incremented
    def question = column[String]("QUESTION")
    def accountId = column[Long]("ACCOUNT_ID")

    // Map the attributes with the model; the ID is optional.
    def * = (id.?, question, accountId) <> (SurveyModel.tupled, SurveyModel.unapply)
  }

}


@Singleton
class SurveyDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends SurveyComponent with HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  // Get the object-oriented list of courses directly from the query table.
  val surveys = TableQuery[SurveyTable]

  /** Retrieve the list of surveys sorted by id */
  def list(): Future[Seq[SurveyModel]] = {
    val query = surveys.sortBy(s => s.id)
    db.run(query.result)
  }

  /** Retrieve an survey from the id. */
  def findById(id: Long): Future[Option[SurveyModel]] =
    db.run(surveys.filter(_.id === id).result.headOption)

  /** Retrieve an survey from the creator id. */
  def findByAccountId(id: Long): Future[Seq[SurveyModel]] =
    db.run(surveys.filter(_.accountId === id).result)

  /** Insert a new survey, then return it. */
  def insert(survey: SurveyModel): Future[SurveyModel] = {
    val insertQuery = surveys returning surveys.map(_.id) into ((survey, id) => survey.copy(Some(id)))
    db.run(insertQuery += survey)
  }

  /** Update a survey, then return an integer that indicates if the survey was found (1) or not (0). */
  def update(id: Long, survey: SurveyModel): Future[Int] = {
    val surveyToUpdate: SurveyModel = survey.copy(Some(id))
    db.run(surveys.filter(_.id === id).update(surveyToUpdate))
  }

  /** Delete a survey, then return an integer that indicates if the survey was found (1) or not (0) */
  def delete(id: Long): Future[Int] =
    db.run(surveys.filter(_.id === id).delete)
}
