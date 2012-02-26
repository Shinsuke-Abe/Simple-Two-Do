package com.maosandbox.mongodbutil

/**
 * Created by IntelliJ IDEA.
 * User: mao
 * Date: 12/02/26
 * Time: 21:21
 * To change this template use File | Settings | File Templates.
 */

import com.mongodb.casbah.Imports._

object MongoDB4SimpleTwoDo {
  val conn = MongoConnection()
  val db = conn("simple_twodo")
  val usersDataCollection = db("users_data")
}
