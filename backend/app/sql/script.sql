SET FOREIGN_KEY_CHECKS=0;
DROP TABLE IF EXISTS `accounts`;
CREATE TABLE `accounts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(45)  unique NOT NULL,
  `password` varchar(45) NOT NULL,
  `email` varchar(45) unique NOT NULL,
  `sex` varchar(45) NOT NULL,
  `age` int(45) NOT NULL,
  `city` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `answers`;
CREATE TABLE `answers` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `choice` int(11) NOT NULL,
  `account_id` int(11) NOT NULL,
  `survey_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY(account_id) REFERENCES accounts(id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY(survey_id) REFERENCES surveys(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



DROP TABLE IF EXISTS `surveys`;
CREATE TABLE `surveys` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `question` varchar(150) NOT NULL,
  `account_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY(account_id) REFERENCES accounts(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



DROP TABLE IF EXISTS `survey_propositions`;
CREATE TABLE `survey_propositions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `choice_numb` int(11),
  `response` varchar(150),
  `survey_id` int(11) NOT NULL,
  `image` mediumblob,
  PRIMARY KEY (`id`),
  FOREIGN KEY(survey_id) REFERENCES surveys(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



DROP TABLE IF EXISTS `votes`;
CREATE TABLE `votes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sex` varchar(150) NOT NULL,
  `age` int(11) NOT NULL,
  `city` varchar(45) NOT NULL,
  `survey_proposition_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY(survey_proposition_id) REFERENCES survey_propositions(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


SET FOREIGN_KEY_CHECKS=1;