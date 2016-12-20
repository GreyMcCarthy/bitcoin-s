package org.bitcoins.core.crypto

import org.bitcoins.core.config.TestNet3
import org.bitcoins.core.number.Int32
import org.bitcoins.core.protocol.CompactSizeUInt
import org.bitcoins.core.protocol.script._
import org.bitcoins.core.protocol.transaction.{Transaction, TransactionInput}
import org.bitcoins.core.script.ScriptProgram
import org.bitcoins.core.script.constant.{ScriptConstant, ScriptToken}
import org.bitcoins.core.script.crypto._
import org.bitcoins.core.script.flag.{ScriptFlag, ScriptFlagUtil, ScriptVerifyDerSig}
import org.bitcoins.core.script.result.ScriptErrorWitnessPubKeyType
import org.bitcoins.core.util.{BitcoinSLogger, BitcoinSUtil, BitcoinScriptUtil}
import org.slf4j.LoggerFactory

import scala.annotation.tailrec

/**
 * Created by chris on 2/16/16.
 * Responsible for checking digital signatures on inputs against their respective
 * public keys
 */
trait TransactionSignatureChecker extends BitcoinSLogger {

  /**
    * Checks the signature of a scriptSig in the spending transaction against the
    * given scriptPubKey & explicitly given public key
    * This is useful for instances of non standard scriptSigs
    *
    * @param txSignatureComponent the relevant transaction information for signature checking
    * @param script the current script state inside the interpreter - this is needed in the case of OP_CODESEPARATORS
    * @param pubKey the public key the signature is being checked against
    * @param signature the signature which is being checked against the transaction & the public key
    * @param flags the script flags used to check validity of the signature
    * @return a boolean indicating if the signature is valid or not
    */
  def checkSignature(txSignatureComponent : TransactionSignatureComponent, script : Seq[ScriptToken],
                     pubKey: ECPublicKey, signature : ECDigitalSignature, flags : Seq[ScriptFlag]) : TransactionSignatureCheckerResult = {
    logger.info("Signature: " + signature)
    val pubKeyEncodedCorrectly = BitcoinScriptUtil.isValidPubKeyEncoding(pubKey,flags)
    if (ScriptFlagUtil.requiresStrictDerEncoding(flags) && !DERSignatureUtil.isValidSignatureEncoding(signature)) {
      logger.error("Signature was not stricly encoded der: " + signature.hex)
      SignatureValidationFailureNotStrictDerEncoding
    } else if (ScriptFlagUtil.requireLowSValue(flags) && !DERSignatureUtil.isLowS(signature)) {
      logger.error("Signature did not have a low s value")
      ScriptValidationFailureHighSValue
    } else if (ScriptFlagUtil.requireStrictEncoding(flags) && signature.bytes.nonEmpty &&
      !HashType.hashTypes.contains(HashType(signature.bytes.last))) {
      logger.error("signature: " + signature.bytes)
      logger.error("Hash type was not defined on the signature")
      ScriptValidationFailureHashType
    } else if (pubKeyEncodedCorrectly.isDefined) {
      val err = pubKeyEncodedCorrectly.get
      val result = if (err == ScriptErrorWitnessPubKeyType) ScriptValidationFailureWitnessPubKeyType else SignatureValidationFailurePubKeyEncoding
      logger.error("The public key given for signature checking was not encoded correctly, err: " + result)
      result
    } else {
      val sigsRemovedScript = BitcoinScriptUtil.calculateScriptForChecking(txSignatureComponent,signature,script)
      val hashTypeByte = if (signature.bytes.nonEmpty) signature.bytes.last else 0x00.toByte
      val hashType = HashType(Seq(0.toByte, 0.toByte, 0.toByte, hashTypeByte))

      val hashForSignature = txSignatureComponent match {
        case b : BaseTransactionSignatureComponent =>
          TransactionSignatureSerializer.hashForSignature(txSignatureComponent.transaction,
            txSignatureComponent.inputIndex,
            sigsRemovedScript, hashType)
        case w : WitnessV0TransactionSignatureComponent =>
          TransactionSignatureSerializer.hashForSignature(w.transaction,w.inputIndex,sigsRemovedScript, hashType, w.amount)
      }

      logger.info("Hash for signature: " + BitcoinSUtil.encodeHex(hashForSignature.bytes))
      val isValid = pubKey.verify(hashForSignature,signature)
      if (isValid) SignatureValidationSuccess else SignatureValidationFailureIncorrectSignatures
    }
  }

  /**
   * This is a helper function to check digital signatures against public keys
   * if the signature does not match this public key, check it against the next
   * public key in the sequence
   * @param txSignatureComponent the tx signature component that contains all relevant transaction information
   * @param script the script state this is needed in case there is an OP_CODESEPARATOR inside the script
   * @param sigs the signatures that are being checked for validity
   * @param pubKeys the public keys which are needed to verify that the signatures are correct
   * @param flags the script verify flags which are rules to verify the signatures
   * @return a boolean indicating if all of the signatures are valid against the given public keys
   */
  @tailrec
  final def multiSignatureEvaluator(txSignatureComponent : TransactionSignatureComponent, script : Seq[ScriptToken],
                     sigs : List[ECDigitalSignature], pubKeys : List[ECPublicKey], flags : Seq[ScriptFlag],
                     requiredSigs : Long) : TransactionSignatureCheckerResult = {
    logger.debug("Signatures inside of helper: " + sigs)
    logger.debug("Public keys inside of helper: " + pubKeys)
    if (sigs.size > pubKeys.size) {
      //this is how bitcoin core treats this. If there are ever any more
      //signatures than public keys remaining we immediately return
      //false https://github.com/bitcoin/bitcoin/blob/master/src/script/interpreter.cpp#L955-L959
      logger.warn("We have more sigs than we have public keys remaining")
      SignatureValidationFailureIncorrectSignatures
    }
    else if (requiredSigs > sigs.size) {
      //for the case when we do not have enough sigs left to check to meet the required signature threshold
      //https://github.com/bitcoin/bitcoin/blob/master/src/script/interpreter.cpp#L914-915
      logger.warn("We do not have enough sigs to meet the threshold of requireSigs in the multiSignatureScriptPubKey")
      SignatureValidationFailureSignatureCount
    }
    else if (sigs.nonEmpty && pubKeys.nonEmpty) {
      val sig = sigs.head
      val pubKey = pubKeys.head
      val result = checkSignature(txSignatureComponent,script,pubKey,sig,flags)
      result match {
        case SignatureValidationSuccess =>
          multiSignatureEvaluator(txSignatureComponent, script, sigs.tail,pubKeys.tail,flags, requiredSigs - 1)
        case SignatureValidationFailureIncorrectSignatures =>
          multiSignatureEvaluator(txSignatureComponent, script, sigs, pubKeys.tail,flags, requiredSigs)
        case x @ (SignatureValidationFailureNotStrictDerEncoding | SignatureValidationFailureSignatureCount |
                  SignatureValidationFailurePubKeyEncoding | ScriptValidationFailureHighSValue |
                  ScriptValidationFailureHashType | ScriptValidationFailureWitnessPubKeyType) =>
          x
      }
    } else if (sigs.isEmpty) {
      //means that we have checked all of the sigs against the public keys
      //validation succeeds
      SignatureValidationSuccess
    } else SignatureValidationFailureIncorrectSignatures
  }
}

object TransactionSignatureChecker extends TransactionSignatureChecker


