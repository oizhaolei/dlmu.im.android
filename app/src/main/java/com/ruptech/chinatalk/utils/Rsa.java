/*
 * Copyright (C) 2010 The MobileSecurePay Project
 * All right reserved.
 * author: shiqun.shi@alipay.com
 */

package com.ruptech.chinatalk.utils;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import android.util.Log;

import com.ruptech.chinatalk.BuildConfig;

public class Rsa {
	static class Base64 {

		static private final int BASELENGTH = 128;
		static private final int LOOKUPLENGTH = 64;
		static private final int TWENTYFOURBITGROUP = 24;
		static private final int EIGHTBIT = 8;
		static private final int SIXTEENBIT = 16;
		static private final int FOURBYTE = 4;
		static private final int SIGN = -128;
		static private final char PAD = '=';
		static private final boolean fDebug = false;
		static final private byte[] base64Alphabet = new byte[BASELENGTH];
		static final private char[] lookUpBase64Alphabet = new char[LOOKUPLENGTH];

		static {
			for (int i = 0; i < BASELENGTH; ++i) {
				base64Alphabet[i] = -1;
			}
			for (int i = 'Z'; i >= 'A'; i--) {
				base64Alphabet[i] = (byte) (i - 'A');
			}
			for (int i = 'z'; i >= 'a'; i--) {
				base64Alphabet[i] = (byte) (i - 'a' + 26);
			}

			for (int i = '9'; i >= '0'; i--) {
				base64Alphabet[i] = (byte) (i - '0' + 52);
			}

			base64Alphabet['+'] = 62;
			base64Alphabet['/'] = 63;

			for (int i = 0; i <= 25; i++) {
				lookUpBase64Alphabet[i] = (char) ('A' + i);
			}

			for (int i = 26, j = 0; i <= 51; i++, j++) {
				lookUpBase64Alphabet[i] = (char) ('a' + j);
			}

			for (int i = 52, j = 0; i <= 61; i++, j++) {
				lookUpBase64Alphabet[i] = (char) ('0' + j);
			}
			lookUpBase64Alphabet[62] = '+';
			lookUpBase64Alphabet[63] = '/';

		}

