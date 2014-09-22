package com.abdulradi.akka.auth

import scala.util.{Success, Failure}
import akka.actor.ActorRef
import play.api.libs.ws.Response

import com.abdulradi.akka.auth.models.ProviderProfile
import ProfileFetcher._

class ProfileFetcher(usersRepo: ActorRef) extends ServiceFetcher[State, Data] {

  when(WaitClientRequest) {
    case Event(Login(token, provider, requester), _) =>
      goto(WaitProviderResponse) using ClientRequest(token, provider, requester)
  }

  when(WaitProviderResponse) {
    case Event(r: Response, cr@ClientRequest(_, provider, requester)) =>
      provider.parse(r.body) match {
        case Success(profile) =>
          goto(WaitClientRequest) using ProfileSuccess(
            profile,
            requester
          )
        case Failure(e) =>
          goto(WaitClientRequest) using ProfileFailure(e, cr)
      }
    case Event(_: Login, _) =>
      stash()
      stay
  }

  whenUnhandled {
    case Event(e, s) =>
      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      stay
  }

  onTransition {
    case WaitClientRequest -> WaitProviderResponse =>
      stateData match {
        case ClientRequest(accessToken, provider, _) =>
          request(provider.profileUrl)("access_token" -> accessToken)
        case _ =>
          ??? // Log warn
      }
    case WaitProviderResponse -> WaitClientRequest =>
      unstashAll()
      stateData match {
        case ProfileSuccess(accessToken, requester) =>
          ??? // Forward to next Actor
        case ProfileFailure(e, r) =>
          r.requester ! e
        case _ =>
          ???
      }
  }
}

object ProfileFetcher {
  // Events
  case class Login(token: String, provider: OAuth2Provider, requester: ActorRef)

  // states
  sealed trait State
  case object WaitClientRequest extends State
  case object WaitProviderResponse extends State

  sealed trait Data
  case class ClientRequest(token: String, provider: OAuth2Provider, requester: ActorRef) extends Data
  case class ProfileSuccess(profile: ProviderProfile, requester: ActorRef) extends Data
  case class ProfileFailure(e: Any, r: ClientRequest) extends Data
}
