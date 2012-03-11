package com.example

import unfiltered.request._
import unfiltered.response._
import util.Properties
import unfiltered.scalate._
import com.simpletwodo.twitterutil.SimpleTwoDoTwitter._
import twitter4j._
import com.simpletwodo.requestutil._
import unfiltered.Cycle
import unfiltered.Cookie
import com.simpletwodo.mongodbutil._
import com.simpletwodo.propertiesutil._

// TODO POSTメソッドでbody部から値を取り出す
class TwoDoApplicationServer extends unfiltered.filter.Plan {

  def intent = UserAuth {
    // to do list main page
    case req@GET(Path(Seg("twodolist" :: Nil)) & Cookies(cookies)) => {
      cookies(SimpleTwoDoProperties.get("cookies.userauthorizedkey")) match {
        case Some(Cookie(_, userIdStr, _, _, _, _)) => {
          SimpleTwoDoDatabase.getUserData(userIdStr.toLong) match {
            case Some(userData) => {
              val twodotweets = getToDoTweets(userData).filter(
                tweet => !userData.userTaskList.exists(_.tweetId == tweet.getId)
              )

              val taskAddedUserData = userData.addUserTasks(
                twodotweets.map(
                  tweet => SimpleTwoDoTask(tweet.getId, taskString(tweet.getText, userData.screenName))
                ).toList
              )

              SimpleTwoDoDatabase.updateUserData(taskAddedUserData)

              Ok ~> HtmlContent ~> Scalate(req, "twodolist.scaml", ("userData", taskAddedUserData))
            }
            case None => authErr(MessageProperties.get("err.authuser.notfound"))
          }
        }
        case _ => authErr(MessageProperties.get("err.authentication"))
      }
    }
    case GET(_) => NotFound ~> ResponseString(MessageProperties.get("err.requestapi.notfound"))
  }

  def authErr(message: String) = {
    Unauthorized ~> HtmlContent ~> ResponseString(message)
  }

  def taskString(tweetText: String, screenName: String) = {
    tweetText.replaceFirst("(?i)@" + screenName, "").replaceAll("""(?i)#SimpleTwoDo""", "").trim()
  }
}

/**
 * Twitterを利用したOAuth認証用のPlanクラス
 */
class AuthenticationServer extends unfiltered.filter.Plan {
  private val reqTokenKey = "RequestToken"
  private val sessionKey = "VHdvRG9TZXNzaW9uS2V5"

  private val authCookieAge: Int = 60 * 60 * 24 * 7 // 1週間

  def intent = {
    // Twitter APIからOAuth認証画面にリダイレクトする
    case GET(Path(Seg("authwithtwitter" :: Nil))) => {
      // リクエストトークンをセッションに保存する
      val reqToken = getRequestToken
      val sessionId = SimpleSessionStore.createSession
      SimpleSessionStore.setSessionAttribute(sessionId, reqTokenKey, reqToken)

      ResponseCookies(Cookie(sessionKey, sessionId)) ~> Redirect(reqToken.getAuthenticationURL)
    }
    // Twitterの認証画面からコールバックされるURL
    case GET(Path(Seg("getaccesstoken" :: Nil)) & Cookies(cookies) & Params(param)) => {
      def p(k: String) = param.get(k).flatMap {
        _.headOption
      } getOrElse ("")

      cookies(sessionKey) match {
        case Some(Cookie(_, sessionId, _, _, _, _)) => {
          SimpleSessionStore.getSessionAttribute(sessionId, reqTokenKey) match {
            case Some(reqToken) => {
              val verifier = p("oauth_verifier")
              val accToken = getAccessToken(reqToken.asInstanceOf[auth.RequestToken], verifier)

              // リクエストトークンは不要になるので、セッションから削除
              SimpleSessionStore.removeSessionAttribute(sessionId, reqTokenKey)

              if (SimpleTwoDoDatabase.getUserData(accToken.getUserId).isEmpty) {
                SimpleTwoDoDatabase.insertUserData(
                  SimpleTwoDoUserData.apply(
                    accToken.getUserId,
                    accToken.getScreenName,
                    accToken.getToken,
                    accToken.getTokenSecret
                  )
                )
              }

              // CookieにユーザIDをセットしてリストページにリダイレクトする
              ResponseCookies(
                Cookie(
                  SimpleTwoDoProperties.get("cookies.userauthorizedkey"),
                  accToken.getUserId.toString,
                  maxAge = Some(authCookieAge)
                )
              ) ~> Redirect("twodolist")
            }
            case None => authErr
          }
        }
        case _ => authErr
      }
    }
  }

  def authErr = {
    Unauthorized ~> HtmlContent ~> ResponseString(MessageProperties.get("err.authentication"))
  }
}

/**
 * ユーザ認証判定のためのフィルタ
 */
object UserAuth extends unfiltered.kit.Prepend {
  def intent = Cycle.Intent[Any, Any] {
    case Cookies(cookies) if (cookies.get(SimpleTwoDoProperties.get("cookies.userauthorizedkey")).isDefined) => {
      Pass
    }
    case _ => {
      Redirect("/authwithtwitter")
    }
  }
}

object Web {
  def main(args: Array[String]) {
    val port = Properties.envOrElse("PORT", "8080").toInt
    unfiltered.jetty.Http(port).context("/public") {
      // add context for static contents "src/main/resource/public"
      _.resources(getClass().getResource("/public/"))
    }.filter(new AuthenticationServer).filter(new TwoDoApplicationServer).run
  }
}
