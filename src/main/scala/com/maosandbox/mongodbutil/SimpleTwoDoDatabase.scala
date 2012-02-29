package com.maosandbox.mongodbutil

/**
 * Created by IntelliJ IDEA.
 * User: mao
 * Date: 12/02/26
 * Time: 21:21
 * To change this template use File | Settings | File Templates.
 */

import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._

object SimpleTwoDoDatabase {
  val conn = MongoConnection()
  val db = conn("simple_twodo")
  val usersDataCollection = db("users_data")

  val g = grater[SimpleTwoDoUserData]

  def insertUserData(userData: SimpleTwoDoUserData) {
    getUserData(userData.userId) match {
      case Some(dbUserData) if userData == dbUserData => throw new IllegalArgumentException("insertUserData error:user data duplicate. userId = " + userData.userId.toString + ".")
      case _ => usersDataCollection += g.asDBObject(userData)
    }
  }

  def updateUserData(userData: SimpleTwoDoUserData) {
    usersDataCollection.remove(MongoDBObject("userId" -> userData.userId))
    insertUserData(userData)
  }

  def getUserData(userId: Long) = {
    usersDataCollection.findOne(MongoDBObject("userId" -> userId)) match {
      case Some(dbData) => Some(g.asObject(dbData))
      case None => None
    }
  }
}

case class SimpleTwoDoUserData(
                                userId: Long,
                                screenName: String,
                                accsToken: String,
                                tokenSecret: String,
                                userTaskList: List[SimpleTwoDoTask] = List[SimpleTwoDoTask]()
                                ) {
  override def equals(other: Any) = other match {
    case that: SimpleTwoDoUserData => that.userId == this.userId && that.accsToken == this.accsToken && that.tokenSecret == this.tokenSecret
    case _ => false
  }

  def addUserTasks(addTaskList: List[SimpleTwoDoTask]) = {
    SimpleTwoDoUserData(userId, screenName, accsToken, tokenSecret, userTaskList ::: addTaskList)
  }

  def updateUserTasks(updateTaskList: List[SimpleTwoDoTask]) = {
    SimpleTwoDoUserData(userId, screenName, accsToken, tokenSecret, updateTaskList)
  }
}

case class SimpleTwoDoTask(tweetId: Long, tweetStatus: String, var taskStatus: Boolean) {
  override def equals(other: Any) = other match {
    case that: SimpleTwoDoTask => that.tweetId == this.tweetId
    case _ => false
  }
}