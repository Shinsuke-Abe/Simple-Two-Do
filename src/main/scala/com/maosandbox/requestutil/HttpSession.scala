package com.maosandbox.requestutil

/**
 * Http Session Object
 * User: mao
 * Date: 12/02/15
 * Time: 22:06
 * Implementation like http sessions mechanism with straight cookie
 */

import scala.collection._
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.{HttpSession => ServletHttpSession}
import unfiltered.request._

/**
 * セッション管理の実装
 * セッションデータの型はマップ、セッションIDは文字列とする
 */
object SimpleSessionStore {
  private val storage = mutable.Map[String, mutable.Map[String, Any]]()

  def createSession = {
    val sid = generateSid
    SimpleSessionStore.synchronized {
      storage += (sid -> mutable.Map[String, Any]())
    }
    sid
  }

  def getSession(sid: String) = storage get sid

  def setSessionAttribute(sid: String, attrKey: String, attrValue: Any) {
    storage get sid match {
      case Some(attrMap) => attrMap.put(attrKey, attrValue)
      case None => throw new IllegalStateException("session Id is not generated")
    }
  }

  def getSessionAttribute(sid: String, attrKey: String) = {
    storage get sid match {
      case Some(attrMap) => attrMap.get(attrKey)
      case None => throw new IllegalStateException("session Id is not generated")
    }
  }

  def removeSessionAttribute(sid: String, attrKey: String) {
    storage get sid match {
      case Some(attrMap) => attrMap.remove(attrKey)
      case None => throw new IllegalStateException("session Id is not generated")
    }
  }

  /**
   * セッションIDの生成メソッド
   * 英数字256桁のランダムなセッションIDを返す
   * @return 生成されたセッションID(文字列)
   */
  protected def generateSid = scala.util.Random.alphanumeric.take(256).mkString
}

object HttpSession {
  def unapply(req: HttpRequest[HttpServletRequest]): Option[ServletHttpSession] = {
    if (req.underlying.getSession(false) != null) Some(req.underlying.getSession(false))
    else None
  }
}
