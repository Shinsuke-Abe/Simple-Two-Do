package com.example

import unfiltered.request._
import unfiltered.response._
import util.Properties
import unfiltered.scalate._
import com.maosandbox.twitterutil.TwitterUtil._

// TODO session管理
// TODO POSTメソッドでbody部から値を取り出す
// TODO Twitter API
class App extends unfiltered.filter.Plan {

  def intent = {
    // Scalate Sample
    case req@GET(Path(Seg("scalate" :: Nil))) => Ok ~> Scalate(req, "hello.ssp")
    // static js sample
    case req@GET(Path(Seg("scalatejs" :: Nil))) => Ok ~> Scalate(req, "hellojs.ssp")
    // get my timeline
    case req@GET(Path(Seg("lasttweet" :: Nil))) => {
      // マルチバイト対応(というかブラウザのデフォルトエンコーディングによる文字化けの対応)のため、
      // HtmlContentを明示的に指定する
      Ok ~> HtmlContent ~> ResponseString(getUserLastTweet("mao_instantlife"))
    }
    case req@GET(Path(Seg("gettimeline" :: Nil))) => {
      val statuses = getUserTimeLine("mao_instantlife")

      var tweetStr = new StringBuilder
      statuses.foreach(tweet => tweetStr.append(tweet.getText).append("<br/>"))

      Ok ~> HtmlContent ~> ResponseString(tweetStr.toString())
    }
    // TODO 自分で自分に出したつぶやきの取得
    // TODO OAuth
    // TODO タスクの状態の永続化(Mongo-DBを使う予定)
    // sessionテスト
    // login -> session(cookie)にユーザ情報追加
    case GET(_) => Ok ~> ResponseString("Unfiltered on Heroku!")
    // /public/index.htmlにはアクセス可能
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