		/**
		 * Decodes Base64 data into octects
		 *
		 * @param encoded
		 *            string containing Base64 data
		 * @return Array containind decoded data.
		 */
		public static byte[] decode(String encoded) {

			if (encoded == null) {
				return null;
			}

			char[] base64Data = encoded.toCharArray();
			// remove white spaces
			int len = removeWhiteSpace(base64Data);

			if (len % FOURBYTE != 0) {
				return null;// should be divisible by four
			}

			int numberQuadruple = (len / FOURBYTE);

			if (numberQuadruple == 0) {
				return new byte[0];
			}

			byte decodedData[] = null;
			byte b1 = 0, b2 = 0, b3 = 0, b4 = 0;
			char d1 = 0, d2 = 0, d3 = 0, d4 = 0;

			int i = 0;
			int encodedIndex = 0;
			int dataIndex = 0;
			decodedData = new byte[(numberQuadruple) * 3];

			for (; i < numberQuadruple - 1; i++) {

				if (!isData((d1 = base64Data[dataIndex++])) || !isData((d2 = base64Data[dataIndex++]))
						|| !isData((d3 = base64Data[dataIndex++])) || !isData((d4 = base64Data[dataIndex++]))) {
					return null;
				}// if found "no data" just return null

				b1 = base64Alphabet[d1];
				b2 = base64Alphabet[d2];
				b3 = base64Alphabet[d3];
				b4 = base64Alphabet[d4];

				decodedData[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
				decodedData[encodedIndex++] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
				decodedData[encodedIndex++] = (byte) (b3 << 6 | b4);
			}

			if (!isData((d1 = base64Data[dataIndex++])) || !isData((d2 = base64Data[dataIndex++]))) {
				return null;// if found "no data" just return null
			}

			b1 = base64Alphabet[d1];
			b2 = base64Alphabet[d2];

			d3 = base64Data[dataIndex++];
			d4 = base64Data[dataIndex++];
			if (!isData((d3)) || !isData((d4))) {// Check if they are PAD
													// characters
				if (isPad(d3) && isPad(d4)) {
					if ((b2 & 0xf) != 0)// last 4 bits should be zero
					{
						return null;
					}
					byte[] tmp = new byte[i * 3 + 1];
					System.arraycopy(decodedData, 0, tmp, 0, i * 3);
					tmp[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
					return tmp;
				} else if (!isPad(d3) && isPad(d4)) {
					b3 = base64Alphabet[d3];
					if ((b3 & 0x3) != 0)// last 2 bits should be zero
					{
						return null;
					}
					byte[] tmp = new byte[i * 3 + 2];
					System.arraycopy(decodedData, 0, tmp, 0, i * 3);
					tmp[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
					tmp[encodedIndex] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
					return tmp;
				} else {
					return null;
				}
			} else { // No PAD e.g 3cQl
				b3 = base64Alphabet[d3];
				b4 = base64Alphabet[d4];
				decodedData[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
				decodedData[encodedIndex++] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
				decodedData[encodedIndex++] = (byte) (b3 << 6 | b4);

			}

			return decodedData;
		}

		/**
		 * Encodes hex octects into Base64
		 *
		 * @param binaryData
		 *            Array containing binaryData
		 * @return Encoded Base64 array
		 */
		public static String encode(byte[] binaryData) {

			if (binaryData == null) {
				return null;
			}

			int lengthDataBits = binaryData.length * EIGHTBIT;
			if (lengthDataBits == 0) {
				return "";
			}

			int fewerThan24bits = lengthDataBits % TWENTYFOURBITGROUP;
			int numberTriplets = lengthDataBits / TWENTYFOURBITGROUP;
			int numberQuartet = fewerThan24bits != 0 ? numberTriplets + 1 : numberTriplets;
			char encodedData[] = null;

			encodedData = new char[numberQuartet * 4];

			byte k = 0, l = 0, b1 = 0, b2 = 0, b3 = 0;

			int encodedIndex = 0;
			int dataIndex = 0;
			if (fDebug) {
				System.out.println("number of triplets = " + numberTriplets);
			}

			for (int i = 0; i < numberTriplets; i++) {
				b1 = binaryData[dataIndex++];
				b2 = binaryData[dataIndex++];
				b3 = binaryData[dataIndex++];

				if (fDebug) {
					System.out.println("b1= " + b1 + ", b2= " + b2 + ", b3= " + b3);
				}

				l = (byte) (b2 & 0x0f);
				k = (byte) (b1 & 0x03);

				byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) ((b1) >> 2 ^ 0xc0);
				byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4) : (byte) ((b2) >> 4 ^ 0xf0);
				byte val3 = ((b3 & SIGN) == 0) ? (byte) (b3 >> 6) : (byte) ((b3) >> 6 ^ 0xfc);

				if (fDebug) {
					System.out.println("val2 = " + val2);
					System.out.println("k4   = " + (k << 4));
					System.out.println("vak  = " + (val2 | (k << 4)));
				}

				encodedData[encodedIndex++] = lookUpBase64Alphabet[val1];
				encodedData[encodedIndex++] = lookUpBase64Alphabet[val2 | (k << 4)];
				encodedData[encodedIndex++] = lookUpBase64Alphabet[(l << 2) | val3];
				encodedData[encodedIndex++] = lookUpBase64Alphabet[b3 & 0x3f];
			}

			// form integral number of 6-bit groups
			if (fewerThan24bits == EIGHTBIT) {
				b1 = binaryData[dataIndex];
				k = (byte) (b1 & 0x03);
				if (fDebug) {
					System.out.println("b1=" + b1);
					System.out.println("b1<<2 = " + (b1 >> 2));
				}
				byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) ((b1) >> 2 ^ 0xc0);
				encodedData[encodedIndex++] = lookUpBase64Alphabet[val1];
				encodedData[encodedIndex++] = lookUpBase64Alphabet[k << 4];
				encodedData[encodedIndex++] = PAD;
				encodedData[encodedIndex++] = PAD;
			} else if (fewerThan24bits == SIXTEENBIT) {
				b1 = binaryData[dataIndex];
				b2 = binaryData[dataIndex + 1];
				l = (byte) (b2 & 0x0f);
				k = (byte) (b1 & 0x03);

				byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) ((b1) >> 2 ^ 0xc0);
				byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4) : (byte) ((b2) >> 4 ^ 0xf0);

				encodedData[encodedIndex++] = lookUpBase64Alphabet[val1];
				encodedData[encodedIndex++] = lookUpBase64Alphabet[val2 | (k << 4)];
				encodedData[encodedIndex++] = lookUpBase64Alphabet[l << 2];
				encodedData[encodedIndex++] = PAD;
			}

