package com.example

import unfiltered.request._
import unfiltered.response._
import util.Properties
import unfiltered.scalate._

class App extends unfiltered.filter.Plan {

  def intent = {
    // Scalate Sample
    case req @ GET(Path(Seg("scalate" :: Nil))) => Ok ~> Scalate(req, "hello.ssp")
    case GET(_) => Ok ~> ResponseString("Unfiltered on Heroku!")
  }
}

object Web {
  def main(args: Array[String]) {
    val port = Properties.envOrElse("PORT", "8080").toInt
    unfiltered.jetty.Http(port).filter(new App).run
  }
}
