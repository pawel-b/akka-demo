package pl.pawelb

import com.typesafe.config.{Config, ConfigFactory}
import java.io.File

/**
 * Base config class to read variables from configuration files
 */
trait BaseConfig {
  def rootConfig: Config

  def getBoolean(path: String, default: Boolean = false) = hasPath(path, default) {
    _.getBoolean(path)
  }

  def getString(path: String, default: String = "") = hasPath(path, default) {
    _.getString(path)
  }

  def getInt(path: String, default: Int = 0) = hasPath(path, default) {
    _.getInt(path)
  }

  def getMilliseconds(path: String, default: Long = 0) = hasPath(path, default) {
    _.getMilliseconds(path)
  }

  def getOptionalString(path: String, default: Option[String] = None) = getOptional(path) {
    _.getString(path)
  }

  def hasPath[T](path: String) = rootConfig.hasPath(path)

  private def hasPath[T](path: String, default: T)(get: Config => T): T = {
    if (rootConfig.hasPath(path)) get(rootConfig) else default
  }

  private def getOptional[T](fullPath: String, default: Option[T] = None)(get: Config => T) = {
    if (rootConfig.hasPath(fullPath)) {
      Some(get(rootConfig))
    } else {
      default
    }
  }
}

trait AkkaDemoConfig extends BaseConfig {
  def commonConfig: Config = ConfigFactory.load()
  def envConfig: Config = ConfigFactory.parseFile(new File("env.conf"));
  def rootConfig = commonConfig.withFallback(envConfig)
}