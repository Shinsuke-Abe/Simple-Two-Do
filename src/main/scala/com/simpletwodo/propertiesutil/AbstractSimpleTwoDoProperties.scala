package com.simpletwodo.propertiesutil

/**
 * Created by IntelliJ IDEA.
 * User: mao
 * Date: 12/03/04
 * Time: 18:41
 * To change this template use File | Settings | File Templates.
 */

import java.io.FileInputStream
import java.util.Properties

abstract class AbstractSimpleTwoDoProperties {
  def propertiesFileStream: FileInputStream

  val prop = new Properties()

  def getProperty(key: String) = prop.getProperty(key)
}
