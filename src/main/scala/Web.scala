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

// TODO 設定ファイル
// TODO POSTメソッドでbody部から値を取り出す
// TODO リファクタリング
class TwoDoApplicationServer extends unfiltered.filter.Plan {
  def intent = UserAuth {
    // Scalate Sample
    case req@GET(Path(Seg("scalate" :: Nil))) => Ok ~> Scalate(req, "hello.ssp")
    // static js sample
    case req@GET(Path(Seg("scalatejs" :: Nil))) => Ok ~> Scalate(req, "hellojs.ssp")
    // to do list main page
    case req@GET(Path(Seg("twodolist" :: Nil)) & Cookies(cookies)) => {
      cookies("TwoDoUserId") match {
        case Some(Cookie(_, userIdStr, _, _, _, _)) => {
          SimpleTwoDoDatabase.getUserData(userIdStr.toLong) match {
            case Some(userData) => {
              val twodotweets = getToDoTweets(userData)

              var tweetStr = new StringBuilder
              tweetStr.append("tweet length=").append(twodotweets.length).append("<br/><br/>")
              twodotweets.foreach(tweet => tweetStr.append(tweet.getText).append("<br/><br/>"))

              Ok ~> HtmlContent ~> ResponseString(tweetStr.toString())
            }
            case None => authErr(MessageProperties.getProperty("err.authuser.notfound"))
          }
        }
        case _ => authErr(MessageProperties.getProperty("err.authentication"))
      }
    }
    case GET(_) => NotFound ~> ResponseString(MessageProperties.getProperty("err.requestapi.notfound"))
    // /public/index.htmlにはアクセス可能
  }

  def authErr(message: String) = {
    Unauthorized ~> HtmlContent ~> ResponseString(message)
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
              ResponseCookies(Cookie("TwoDoUserId", accToken.getUserId.toString, maxAge = Some(authCookieAge))) ~> Redirect("twodolist")
            }
            case None => authErr
          }
        }
        case _ => authErr
      }
    }
  }

  def authErr = {
    Unauthorized ~> HtmlContent ~> ResponseString(MessageProperties.getProperty("err.authentication"))
  }
}

/**
 * ユーザ認証判定のためのフィルタ
 */
object UserAuth extends unfiltered.kit.Prepend {
  def intent = Cycle.Intent[Any, Any] {
    case Cookies(cookies) if (cookies.get("TwoDoUserId").isDefined) => {
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
