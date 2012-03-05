package com.simpletwodo.propertiesutil

/**
 * Created by IntelliJ IDEA.
 * User: mao
 * Date: 12/03/04
 * Time: 19:03
 * To change this template use File | Settings | File Templates.
 */

import java.io.FileInputStream

object SimpleTwoDoProperties extends AbstractSimpleTwoDoProperties {
  override def propertiesFileStream = new FileInputStream("simpletwodo.properties")

  prop.load(propertiesFileStream)
}
