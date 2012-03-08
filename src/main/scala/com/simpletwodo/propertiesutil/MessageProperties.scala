package com.simpletwodo.propertiesutil

/**
 * Properties class implements for message settings.
 * User: mao
 * Date: 12/03/04
 * Time: 18:59
 */

import java.io.FileInputStream

object MessageProperties extends AbstractSimpleTwoDoProperties {
  override def propertiesFileStream = new FileInputStream(basedir + "message.properties")

  prop.load(propertiesFileStream)
}
