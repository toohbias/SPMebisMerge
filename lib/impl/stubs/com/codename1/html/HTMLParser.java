package com.codename1.html;


/**
 *  A class for parsing HTML.  This uses the platform native webview to parse the
 *  HTML, and then serialize it to XML.  It then uses the {@link XMLParser} class to 
 *  parse that well-formed XML into an XML document.
 *  
 *  @author shannah
 */
public class HTMLParser {

	public HTMLParser() {
	}

	/**
	 *  Parses string containing HTML.  This is an asynchronous call.  Does not block.
	 *  @param html The HTML to be parsed
	 *  @return Result object which can be used to obtain the result {@link Element}.
	 */
	public HTMLParser.Result parse(String html) {
	}

	/**
	 *  A promise class for the result of parsing HTML.
	 */
	public static class Result {


		public Result() {
		}
	}
}
