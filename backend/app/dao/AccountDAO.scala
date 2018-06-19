package dao

import models.AccountModel
import play.api.db.slick.HasDatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait AccountComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  // This class convert the database's account table in a object-oriented entity: the Account model.
  class AccountTable(tag: Tag) extends Table[AccountModel](tag, "ACCOUNTS")
  {
    def id        = column[Long]("ID", O.PrimaryKey, O.AutoInc) // Primary key, auto-incremented
    def username = column[String]("USERNAME")
    def password = column[String]("PASSWORD")
    def email    = column[String]("EMAIL")
    def sex      = column[String]("SEX")
    def age      = column[Int]("AGE")
    def city     = column[String]("CITY")


    // Map the attributes with the model; the ID is optional.
    def * = (id.?, username, password, email, sex, age, city) <> (AccountModel.tupled, AccountModel.unapply)
  }

}


@Singleton
class AccountDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends AccountComponent with HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  // Get the object-oriented list of courses directly from the query table.
  val accounts = TableQuery[AccountTable]

  /** Retrieve the list of accounts sorted by username */
  def list(): Future[Seq[AccountModel]] = {
    val query = accounts.sortBy(s => s.username)
    db.run(query.result)
  }

  /** Retrieve an account from the username. */
  def findByUsername(username: String): Future[Option[AccountModel]] =
    db.run(accounts.filter(_.username === username).result.headOption)

  /** Retrieve an account from the username. */
  def findByEmail(email: String): Future[Option[AccountModel]] =
    db.run(accounts.filter(_.email === email).result.headOption)

  /** Retrieve an account from the id. */
  def findById(id: Long): Future[Option[AccountModel]] =
    db.run(accounts.filter(_.id === id).result.headOption)

  /** Insert a new account, then return it. */
  def insert(account: AccountModel): Future[AccountModel] = {
    val insertQuery = accounts returning accounts.map(_.id) into ((account, id) => account.copy(Some(id)))
    db.run(insertQuery += account)
  }

  /** Update a account, then return an integer that indicates if the account was found (1) or not (0). */
  def update(id: Long, account: AccountModel): Future[Int] = {
    val accountToUpdate: AccountModel = account.copy(Some(id))
    db.run(accounts.filter(_.id === id).update(accountToUpdate))
  }

  /** Delete a account, then return an integer that indicates if the account was found (1) or not (0) */
  /** Delete a account, then return an integer that indicates if the account was found (1) or not (0) */
  def delete(id: Long): Future[Int] =
    db.run(accounts.filter(_.id === id).delete)
}
