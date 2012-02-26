package com.maosandbox.twitterutil

/**
 * Created by IntelliJ IDEA.
 * User: mao
 * Date: 12/02/10
 * Time: 21:45
 * To change this template use File | Settings | File Templates.
 */

import twitter4j._
import scala.collection.JavaConverters._
import scala.collection.mutable._

object TwitterUtil {
  private val twitter = new TwitterFactory().getInstance()

  def getUserTimeLine(user: String): Buffer[Status] = {
    asScalaBufferConverter(twitter.getUserTimeline(user)).asScala
  }

  def getUserLastTweet(user: String): String = {
    getUserTimeLine(user).head.getText
  }

  def getQueryTweets(query: Query): Buffer[Tweet] = {
    val result = twitter.search(query)
    result.getTweets.asScala
  }

  def getRequestToken: auth.RequestToken = {
    twitter.getOAuthRequestToken("http://localhost:8080/getaccesstoken")
  }

  def getAccessToken(reqToken: auth.RequestToken, verifier: String): auth.AccessToken = {
    twitter.getOAuthAccessToken(reqToken, verifier)
  }
}