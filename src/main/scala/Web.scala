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

/**
 * SimpleTwoDoのメイン処理を行うPlanクラス
 */
class TwoDoApplicationServer extends unfiltered.filter.Plan with SimpleTwoDoServer {

  def intent = UserAuth {
    case req@(Cookies(cookies)) if (cookies.get(authKey).isDefined) => {
      // UserAuthフィルタで存在は確認済
      val userId = cookies(authKey).get.value.toLong

      getUser(userId) match {
        case Some(userData) => {
          req match {
            // to do list main page
            case GET(Path(Seg("twodolist" :: Nil))) => {
              val twodotweets = getToDoTweets(userData).filter(
                tweet => !userData.userTaskList.exists(_.tweetId == tweet.getId)
              )

              val taskAddedUserData = userData.addUserTasks(
                twodotweets.map(
                  tweet => SimpleTwoDoTask(tweet.getId, taskString(tweet.getText, userData.screenName))
                ).toList
              )

              updateUser(taskAddedUserData)

              Ok ~> HtmlContent ~> Scalate(req, templateName, ("userData", taskAddedUserData))
            }

            // task status change request
            case POST(Path(Seg("changetaskstatus" :: tweetId :: status :: Nil))) => {
              val taskUpdatedUserData = userData.updateUserTasks(
                userData.userTaskList.map(
                  userTask =>
                    if (userTask.tweetId == tweetId.toLong)
                      SimpleTwoDoTask(
                        userTask.tweetId,
                        userTask.tweetStatus,
                        status.toBoolean
                      )
                    else userTask
                )
              )

              try {
                updateUser(taskUpdatedUserData)
                Ok ~> JsonContent ~> ResponseString("""{"result": true}""")
              } catch {
                case ex: Exception =>
                  Ok ~> JsonContent ~> ResponseString("""{"result": false, "errmsg": %str}""".format(ex.getMessage))
              }
            }

            // default routing
            case Path(Seg(Nil)) => {
              Redirect("twodolist")
            }

            // not found request
            case _ => NotFound ~> ResponseString(err404Msg)
          }
        }
        case None => authErr(authUserNotFoundMsg)
      }
    }
    case _ => authErr(authErrMsg)
  }

  def taskString(tweetText: String, screenName: String) = {
    tweetText.replaceFirst("(?i)@" + screenName, "").replaceAll("""(?i)#SimpleTwoDo""", "").trim()
  }
}

/**
 * Twitterを利用したOAuth認証用のPlanクラス
 */
class AuthenticationServer extends unfiltered.filter.Plan with SimpleTwoDoServer {
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

              if (getUser(accToken.getUserId).isEmpty) insertNewUser(accToken)

              // CookieにユーザIDをセットしてリストページにリダイレクトする
              ResponseCookies(
                Cookie(
                  authKey,
                  accToken.getUserId.toString,
                  maxAge = Some(authCookieAge)
                )
              ) ~> Redirect("twodolist")
            }
            case None => authErr(authErrMsg)
          }
        }
        case _ => authErr(authErrMsg)
      }
    }
  }
}

/**
 * ユーザ認証判定のためのフィルタ
 */
object UserAuth extends unfiltered.kit.Prepend with SimpleTwoDoServer {
  def intent = Cycle.Intent[Any, Any] {
    case Cookies(cookies) if (cookies.get(authKey).isDefined) => {
      Pass
    }
    case _ => {
      Redirect("/authwithtwitter")
    }
  }
}

/**
 * 共通的に利用する文字列・処理をまとめたトレイト
 */
trait SimpleTwoDoServer {
  val authKey = SimpleTwoDoProperties.get("cookies.userauthorizedkey")
  val authErrMsg = MessageProperties.get("err.authentication")
  val authUserNotFoundMsg = MessageProperties.get("err.authuser.notfound")
  val err404Msg = MessageProperties.get("err.requestapi.notfound")
  val templateName = "twodolist.scaml"

  def authErr(message: String) = {
    Unauthorized ~> HtmlContent ~> ResponseString(message)
  }

  def insertNewUser(accToken: auth.AccessToken) {
    SimpleTwoDoDatabase.insertUserData(
      SimpleTwoDoUserData.apply(
        accToken.getUserId,
        accToken.getScreenName,
        accToken.getToken,
        accToken.getTokenSecret
      )
    )
  }

  def updateUser(userData: SimpleTwoDoUserData) {
    SimpleTwoDoDatabase.updateUserData(userData)
  }

  def getUser(userId: Long) = {
    SimpleTwoDoDatabase.getUserData(userId)
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
