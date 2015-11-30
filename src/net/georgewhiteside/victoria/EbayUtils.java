package net.georgewhiteside.victoria;

//350 characters per search; 99 characters per word ("word" defined as consecutive characters

public class EbayUtils {
	public static String prepareQuery(String input) {
		String[] terms = input.split("\\s+");
		
		boolean termsOkay = true;
		for(String term : terms) {
			if(term.length() > 99) {
				termsOkay = false;
				break;
			}
		}
		
		if(termsOkay && input.length() <= 350) {
			return input;
		}
		
		return "";
	}
}