			return new String(encodedData);
		}

		private static boolean isData(char octect) {
			return (octect < BASELENGTH && base64Alphabet[octect] != -1);
		}

		private static boolean isPad(char octect) {
			return (octect == PAD);
		}

		private static boolean isWhiteSpace(char octect) {
			return (octect == 0x20 || octect == 0xd || octect == 0xa || octect == 0x9);
		}

		/**
		 * remove WhiteSpace from MIME containing encoded Base64 data.
		 *
		 * @param data
		 *            the byte array of base64 data (with WS)
		 * @return the new length
		 */
		private static int removeWhiteSpace(char[] data) {
			if (data == null) {
				return 0;
			}

			// count characters that's not whitespace
			int newSize = 0;
			int len = data.length;
			for (int i = 0; i < len; i++) {
				if (!isWhiteSpace(data[i])) {
					data[newSize++] = data[i];
				}
			}
			return newSize;
		}
	}

	private static final String ALGORITHM = "RSA";

	public static final String SIGN_ALGORITHMS = "SHA1WithRSA";

	public static boolean doCheck(String content, String sign, String publicKey) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			byte[] encodedKey = Base64.decode(publicKey);
			PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

			java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);

			signature.initVerify(pubKey);
			signature.update(content.getBytes("utf-8"));
			if (BuildConfig.DEBUG)
				Log.i("Result", "content :   " + content);
			if (BuildConfig.DEBUG)
				Log.i("Result", "sign:   " + sign);
			boolean bverify = signature.verify(Base64.decode(sign));
			if (BuildConfig.DEBUG)
				Log.i("Result", "bverify = " + bverify);
			return bverify;
		} catch (Exception e) {
			Utils.sendClientException(e);
		}

		return false;
	}

	public static String encrypt(String content, String key) throws Exception {
		PublicKey pubkey = getPublicKeyFromX509(ALGORITHM, key);

		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, pubkey);

		byte plaintext[] = content.getBytes("UTF-8");
		byte[] output = cipher.doFinal(plaintext);

		String s = new String(Base64.encode(output));

		return s;

	}

	public static String getMD5(String content) {
		String s = null;
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			md.update(content.getBytes());
			byte tmp[] = md.digest();
			char str[] = new char[16 * 2];
			int k = 0;
			for (int i = 0; i < 16; i++) {
				byte byte0 = tmp[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			s = new String(str);

		} catch (Exception e) {
			Utils.sendClientException(e);
		}
		return s;
	}

	/**
	 * @param algorithm
	 * @param ins
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws AlipayException
	 */
	private static PublicKey getPublicKeyFromX509(String algorithm, String bysKey) throws Exception {
		byte[] decodedKey = Base64.decode(bysKey);
		X509EncodedKeySpec x509 = new X509EncodedKeySpec(decodedKey);

		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		return keyFactory.generatePublic(x509);
	}

	public static String sign(String content, String privateKey) throws Exception {
		String charset = "UTF-8";
		PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decode(privateKey));
		KeyFactory keyf = KeyFactory.getInstance("RSA", "BC");
		PrivateKey priKey = keyf.generatePrivate(priPKCS8);

		java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);

		signature.initSign(priKey);
		signature.update(content.getBytes(charset));

		byte[] signed = signature.sign();

		return Base64.encode(signed);
	}
}
