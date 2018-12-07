package jmstestkit

import java.net.URI
import java.util.UUID

import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.broker.{BrokerFactory, BrokerService}

import scala.util.Try

class JmsBroker(val service: BrokerService) {
  checkState()

  def isStarted: Boolean = service.isStarted
  def isStopped: Boolean = service.isStopped

  def brokerUri: String = service.getDefaultSocketURIString

  def closeClientConnections(): Unit = {
    for (clientConn <- service.getBroker.getClients) {
      Try { clientConn.stop() }
    }
  }

  def clientConnectionCount: Int = service.getBroker.getClients.size

  def createQueueConnectionFactory: javax.jms.QueueConnectionFactory = {
    checkState()
    new ActiveMQConnectionFactory(service.getDefaultSocketURIString)
  }

  def createTopicConnectionFactory: javax.jms.TopicConnectionFactory = {
    checkState()
    new ActiveMQConnectionFactory(service.getDefaultSocketURIString)
  }

  def createConnectionFactory: javax.jms.ConnectionFactory = {
    checkState()
    new ActiveMQConnectionFactory(service.getDefaultSocketURIString)
  }

  def start(force: Boolean = true): Unit = service.start(force)

  def restart(): Unit = {
    stop()
    start(force = true)
  }

  def stop(): Unit = service.stop()

  private def checkState(): Unit = {
    if (service.isStopped) {
      throw new IllegalStateException("Broker is stopped")
    } else if (service.isStopping) {
      throw new IllegalStateException("Broker is stopping")
    }
  }
}

object JmsBroker {
  def apply(): JmsBroker = {
    val inMemoryBrokerName = "brokerName-" + UUID.randomUUID.toString
    val transportUri = s"vm://${inMemoryBrokerName}?create=false"
    val brokerConfigUri = new URI(s"broker:(${transportUri})/${inMemoryBrokerName}?persistent=false&useJmx=false")

    val brokerService = BrokerFactory.createBroker(brokerConfigUri)
    brokerService.setPersistent(false)
    brokerService.setUseJmx(false)
    brokerService.setStartAsync(false)
    brokerService.start()
    new JmsBroker(brokerService)
  }
}
