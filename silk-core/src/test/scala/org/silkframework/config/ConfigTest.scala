package org.silkframework.config

import javax.inject.Inject
import com.google.inject.Guice
import com.typesafe.config.{Config => TypesafeConfig}
import net.codingwell.scalaguice.ScalaModule
import org.scalatest.{FlatSpec, MustMatchers}

import java.time.Instant
/**
  * Created on 9/27/16.
  */
class ConfigTest extends FlatSpec with MustMatchers {
  behavior of "Config dependency injection"
  it should "inject into Scala objects" in {
    Guice.createInjector(new ScalaModule() {
      override def configure() {
        bind[Config].to(classOf[DefaultConfig])
        bind[ConfigTestHelper.type].toInstance(ConfigTestHelper)
      }
    })
    ConfigTestHelper.get mustBe a[DefaultConfig]
  }

  it should "override Config with a custom test version" in {
    ConfigTestHelper.get mustBe a[DefaultConfig]
    Guice.createInjector(new ScalaModule() {
      override def configure() {
        bind[Config].to[TestConfig]
        bind[ConfigTestHelper.type].toInstance(ConfigTestHelper)
      }
    })
    ConfigTestHelper.get mustBe a[TestConfig]
  }
}

class TestConfig extends Config {
  override def apply(): TypesafeConfig = null

  /** Refreshes the Config instance, e.g. load from changed config file or newly set property values. */
  override def refresh(): Unit = {}

  override def timestamp: Instant = Instant.now()
}

object ConfigTestHelper {
  @Inject
  private var configMgr: Config = DefaultConfig.instance

  def get(): Config = configMgr
}