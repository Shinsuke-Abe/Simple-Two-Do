package com.simpletwodo.requestutil

/**
 * Http Session Object
 * User: mao
 * Date: 12/02/15
 * Time: 22:06
 * Implementation like http sessions mechanism with straight cookie
 */

import scala.collection._
import com.simpletwodo.propertiesutil.MessageProperties

/**
 * セッション管理オブジェクト
 * セッションデータの型はマップ、セッションIDは文字列とする
 */
object SimpleSessionStore {
  private val storage = mutable.Map[String, mutable.Map[String, Any]]()

  /**
   * セッションの生成
   * @return 生成したセッションのセッションID
   */
  def createSession = {
    val sid = generateSid
    SimpleSessionStore.synchronized {
      storage += (sid -> mutable.Map[String, Any]())
    }
    sid
  }

  /**
   * セッションデータを取得する
   * @param sid セッションID
   * @return セッションIDに対応するセッションデータのマップ
   */
  def getSession(sid: String) = storage get sid

  /**
   * セッションデータを削除する
   * @param sid セッションID
   */
  def removeSession(sid: String) {
    storage remove sid
  }

  /**
   * セッション属性をセットする。
   * 生成されていないセッションIDを指定した場合はIllegalStateExceptionがスローされる。
   * @param sid セッションID
   * @param attrKey セッション属性のキー
   * @param attrValue セッション属性の値
   */
  def setSessionAttribute(sid: String, attrKey: String, attrValue: Any) {
    storage get sid match {
      case Some(attrMap) => attrMap.put(attrKey, attrValue)
      case None => throw new IllegalStateException(MessageProperties.get("err.sessionid.notfound"))
    }
  }

  /**
   * セッション属性を取得する。
   * 生成されていないセッションIDを指定した場合はIllegalStateExceptionがスローされる。
   * @param sid セッションID
   * @param attrKey セッション属性のキー
   * @return セッション属性の値
   */
  def getSessionAttribute(sid: String, attrKey: String) = {
    storage get sid match {
      case Some(attrMap) => attrMap.get(attrKey)
      case None => throw new IllegalStateException(MessageProperties.get("err.sessionid.notfound"))
    }
  }

  /**
   * セッション属性を削除する。
   * 生成されていないセッションIDを指定した場合はIllegalStateExceptionがスローされる。
   * @param sid セッションID
   * @param attrKey セッション属性のキー
   */
  def removeSessionAttribute(sid: String, attrKey: String) {
    storage get sid match {
      case Some(attrMap) => attrMap.remove(attrKey)
      case None => throw new IllegalStateException(MessageProperties.get("err.sessionid.notfound"))
    }
  }

  /**
   * セッションIDの生成メソッド
   * 英数字256桁のランダムなセッションIDを返す
   * @return 生成されたセッションID(文字列)
   */
  protected def generateSid = scala.util.Random.alphanumeric.take(256).mkString
}