package com.simpletwodo.propertiesutil

/**
 * Created by IntelliJ IDEA.
 * User: mao
 * Date: 12/03/04
 * Time: 18:59
 * To change this template use File | Settings | File Templates.
 */

import java.io.FileInputStream

object MessageProperties extends AbstractSimpleTwoDoProperties {
  override def propertiesFileStream = new FileInputStream("target/classes/message.properties")

  prop.load(propertiesFileStream)
}
