package org.bitcoins.server

import java.nio.file.Path

import com.typesafe.config.{Config, ConfigFactory}
import org.bitcoins.chain.config.ChainAppConfig
import org.bitcoins.core.util.StartStopAsync
import org.bitcoins.db.AppConfig
import org.bitcoins.node.config.NodeAppConfig
import org.bitcoins.wallet.config.WalletAppConfig

import scala.concurrent.{ExecutionContext, Future}

/**
  * A unified config class for all submodules of Bitcoin-S
  * that accepts configuration. Thanks to implicit definitions
  * in this case class' companion object an instance
  * of this class can be passed in anywhere a wallet,
  * chain or node config is required.
  *
  * @param directory The data directory of this app configuration
  * @param confs A sequence of optional configuration overrides
  */
case class BitcoinSAppConfig(
    private val directory: Path,
    private val confs: Config*)(implicit ec: ExecutionContext)
    extends StartStopAsync[Unit] {
  lazy val walletConf: WalletAppConfig = WalletAppConfig(directory, confs: _*)
  lazy val nodeConf: NodeAppConfig = NodeAppConfig(directory, confs: _*)
  lazy val chainConf: ChainAppConfig = ChainAppConfig(directory, confs: _*)

  lazy val bitcoindRpcConf: BitcoindRpcAppConfig =
    BitcoindRpcAppConfig(directory, confs: _*)

  /** Initializes the wallet, node and chain projects */
  override def start(): Future[Unit] = {
    val futures = List(walletConf.start(),
                       nodeConf.start(),
                       chainConf.start(),
                       bitcoindRpcConf.start())

    Future.sequence(futures).map(_ => ())
  }

  override def stop(): Future[Unit] = {
    for {
      _ <- nodeConf.stop()
      _ <- walletConf.stop()
      _ <- chainConf.stop()
      _ <- bitcoindRpcConf.stop()
    } yield ()
  }

  /** The underlying config the result of our fields derive from */
  lazy val config: Config = {
    val finalConfig =
      AppConfig.getBaseConfig(baseDatadir = directory, confs.toList)
    val resolved = finalConfig.resolve()

    resolved.checkValid(ConfigFactory.defaultReference(), "bitcoin-s")

    resolved
  }

  def serverConf: Config = {
    config.getConfig("bitcoin-s.server")
  }

  def rpcPortOpt: Option[Int] = {
    if (serverConf.hasPath("bitcoin-s.server.rpcport")) {
      Some(serverConf.getInt("bitcoin-s.server.rpcport"))
    } else {
      None
    }
  }
}

/**
  * Implicit conversions that allow a unified configuration
  * to be passed in wherever a specializes one is required
  */
object BitcoinSAppConfig {

  /** Constructs an app configuration from the default Bitcoin-S
    * data directory and given list of configuration overrides.
    */
  def fromDefaultDatadir(confs: Config*)(implicit
      ec: ExecutionContext): BitcoinSAppConfig =
    BitcoinSAppConfig(AppConfig.DEFAULT_BITCOIN_S_DATADIR, confs: _*)

  import scala.language.implicitConversions

  /** Converts the given implicit config to a wallet config */
  implicit def implicitToWalletConf(implicit
      conf: BitcoinSAppConfig): WalletAppConfig =
    conf.walletConf

  /** Converts the given config to a wallet config */
  implicit def toWalletConf(conf: BitcoinSAppConfig): WalletAppConfig =
    conf.walletConf

  /** Converts the given implicit config to a chain config */
  implicit def implicitToChainConf(implicit
      conf: BitcoinSAppConfig): ChainAppConfig =
    conf.chainConf

  /** Converts the given config to a chain config */
  implicit def toChainConf(conf: BitcoinSAppConfig): ChainAppConfig =
    conf.chainConf

  /** Converts the given implicit config to a node config */
  implicit def implicitToNodeConf(implicit
      conf: BitcoinSAppConfig): NodeAppConfig =
    conf.nodeConf

  /** Converts the given config to a node config */
  implicit def toNodeConf(conf: BitcoinSAppConfig): NodeAppConfig =
    conf.nodeConf

  /** Converts the given implicit config to a bitcoind rpc config */
  implicit def implicitToBitcoindRpcConf(implicit
      conf: BitcoinSAppConfig): BitcoindRpcAppConfig =
    conf.bitcoindRpcConf

  /** Converts the given config to a bitcoind rpc config */
  implicit def toBitcoindRpcConf(
      conf: BitcoinSAppConfig): BitcoindRpcAppConfig =
    conf.bitcoindRpcConf

}