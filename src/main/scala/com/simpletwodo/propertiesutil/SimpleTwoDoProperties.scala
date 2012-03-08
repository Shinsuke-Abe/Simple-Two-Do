package com.simpletwodo.propertiesutil

/**
 * Properties class implements for Simple TwoDo basic settings.
 * User: mao
 * Date: 12/03/04
 * Time: 19:03
 */

import java.io.FileInputStream

object SimpleTwoDoProperties extends AbstractSimpleTwoDoProperties {
  override def propertiesFileStream = new FileInputStream(basedir + "simpletwodo.properties")

  prop.load(propertiesFileStream)
}
