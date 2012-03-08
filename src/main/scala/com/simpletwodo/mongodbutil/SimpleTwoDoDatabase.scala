package com.simpletwodo.mongodbutil

/**
 * SimpleTwoDo database and data entities
 * User: mao
 * Date: 12/02/26
 * Time: 21:21
 * User data and task list are stored MongoDB.
 * Connection string of MongoDB are environment values.
 */

import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import com.mongodb.MongoURI
import com.simpletwodo.propertiesutil.MessageProperties
import com.simpletwodo.propertiesutil.ServerEnvSettings

/**
 * MongoDBへの接続とユーザデータとタスクリストのCRUDを管理します。
 */
object SimpleTwoDoDatabase {
  val conn = MongoConnection(new MongoURI(ServerEnvSettings.get("MONGOLAB_URI")))
  val db = conn("simple_twodo")
  val usersDataCollection = db("users_data")

  private val userIdKey = "userId"

  val g = grater[SimpleTwoDoUserData]

  /**
   * 引数で指定されたユーザデータをデータベースに登録します。
   * @param userData ユーザ情報
   */
  def insertUserData(userData: SimpleTwoDoUserData) {
    getUserData(userData.userId) match {
      case Some(dbUserData) if userData == dbUserData =>
        throw new IllegalArgumentException(MessageProperties.get("err.database.duplicate").format(userData.userId))
      case _ => usersDataCollection += g.asDBObject(userData)
    }
  }

  /**
   * 引数で指定されたユーザデータを更新します。
   * 更新は、DELETE INSERTです。
   * @param userData ユーザ情報
   */
  def updateUserData(userData: SimpleTwoDoUserData) {
    removeUserData(userData.userId)
    insertUserData(userData)
  }

  /**
   * 指定したユーザIDのユーザデータを削除します。
   * @param userId ユーザID
   */
  def removeUserData(userId: Long) {
    usersDataCollection.remove(MongoDBObject(userIdKey -> userId))
  }

  /**
   * 指定したユーザIDのユーザデータを取得します。
   * @param userId ユーザID
   * @return ユーザデータ
   */
  def getUserData(userId: Long) = {
    usersDataCollection.findOne(MongoDBObject(userIdKey -> userId)) match {
      case Some(dbData) => Some(g.asObject(dbData))
      case None => None
    }
  }
}

/**
 * ユーザデータクラス
 * @param userId ユーザID
 * @param screenName 表示名
 * @param accsToken OAuth認証情報(アクセストークン)
 * @param tokenSecret OAuth認証情報(トークンシークレット)
 * @param userTaskList ユーザごとのタスクリスト
 */
case class SimpleTwoDoUserData(
                                userId: Long,
                                screenName: String,
                                accsToken: String,
                                tokenSecret: String,
                                userTaskList: List[SimpleTwoDoTask] = List[SimpleTwoDoTask]()
                                ) {
  override def equals(other: Any) = other match {
    case that: SimpleTwoDoUserData =>
      that.userId == this.userId && that.accsToken == this.accsToken && that.tokenSecret == this.tokenSecret
    case _ => false
  }

  /**
   * 引数で指定されたタスクを追加したユーザデータを返します。
   * @param addTaskList 追加するタスクのリスト
   * @return 新規生成したユーザデータ
   */
  def addUserTasks(addTaskList: List[SimpleTwoDoTask]) = {
    SimpleTwoDoUserData(userId, screenName, accsToken, tokenSecret, userTaskList ::: addTaskList)
  }

  /**
   * 引数で指定したタスクリストに変更したユーザデータを返します。
   * @param updateTaskList 変更するユーザデータのリスト
   * @return 新規生成したユーザデータ
   */
  def updateUserTasks(updateTaskList: List[SimpleTwoDoTask]) = {
    SimpleTwoDoUserData(userId, screenName, accsToken, tokenSecret, updateTaskList)
  }

  /**
   * 引数で指定した表示名に変更したユーザデータを返します。
   * @param newScreenName 新しいユーザデータ
   * @return 新規生成したユーザデータ
   */
  def updateScreenName(newScreenName: String) = {
    SimpleTwoDoUserData(userId, newScreenName, accsToken, tokenSecret, userTaskList)
  }

  /**
   * 引数で指定したOAuth認証情報に変更したユーザデータを返します。
   * @param newAccsToken 新しいアクセストークン
   * @param newTokenSecret 新しいトークンシークレット
   * @return 新規生成したユーザデータ
   */
  def updateOAuthInfo(newAccsToken: String, newTokenSecret: String) = {
    SimpleTwoDoUserData(userId, screenName, newAccsToken, newTokenSecret, userTaskList)
  }
}

/**
 * 個別のタスクを管理するクラス。
 * @param tweetId タスクとなるツイートのID
 * @param tweetStatus ツイートの内容(メンションとハッシュタグはのぞく)
 * @param taskStatus タスクの状況(false=>to do true=>done)
 */
case class SimpleTwoDoTask(tweetId: Long, tweetStatus: String, var taskStatus: Boolean) {
  override def equals(other: Any) = other match {
    case that: SimpleTwoDoTask => that.tweetId == this.tweetId
    case _ => false
  }
}