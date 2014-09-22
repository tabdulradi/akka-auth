package com.abdulradi.akka.auth

import scala.util.Try
import com.abdulradi.akka.auth.models.ProviderProfile

trait OAuth2Provider {
  def clientId: String
  def clientSecret: String
  def accessTokenUrl: String
  def profileUrl: String
  def parse(profile: String): Try[ProviderProfile]
}
