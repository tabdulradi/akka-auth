package com.abdulradi.akka.auth.models

import play.api.libs.json.JsObject

trait User {
  /*
   * Unique identifier for the user
   * May be used by rest of the application to refer to the user
   */
  def id: String

  /*
   * Classic username/password authentication (Optional)
   */
  def credentials: Option[Credentials]

  /*
   * Map between providerId to profile data
   */
  def providerProfiles: Map[String, ProviderProfile]
}


case class Credentials(email: String, password: String)

case class ProviderProfile(
  emails: Set[String],
  profile: JsObject
)
