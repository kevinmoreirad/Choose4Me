package models

case class AccountModel(id: Option[Long], username: String, password: String, email: String,
                        sex: String, age: Int, city: String)
{

}
