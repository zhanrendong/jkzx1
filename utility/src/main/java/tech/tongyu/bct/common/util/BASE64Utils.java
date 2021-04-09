package tech.tongyu.bct.common.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class BASE64Utils {

	private final static String CHARSET = "UTF-8";

	public static String encode(String txt) throws UnsupportedEncodingException {
		return new BASE64Encoder().encode(txt.getBytes(CHARSET));
	}

	public static String decode(String txt) throws IOException {
		byte[] decodeBuffer = new BASE64Decoder().decodeBuffer(txt);
		return new String(decodeBuffer, CHARSET);
	}

}
