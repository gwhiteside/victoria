package net.georgewhiteside.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class Util
{
	private Util() {
	}
	
	public static String loadTextResource(String name) {
		InputStream is = Util.class.getResourceAsStream(name);
		
		Writer writer = new StringWriter();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			char[] buffer = new char[1024];
			int bytesRead;
			while((bytesRead = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, bytesRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return writer.toString();
	}
	
	public static boolean isInteger(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	/**
	 * If you are not concerned with potential overflow problems this function will perform about 20-30 times faster than using Integer.parseInt().
	 * @param str
	 * @return
	 * @author Jonas Klemming
	 */
	public static boolean isIntegerFast(String str) {
	    if (str == null) {
	        return false;
	    }
	    int length = str.length();
	    if (length == 0) {
	        return false;
	    }
	    int i = 0;
	    if (str.charAt(0) == '-') {
	        if (length == 1) {
	            return false;
	        }
	        i = 1;
	    }
	    for (; i < length; i++) {
	        char c = str.charAt(i);
	        if (c < '0' || c > '9') {
	            return false;
	        }
	    }
	    return true;
	}
}
