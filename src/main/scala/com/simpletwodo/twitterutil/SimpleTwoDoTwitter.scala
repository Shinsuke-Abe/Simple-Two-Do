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
import com.simpletwodo.propertiesutil.SimpleTwoDoProperties
import com.simpletwodo.propertiesutil.ServerEnvSettings

object SimpleTwoDoTwitter {

  private val confbuilder = new conf.ConfigurationBuilder
  confbuilder.setOAuthConsumerKey(ServerEnvSettings.get("OAUTH_CONSUMERKEY"))
  confbuilder.setOAuthConsumerSecret(ServerEnvSettings.get("OAUTH_CONSUMERSECRET"))

  private val twitterconfig = confbuilder.build()

  def getAuthorizedInstance(userData: SimpleTwoDoUserData) = {
    val factory = new TwitterFactory(twitterconfig)
    factory.getInstance(loadAccessToken(userData))
  }

  private def loadAccessToken(userData: SimpleTwoDoUserData) = {
    new auth.AccessToken(userData.accsToken, userData.tokenSecret)
  }

  def getToDoTweets(userData: SimpleTwoDoUserData): Buffer[Tweet] = {
    val result = getAuthorizedInstance(userData).search(queryGenerate(userData.screenName))
    result.getTweets.asScala
  }

  private def queryGenerate(screenId: String): Query = {
    val ret = new Query(SimpleTwoDoProperties.get("twitter.query").format(screenId, screenId))
    ret.setRpp(100)

    ret
  }

  def getRequestToken: auth.RequestToken = {
    val factory = new TwitterFactory(twitterconfig)
    factory.getInstance().getOAuthRequestToken(ServerEnvSettings.get("TWITTER_AUTHCALLBACKURL"))
  }

  def getAccessToken(reqToken: auth.RequestToken, verifier: String): auth.AccessToken = {
    val factory = new TwitterFactory(twitterconfig)
    factory.getInstance().getOAuthAccessToken(reqToken, verifier)
  }
}