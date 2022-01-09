package org.bitcoins.server

import org.bitcoins.cli.{CliCommand, Config, ConsoleCli}
import org.bitcoins.commons.util.ServerArgParser
import org.bitcoins.testkit.fixtures.BitcoinSAppConfigBitcoinFixtureNotStarted

/** Test starting bitcoin-s with bitcoind as the backend for app */
class BitcoinSServerMainBitcoindTest
    extends BitcoinSAppConfigBitcoinFixtureNotStarted {

  behavior of "BitcoinSServerMain"

  it must "start our app server with bitcoind as a backend" in {
    config: BitcoinSAppConfig =>
      val server = new BitcoinSServerMain(ServerArgParser.empty)(system, config)

      val cliConfig = Config(rpcPortOpt = Some(config.rpcPort))

      for {
        _ <- server.start()

        info = ConsoleCli.exec(CliCommand.WalletInfo, cliConfig)
        balance = ConsoleCli.exec(CliCommand.GetBalance(isSats = true),
                                  cliConfig)
        addr = ConsoleCli.exec(CliCommand.GetNewAddress(labelOpt = None),
                               cliConfig)
        blockHash = ConsoleCli.exec(CliCommand.GetBestBlockHash, cliConfig)
      } yield {
        assert(info.isSuccess)
        assert(balance.isSuccess)
        assert(balance.get == "0")
        assert(addr.isSuccess)
        assert(blockHash.isSuccess)
      }
  }
}