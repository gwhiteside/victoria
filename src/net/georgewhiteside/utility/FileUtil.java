package net.georgewhiteside.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class FileUtil
{
	private FileUtil() {
	}
	
	public static String loadTextResource(String name) {
		InputStream is = FileUtil.class.getResourceAsStream(name);
		
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
}
