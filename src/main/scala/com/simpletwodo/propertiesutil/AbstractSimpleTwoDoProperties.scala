package com.simpletwodo.propertiesutil

/**
 * Abstract class for use property files
 * User: mao
 * Date: 12/03/04
 * Time: 18:41
 * If you use .properties files.
 * Implement this abstract class.
 */

import java.io.FileInputStream
import java.util.Properties

abstract class AbstractSimpleTwoDoProperties {
  val prop = new Properties()
  val basedir = "target/scala-2.9.1/classes/"

  /**
   * プロパティファイルのストリームを取得します。
   * 継承クラスで実装してください。
   *
   * @return プロパティファイルのストリーム
   */
  def propertiesFileStream: FileInputStream

  /**
   * 指定されたキーの値を取得します。
   *
   * @param key キー文字列
   * @return 値(存在しない場合はnull)
   */
  def get(key: String) = prop.getProperty(key)
}
