package dao

import models.VoteModel

import scala.concurrent.{ExecutionContext, Future}
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait VoteComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  // This class convert the database's Vote table in a object-oriented entity: the Vote model.
  class VoteTable(tag: Tag) extends Table[VoteModel](tag, "VOTES")
  {
    def id        = column[Long]("ID", O.PrimaryKey, O.AutoInc) // Primary key, auto-incremented
    def sex      = column[String]("SEX")
    def age        = column[Int]("AGE")
    def city     = column[String]("CITY")
    def surveyPropositionId = column[Long]("SURVEY_PROPOSITION_ID")

    // Map the attributes with the model; the ID is optional.
    def * = (id.?, sex, age, city, surveyPropositionId) <> (VoteModel.tupled, VoteModel.unapply)
  }

}


@Singleton
class VoteDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends VoteComponent with HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  // Get the object-oriented list of courses directly from the query table.
  val votes = TableQuery[VoteTable]

  /** Retrieve the list of Votes sorted by id */
  def list(): Future[Seq[VoteModel]] = {
    val query = votes.sortBy(s => s.id)
    db.run(query.result)
  }

  /** Retrieve list of votes of a given surveyproposition id. */
  def findBySurveyPropositionId(id: Long): Future[Seq[VoteModel]] = {
    db.run(votes.filter(_.surveyPropositionId === id).result)
  }

  /** Retrieve list of votes of a given surveyproposition ids. */
  def findBySurveyPropositionIds(id1: Long, id2: Long): Future[Seq[VoteModel]] = {
    db.run(votes.filter(x => x.surveyPropositionId === id1 || x.surveyPropositionId === id2).result)
  }

  /** Retrieve an vote with id */
  def findById(id: Long): Future[Option[VoteModel]] =
    db.run(votes.filter(_.id === id).result.headOption)

  /** Insert a new Vote, then return it. */
  def insert(vote: VoteModel): Future[VoteModel] = {
    val insertQuery = votes returning votes.map(_.id) into ((vote, id) => vote.copy(Some(id)))
    db.run(insertQuery += vote)
  }

  /** Update a Vote, then return an integer that indicates if the vote was found (1) or not (0). */
  def update(id: Long, vote: VoteModel): Future[Int] = {
    val voteToUpdate: VoteModel = vote.copy(Some(id))
    db.run(votes.filter(_.id === id).update(voteToUpdate))
  }

  /** Delete a SurveyProposition, then return an integer that indicates if the vote was found (1) or not (0) */
  def delete(id: Long): Future[Int] =
    db.run(votes.filter(_.id === id).delete)

}
