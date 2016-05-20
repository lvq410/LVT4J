package com.lvt4j.basic;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加解密
 * 
 * @author LV
 *
 */
public class TCode {
	public static final class MD5 {
		public static String encode(String text) {
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("MD5");
				md.update(text.getBytes());
				return TDataConvert.byteS2HexStr(md.digest());
			} catch (NoSuchAlgorithmException e) {
				return null;
			}
		}

		public static String encode(File file) throws Exception {
			MessageDigest md = MessageDigest.getInstance("MD5");
			FileInputStream is = new FileInputStream(file);
			byte[] buff = new byte[1024];
			int len;
			while ((len = is.read(buff)) > 0)
				md.update(buff, 0, len);
			is.close();
			return TDataConvert.byteS2HexStr(md.digest());
		}

		public static String encode(byte[] bytes) {
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.update(bytes);
				return TDataConvert.byteS2HexStr(md.digest());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static final class SHA {
		public static String encode(String text) throws Exception {
			MessageDigest md = MessageDigest.getInstance("SHA");
			md.update(text.getBytes());
			return TDataConvert.byteS2HexStr(md.digest());
		}

		public static String encode(File file) throws Exception {
			MessageDigest md = MessageDigest.getInstance("SHA");
			FileInputStream is = new FileInputStream(file);
			byte[] buff = new byte[1024];
			int len;
			while ((len = is.read(buff)) > 0)
				md.update(buff, 0, len);
			is.close();
			return TDataConvert.byteS2HexStr(md.digest());
		}

		public static String encode(byte[] bytes) throws Exception {
			MessageDigest md = MessageDigest.getInstance("SHA");
			md.update(bytes);
			return TDataConvert.byteS2HexStr(md.digest());
		}
	}

	public static final class SHA1 {
		public static String encode(String text) throws Exception {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			md.update(text.getBytes());
			return TDataConvert.byteS2HexStr(md.digest());
		}

		public static String encode(File file) throws Exception {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			FileInputStream is = new FileInputStream(file);
			byte[] buff = new byte[1024];
			int len;
			while ((len = is.read(buff)) > 0)
				md.update(buff, 0, len);
			is.close();
			return TDataConvert.byteS2HexStr(md.digest());
		}

		public static String encode(byte[] bytes) throws Exception {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			md.update(bytes);
			return TDataConvert.byteS2HexStr(md.digest());
		}
	}

	public static final class AES {

		public static String encrypt4Android(String plainText, String password)
				throws Exception {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
			sr.setSeed(password.getBytes());
			kgen.init(128, sr);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			byte[] byteContent = plainText.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] result = cipher.doFinal(byteContent);
			return TDataConvert.byteS2HexStr(result);
		}

		public static String encrypt(String plainText, String password)
				throws Exception {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			sr.setSeed(password.getBytes());
			kgen.init(128, sr);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			byte[] byteContent = plainText.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] result = cipher.doFinal(byteContent);
			return TDataConvert.byteS2HexStr(result);
		}

		public static String decrypt4Android(String cipherText, String password)
				throws Exception {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			// not Android SecureRandom sr =
			// SecureRandom.getInstance("SHA1PRNG");
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
			sr.setSeed(password.getBytes());
			kgen.init(128, sr);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] result = cipher.doFinal(TDataConvert
					.hexStr2ByteS(cipherText));
			return new String(result);
		}

		public static String decrypt(String cipherText, String password)
				throws Exception {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			sr.setSeed(password.getBytes());
			kgen.init(128, sr);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] result = cipher.doFinal(TDataConvert
					.hexStr2ByteS(cipherText));
			return new String(result);
		}
	}

	public static void main(String[] args) throws Exception {
		String t = "123";
		String k = "1";
		String r = TCode.AES.encrypt(t, k);
		System.out.println(r);
		System.out.println(TCode.AES.decrypt(r, k));
	}
}