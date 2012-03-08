package com.simpletwodo.propertiesutil

/**
 * Created by IntelliJ IDEA.
 * User: mao
 * Date: 12/03/06
 * Time: 23:13
 * To change this template use File | Settings | File Templates.
 */

import java.io.FileInputStream
import java.io.FileNotFoundException
import scala.util.control.Exception._

object ServerEnvSettings extends AbstractSimpleTwoDoProperties {
  override def propertiesFileStream = new FileInputStream(basedir + "localsetting.properties")

  val propertiesFile = catching(classOf[FileNotFoundException]) opt propertiesFileStream
  if (propertiesFile.isDefined) prop.load(propertiesFile.get)

  override def get(key: String) = {
    val envVal = System.getenv(key)
    if (envVal != null) envVal
    else prop.getProperty(key)
  }
}
