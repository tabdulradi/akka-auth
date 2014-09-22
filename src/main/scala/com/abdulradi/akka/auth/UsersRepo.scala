package com.abdulradi.akka.auth

import scala.concurrent.Future
import akka.actor.{ActorRef, Actor, ActorLogging}
import akka.pattern.pipe
import play.api.libs.json.{Json, JsObject, JsSuccess, JsError}
import com.abdulradi.akka.auth.models.{ProviderProfile, Credentials, User}

trait UsersRepo extends Actor with ActorLogging {
  import UsersRepo._
  implicit val ec = context.system.dispatcher

  def receive = {
    case GetSimilar(profile, replyTo) =>
      val f = getSimilar(profile).map(GetSimilarResult(_, profile))
      f pipeTo replyTo
      f onFailure {
        case e =>
          log.error("UsersRepo unable to GetSimilar because: {}, profile was: {} ", e, profile)
      }
  }

  def getSimilar(profile: ProviderProfile): Future[Option[User]]

  def get(credentials: Credentials): Future[Option[User]]

}

object UsersRepo {
  case class GetSimilar(profile: ProviderProfile, replyTo: ActorRef)
  case class GetSimilarResult(user: Option[User], profile: ProviderProfile)
}
