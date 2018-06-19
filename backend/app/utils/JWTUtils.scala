package utils

import authentikat.jwt.JsonWebToken.parse
import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtClaimsSetJValue, JwtHeader}
import org.apache.commons.codec.binary.Base64.decodeBase64

import scala.util.control.Exception.allCatch

class JWTUtils {
  val JwtSecretKey = "qwswett5559598589-djebjfbe38383877dd"
  val JwtSecretAlgo = "HS256"
  def createToken(payload: String): String = {
    val header = JwtHeader(JwtSecretAlgo)
    val claimsSet = JwtClaimsSet(Map("username" -> payload))
    JsonWebToken(header, claimsSet, JwtSecretKey)
  }
  def isValidToken(jwtToken: String): Boolean =
    JsonWebToken.validate(jwtToken, JwtSecretKey)
  def decodePayload(jwtToken: String): Option[String] = {
    jwtToken match {
      case JsonWebToken(header, claimsSet, signature) => Option(claimsSet.asSimpleMap.get("username"))
      case _                                          => None
    }
  }
}


object JWTUtils extends JWTUtils