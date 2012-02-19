package com.example

import unfiltered.request._
import unfiltered.response._
import util.Properties
import unfiltered.scalate._
import com.maosandbox.twitterutil.TwitterUtil._
import twitter4j._
import com.maosandbox.requestutil._

// TODO session管理
// TODO POSTメソッドでbody部から値を取り出す
// TODO Twitter API
class App extends unfiltered.filter.Plan {

  def intent = {
    // Scalate Sample
    case req@GET(Path(Seg("scalate" :: Nil))) => Ok ~> Scalate(req, "hello.ssp")
    // static js sample
    case req@GET(Path(Seg("scalatejs" :: Nil))) => Ok ~> Scalate(req, "hellojs.ssp")
    // get my last one tweet
    case req@GET(Path(Seg("lasttweet" :: Nil))) => {
      // マルチバイト対応(というかブラウザのデフォルトエンコーディングによる文字化けの対応)のため、
      // HtmlContentを明示的に指定する
      Ok ~> HtmlContent ~> ResponseString(getUserLastTweet("mao_instantlife"))
    }
    // get my timeline
    case req@GET(Path(Seg("gettimeline" :: Nil))) => {
      val statuses = getUserTimeLine("mao_instantlife")

      var tweetStr = new StringBuilder
      statuses.foreach(tweet => tweetStr.append(tweet.getText).append("<br/>"))

      Ok ~> HtmlContent ~> ResponseString(tweetStr.toString())
    }
    // twodo で利用するツイートの取得
    case req@GET(Path(Seg("gettwodotweets" :: Nil))) => {
      val twodotweets = getQueryTweets(queryGenerate("mao_instantlife", "2012-02-12"))

      var tweetStr = new StringBuilder
      tweetStr.append("tweet length=").append(twodotweets.length).append("<br/><br/>")
      twodotweets.foreach(tweet => tweetStr.append(tweet.getText).append("<br/><br/>"))

      Ok ~> HtmlContent ~> ResponseString(tweetStr.toString())
    }
    // Twitter APIからOAuth認証画面にリダイレクトする
    case req@GET(Path(Seg("authwithtwitter" :: Nil))) => {
      // リクエストトークンをセッションに保存する
      val reqToken = getRequestToken
      var session = req.underlying.getSession(true)
      session.setAttribute("RequestToken", reqToken)

      Redirect(reqToken.getAuthenticationURL)
    }
    // Twitterの認証画面からコールバックされるURL
    case GET(Path(Seg("getaccesstoken" :: Nil)) & HttpSession(session) & Params(param)) => {
      def p(k: String) = param.get(k).flatMap {
        _.headOption
      } getOrElse ("")
      // セッションからリクエストトークンを取得する
      val reqToken = session.getAttribute("RequestToken").asInstanceOf[auth.RequestToken]
      // リクエストパラメータからoauth_verifierを取得する
      val verifier = p("oauth_verifier")

      val accToken = getAccessToken(reqToken, verifier)

      // リクエストトークンは不要になるので、セッションから削除
      session.removeAttribute("RequestToken")

      Ok ~> HtmlContent ~> ResponseString("token=" + accToken.getToken + "<br/>tokenSecret=" + accToken.getTokenSecret)
    }
    // TODO OAuth
    // TODO アクセストークンの永続化
    // TODO タスクの状態の永続化(Mongo-DBを使う予定)
    // sessionテスト
    // login -> session(cookie)にユーザ情報追加
    case GET(_) => Ok ~> ResponseString("Unfiltered on Heroku!")
    // /public/index.htmlにはアクセス可能
  }

  private def queryGenerate(screenId: String, lastAccessDate: String): Query = {
    val ret = new Query("from:" + screenId + " to:" + screenId + " #SimpleTwoDo")
    ret.setSince(lastAccessDate)
    ret.setRpp(100)

    ret
  }
}

object Web {
  def main(args: Array[String]) {
    val port = Properties.envOrElse("PORT", "8080").toInt
    unfiltered.jetty.Http(port).context("/public") {
      // add context for static contents "src/main/resource/public"
      _.resources(getClass().getResource("/public/"))
    }.filter(new App).run
  }
}
