package com.example

import unfiltered.request._
import unfiltered.response._
import util.Properties
import unfiltered.scalate._

// TODO session管理
// TODO Twitter API
class App extends unfiltered.filter.Plan {

  def intent = {
    // Scalate Sample
    case req@GET(Path(Seg("scalate" :: Nil))) => Ok ~> Scalate(req, "hello.ssp")
    // static js sample
    case req@GET(Path(Seg("scalatejs" :: Nil))) => Ok ~> Scalate(req, "hellojs.ssp")
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
