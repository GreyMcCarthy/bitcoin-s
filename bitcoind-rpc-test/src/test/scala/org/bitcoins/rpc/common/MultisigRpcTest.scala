package org.bitcoins.rpc.common

import org.bitcoins.commons.jsonmodels.bitcoind.RpcOpts.AddressType
import org.bitcoins.core.protocol.P2PKHAddress
import org.bitcoins.crypto.ECPrivateKey
import org.bitcoins.rpc.client.common.{BitcoindRpcClient, BitcoindVersion}
import org.bitcoins.rpc.config.BitcoindInstanceLocal
import org.bitcoins.testkit.rpc.BitcoindRpcTestUtil
import org.bitcoins.testkit.util.BitcoindRpcTest

import scala.concurrent.Future

class MultisigRpcTest extends BitcoindRpcTest {

  val instance: BitcoindInstanceLocal =
    BitcoindRpcTestUtil.instance(versionOpt = Some(BitcoindVersion.V21))

  lazy val clientF: Future[BitcoindRpcClient] =
    BitcoindRpcTestUtil.startedBitcoindRpcClient(instanceOpt = Some(instance),
                                                 clientAccum = clientAccum)

  behavior of "MultisigRpc"

  it should "be able to create a multi sig address" in {
    val ecPrivKey1 = ECPrivateKey.freshPrivateKey
    val ecPrivKey2 = ECPrivateKey.freshPrivateKey

    val pubKey1 = ecPrivKey1.publicKey
    val pubKey2 = ecPrivKey2.publicKey

    for {
      client <- clientF
      _ <- client.createMultiSig(2, Vector(pubKey1, pubKey2))
    } yield succeed
  }

  it should "be able to add a multi sig address to the wallet" in {
    val ecPrivKey1 = ECPrivateKey.freshPrivateKey
    val pubKey1 = ecPrivKey1.publicKey

    for {
      client <- clientF
      address <- client.getNewAddress(addressType = AddressType.Legacy)
      _ <- {
        val pubkey = Left(pubKey1)
        val p2pkh = Right(address.asInstanceOf[P2PKHAddress])
        client
          .addMultiSigAddress(2, Vector(pubkey, p2pkh))
      }
    } yield succeed
  }

}
