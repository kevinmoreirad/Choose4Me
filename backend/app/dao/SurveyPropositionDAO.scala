package dao


import java.util.Base64

import com.mysql.jdbc.Blob
import models.SurveyPropositionModel

import scala.concurrent.{ExecutionContext, Future}
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile


trait SurveyPropositionComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  // This class convert the database's SurveyProposition table in a object-oriented entity: the SurveyProposition model.
  class SurveyPropositionTable(tag: Tag) extends Table[SurveyPropositionModel](tag, "SURVEY_PROPOSITIONS")
  {
    def id        = column[Long]("ID", O.PrimaryKey, O.AutoInc) // Primary key, auto-incremented
    def choiceNumb= column[Long]("CHOICE_NUMB")
    def response = column[String]("RESPONSE")
    def surveyId  = column[Long]("SURVEY_ID")
    def image = column[Array[Byte]]("IMAGE")


    // Map the attributes with the model; the ID is optional.
    def * = (id.?, response.?,choiceNumb, surveyId, image.?) <> (SurveyPropositionModel.tupled, SurveyPropositionModel.unapply)
  }

}


@Singleton
class SurveyPropositionDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends SurveyPropositionComponent with HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  // Get the object-oriented list of courses directly from the query table.
  val surveyPropositions = TableQuery[SurveyPropositionTable]

  /** Retrieve the list of SurveyPropositions sorted by id */
  def list(): Future[Seq[SurveyPropositionModel]] = {
    val query = surveyPropositions.sortBy(s => s.id)
    db.run(query.result)
  }

  /** Retrieve an SurveyProposition from the id. */
  def findById(id: Long): Future[Option[SurveyPropositionModel]] =
    db.run(surveyPropositions.filter(_.id === id).result.headOption)

  /** Retrieve an SurveyProposition from the survey id and choice number */
  def findBySurveyIdAndChoiceNumber(id: Long, choiceNumb: Long): Future[Option[SurveyPropositionModel]] =
    db.run(surveyPropositions.filter(_.surveyId === id).filter(_.choiceNumb === choiceNumb).result.headOption)

  /** Retrieve an SurveyProposition from the survey id and choice number */
  def findBySurveyId(id: Long): Future[Seq[SurveyPropositionModel]] =
    db.run(surveyPropositions.filter(_.surveyId === id).result)

  /** Insert a new SurveyProposition, then return it. */
  def insert(surveyProposition: SurveyPropositionModel): Future[SurveyPropositionModel] = {
    val insertQuery = surveyPropositions returning surveyPropositions.map(_.id) into ((surveyProposition, id) => surveyProposition.copy(Some(id)))
    db.run(insertQuery += surveyProposition)
  }

  /** Update a SurveyProposition, then return an integer that indicates if the surveyproposition was found (1) or not (0). */
  def update(id: Long, surveyProposition: SurveyPropositionModel): Future[Int] = {
    val surveyPropositionToUpdate: SurveyPropositionModel = surveyProposition.copy(Some(id))
    db.run(surveyPropositions.filter(_.id === id).update(surveyPropositionToUpdate))
  }

  /** Delete a SurveyProposition, then return an integer that indicates if the course was found (1) or not (0) */
  def delete(id: Long): Future[Int] =
    db.run(surveyPropositions.filter(_.id === id).delete)
}
