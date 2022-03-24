# 1.9.1

This release is backwards compatible with the 1.9.0 release of bitcoin-s

See the individual module sections for more information on lower level updates to the codebase.

## Running Bitcoin-S

If you want to run the standalone server binary, after verifying gpg signatures, you
can `unzip bitcoin-s-server-1.9.1.zip` and then run it with `chmod +x ./bin/bitcoin-s-server && ./bin/bitcoin-s-server` to start the node. You will need to
configure the node properly first, you can find example
configurations [here](https://bitcoin-s.org/docs/config/configuration#example-configuration-file).

You can then unzip the `bitcoin-s-cli-1.9.1.zip` folder and start using the `bitcoin-s-cli` like this:

```bashrc
./bin/bitcoin-s-cli --help
Usage: bitcoin-s-cli [options] [<cmd>]

  -n, --network <value>    Select the active network.
  --debug                  Print debugging information
  --rpcport <value>        The port to send our rpc request to on the server
  -h, --help               Display this help message and exit
```

For more information on what commands `bitcoin-s-cli` supports check the documentation, here is where to
start: https://bitcoin-s.org/docs/next/applications/server#server-endpoints

## Verifying signatures

This release is signed with [Chris's signing key](https://bitcoin-s.org/docs/next/security#disclosure) with
fingerprint `339A49229576050819083EB3F99724872F822910`

To do the verification, first hash the executable using `sha256sum`. You should check that the result is listed in
the `SHA256SUMS.asc` file next to its file name. After doing that you can use `gpg --verify` to authenticate the
signature.

Example:

UPDATE ME!!!!!!!
```
$ sha256sum bitcoin-s-server-1.9.0.zip
aa1084edb5fcd3d1dbcafe0d0fba787abf4cd455bbe38809bd9a65a49c0cd0eb bitcoin-s-server-1.9.0.zip
$ gpg --verify SHA256SUMS.asc
gpg: Signature made Thu 24 Sep 2020 12:49:39 PM CDT
gpg:                using RSA key 339A49229576050819083EB3F99724872F822910
gpg:                issuer "stewart.chris1234@gmail.com"
gpg: Good signature from "Chris Stewart <stewart.chris1234@gmail.com>"
```

### Website

https://bitcoin-s.org/

### Releases

https://repo1.maven.org/maven2/org/bitcoin-s/

### Snapshot releases

https://oss.sonatype.org/content/repositories/snapshots/org/bitcoin-s/

# Executive Summary

This release integrates Tor network support for opening a DLC with your counterparty.
This is a huge UX improvement over the previous flow where 2 _manual_ round trips
were required to open a DLC with your peer.

See individual module sections for updates on per module basis.


## app commons


6e87eb1480 Offer.offerCollateralSatoshis to offerCollateral (#4148)
1bab51c1c6 Adjust appconfig logging to INFO (#4139)

## App server

73fe4099f9 Renable tor configuration by default (#4179)
668ab21ca1 2022 03 09 label refactor (#4175)
aeb3169884 `getdlcoffer` RPC (#4166)
56d0ae68ad `offer-send` RPC (#4153)
6b7058af4a Add the temporaryContractId field to the json rpc response for offers (#4146)
c315dce05b Add jdk version to log (#4142)
b86d4e492c Allow both lnmessage and raw tlv to be sent over rpc for acceptdlc (#4140)
44b2ca3c3d Improve validation error messages (#4141)

## bundle

bd7f35e9ed Set both bitcoin-s.proxy.enabled and bitcoin-s.tor.enabled in bitcoin-s-bundle.conf (#4194)

## Build

b8539bf68c Upgrade sbt to 1.6.2 (#4187)
23f359821f 2022 03 08 Publish zip as part of `release.yml` (#4174)
30226219e6 2022 03 01 static wix upgrade product code (#4149)
8d47f804d9 Update previous stable version to 1.9.0 (#4143)
54e18e6a8d Update docker image to jdk 17 (#4128)
96d11163a6 Bump heap size on ci-release (#4118)
16f3da7ee0 Update setup scala action to v13 (#4113)
e98b2c6caa Update release.yml to use openjdk@1.17.0 (#4115)
eddcc94b03 Upgrade CI jdks to openjdk@1.17.0 (#4114)

## Cli

## chain

## clightning rpc

## Core

c379cf4a73 Round Bitcoins to nearest Satoshi (#4154)
5e9be9d69e Fix hardened serialization (#4160)
c3300aec52 2022 03 03 hdpath fromstring factory exn (#4159)
d1fc32758b Fix tempContractId hash so we include LnMessage() bytes (#4150)
528e7c8f0e Remove TxoState.DoesNotExist (#4108)
f657510d80 Remove invoice max amount limit (#4104)

## Crypto

## Db commons

## DLC node

c1dccd7831 Offer inbox RPC (#4129)
5aa46be423 Add a warn log if tor is not enabled and we are booting up DLCServer (#4126)
f3c443804b Make DLCDataHandler more type safe (#4123)
34b7d18268 External payout addresses for acceptdlc (#4121)

## DLC Oracle

## DLC wallet

faac871db6 Make buildCreateOfferAction upsert funding inputs rather than insert (#4186)
3f18f7b04c Only allow executing a DLC if it is in the Broadcast or Confirmed state (#4185)
a747f84bbb Mark extarnal payout addresses (#4173)
37611ddea1 Move all initialization of accept into initDLCForAccept() (#4162)
0bb0d9acdb Add AdaptorSigComputation states to states that a DLC can be cancelled in (#4158)
ee98aa1d30 Increase offer message size (#4136)
ffd7d921a8 Database support for incoming offers (#4105)
5777ec1c31 Add an ability to set custom payout and change addresses (#4101)

## Eclair rpc

## Esplora

## fee provider

## gui

ec384a8b39 2022 03 05 rescan gui (#4170)
8f02ebce10 2022 02 26 dynamic fee gui (#4135)

## keymanager

8e3b8c1f4f Improve key manager error message (#4161)
5113cac6c2 Log root xpub on keymanager startup (#4157)

## Lnd rpc

5c9d64647e Add PSBT functions for LND (#4124)
cff0e84440 Fix get txs for lnd (#4111)

## node

b46574c0c4 2022 02 18 sync since creationtime pt2 (#4109)

## Oracle explorer client

## Oracle server

## secp256k1jni

## wallet

46229d712c Rename `clearAllUtxosAndAddresses()` -> `clearAllUtxos()` (#4193)
ef41cce32d Log entire address path when the address is generated (#4191)
8214c0b931 Add the account to the address generation log (#4190)
c882372c45 Improve process compact filter logs (#4189)
1072078d7c Improve tx processing log to show when we have a relevant input/output to the wallet (#4172)
d63e845de4 Only update wallet's sync height if we don't have a match in processCompactFilter() (#4169)
f1bd0ea3a5 Add log message for the case where we match a script but do not have a matching address in our database (#4156)
4cefa56c99 Add unit test for address creation when we are already watching the script (#4152)
374c1d7b9f Upgrade RescanHandlingTest to use the newest release of bitcoind (#4151)
7a5c2971dd Refactor receive flow to use the same utxo state transitions (#4134)
c069b01e53 2022 02 24 rm prune addresses after rescan (#4130)
3ba95700b2 Add RescanState, make sure we don't start concurrent rescans (#4131)
940c66807e Add log for when a new address is generated (#4127)
e6bf7bd67e Segregate updating received utxos and spent utxos (#4093)

## testkit-core

c11d9ef1fe Fix unit test where coinbase input was sometimes selected (#4165)

## tor

fe86233489 Add better tor message to UI (#4181)

## Website

7ddeae66d2 Add web UI build tutorial (#4183)
90ff59c6fe Add The Bitcoin Company to users list (#4177)

## Dependencies

666885bc11 Re-add javafx media,graphics dependnecies to fix GUI (#4107)
5b1b1ee149 Remove uneeded javafx deps (#4103)













