package com.simpletwodo.twitterutil

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
import com.simpletwodo.mongodbutil._

object SimpleTwoDoTwitter {
  def getAuthorizedInstance(userData: SimpleTwoDoUserData) = {
    val factory = new TwitterFactory()
    factory.getInstance(loadAccessToken(userData))
  }

  private def loadAccessToken(userData: SimpleTwoDoUserData) = {
    new auth.AccessToken(userData.accsToken, userData.tokenSecret)
  }

  def getToDoTweets(userData: SimpleTwoDoUserData): Buffer[Tweet] = {
    val result = getAuthorizedInstance(userData).search(queryGenerate(userData.screenName, "2012-02-12"))
    result.getTweets.asScala
  }

  private def queryGenerate(screenId: String, lastAccessDate: String): Query = {
    val ret = new Query("from:" + screenId + " to:" + screenId + " #SimpleTwoDo")
    ret.setSince(lastAccessDate)
    ret.setRpp(100)

    ret
  }

  def getRequestToken: auth.RequestToken = {
    val factory = new TwitterFactory()
    factory.getInstance().getOAuthRequestToken("http://localhost:8080/getaccesstoken")
  }

  def getAccessToken(reqToken: auth.RequestToken, verifier: String): auth.AccessToken = {
    val factory = new TwitterFactory()
    factory.getInstance().getOAuthAccessToken(reqToken, verifier)
  }
}