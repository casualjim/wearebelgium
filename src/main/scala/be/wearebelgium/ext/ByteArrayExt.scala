package be.wearebelgium.ext

import org.apache.commons.codec.binary.Hex

class ByteArrayExt(arr: Array[Byte]) {
  def hexEncode(toLowerCase: Boolean = true) = new String(Hex.encodeHex(arr, toLowerCase))
}
