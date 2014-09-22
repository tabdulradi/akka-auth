package com.abdulradi.akka.auth

import scala.concurrent.Future
import akka.actor.{Actor, ActorLogging, FSM, ActorRef, Stash}
import akka.pattern.pipe
import play.api.libs.ws.Response

import com.abdulradi.akka.auth.util.QueryStringParser
import TokenFetcher._

class ServiceFetcher[S, D] extends Actor with ActorLogging with FSM[S, D] with Stash {
  implicit val ec = context.system.dispatcher

  private val client = {
    val builder = new com.ning.http.client.AsyncHttpClientConfig.Builder()
    new play.api.libs.ws.ning.NingWSClient(builder.build())
  }

  def request(url: String)(qs: (String, String)*) =
    pipe(client.url(url).withQueryString(qs: _*).get) to self

}
