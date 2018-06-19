package models


case class SurveyPropositionModel(id: Option[Long], response: Option[String],choiceNumb: Long, surveyId: Long, image: Option[Array[Byte]])
{

}
