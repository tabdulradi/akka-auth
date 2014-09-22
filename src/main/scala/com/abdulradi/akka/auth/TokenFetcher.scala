package com.abdulradi.akka.auth

import akka.actor.ActorRef
import play.api.libs.ws.Response
import com.abdulradi.akka.auth.util.QueryStringParser
import TokenFetcher._

class TokenFetcher(providers: Map[String, OAuth2Provider], profileFetcher: ActorRef) extends ServiceFetcher[State, Data] {

  when(WaitClientRequest) {
    case Event(Login(code, redirectUri, pId), _) if providers.isDefinedAt(pId) =>
      goto(WaitProviderResponse) using ClientRequest(code, redirectUri, providers(pId), sender())
    case Event(Login(_, _, provider), _) =>
      ??? // Unkown provider
      stay
  }

  when(WaitProviderResponse) {
    case Event(r: Response, cr@ClientRequest(_, _, provider, requester)) =>
      QueryStringParser.parse(r.body) match {
        case QueryStringParser.Success(data, _) if data.isDefinedAt("access_token") =>
          goto(WaitClientRequest) using TokenSuccess(
            data("access_token"),
            provider,
            requester
          )
        case e =>
          goto(WaitClientRequest) using TokenFailure(e, cr)
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
        case ClientRequest(code, redirectUri, provider, requester) =>
          request(provider.accessTokenUrl)(
            "redirect_uri" -> redirectUri,
            "code" -> code,
            "client_id" -> provider.clientId,
            "client_secret" -> provider.clientSecret
          )
        case _ =>
          ??? // Log warn
      }
    case WaitProviderResponse -> WaitClientRequest =>
      unstashAll()
      stateData match {
        case TokenSuccess(accessToken, provider, requester) =>
          profileFetcher ! ProfileFetcher.Login(accessToken, provider, requester)
        case TokenFailure(e, request) =>
          request.requester ! e
        case _ =>
          ???
      }
  }
}

object TokenFetcher {
  // Events
  case class Login(code: String, provider: String, redirectUri: String)

  // states
  sealed trait State
  case object WaitClientRequest extends State
  case object WaitProviderResponse extends State

  sealed trait Data
  case class ClientRequest(code: String, redirectUri: String, provider: OAuth2Provider, requester: ActorRef) extends Data
  case class TokenSuccess(accessToken: String, provider: OAuth2Provider, requester: ActorRef) extends Data
  case class TokenFailure(e: Any, r: ClientRequest) extends Data
}
