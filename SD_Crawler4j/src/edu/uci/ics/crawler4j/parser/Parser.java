/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.tika.metadata.DublinCore;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;

import edu.uci.ics.crawler4j.crawler.Configurable;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.util.Util;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */
public class Parser extends Configurable {
	protected static final Logger logger = Logger.getLogger(Parser.class.getName());
	private HtmlParser htmlParser;
	private ParseContext parseContext;
	private Pattern scriptSrc = Pattern.compile("<script src=(?:\"|')(.*?)(?:\"|')>");

	public Parser(CrawlConfig config) 
	{
		super(config);
		htmlParser = new HtmlParser();
		parseContext = new ParseContext();
	}

	public boolean parse(Page page, String contextURL) {
		logger.info(String.format("ContentType=%s", page.getContentType()));
		if (page.getContentType()==null || 
			Util.hasBinaryContent(page.getContentType())) 
		{
			if (!config.isIncludeBinaryContentInCrawling()) 
			{
				return false;
			}

			page.setParseData(BinaryParseData.getInstance());
			return true;

		} 
		else if (Util.hasPlainTextContent(page.getContentType())) 
		{
			try {
				TextParseData parseData = new TextParseData();
				if (page.getContentCharset() == null) {
					parseData.setTextContent(new String(page.getContentData()));
				} else {
					parseData.setTextContent(new String(page.getContentData(), page.getContentCharset()));
				}
				page.setParseData(parseData);
				return true;
			} catch (Exception e) {
				logger.error(e.getMessage() + ", while parsing: " + page.getWebURL().getURL());
			}
			return false;
		}

		Metadata metadata = new Metadata();
		HtmlContentHandler contentHandler = new HtmlContentHandler();
		InputStream inputStream = null;
		try 
		{
			inputStream = new ByteArrayInputStream(page.getContentData());
			htmlParser.parse(inputStream, contentHandler, metadata, parseContext);
		} 
		catch (Exception e) 
		{
			logger.error(e.getMessage() + ", while parsing: " + page.getWebURL().getURL());
		} 
		finally 
		{
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				logger.error(e.getMessage() + ", while parsing: " + page.getWebURL().getURL());
			}
		}

		if (page.getContentCharset() == null) 
		{
			page.setContentCharset(metadata.get("Content-Encoding"));
		}

		HtmlParseData parseData = new HtmlParseData();
		parseData.setText(contentHandler.getBodyText().trim());
		parseData.setTitle(metadata.get(DublinCore.TITLE));

		List<WebURL> outgoingUrls = new ArrayList<WebURL>();

		String baseURL = contentHandler.getBaseUrl();
		if (baseURL != null) {
			contextURL = baseURL;
		}

		int urlCount = 0;
		for (ExtractedUrlAnchorPair urlAnchorPair : contentHandler.getOutgoingUrls()) 
		{			
			String href = urlAnchorPair.getHref();
			href = href.trim();
			if (href.length() == 0) 
			{
				continue;
			}			
			String hrefWithoutProtocol = href.toLowerCase();
			if (href.startsWith("http://")) 
			{
				hrefWithoutProtocol = href.substring(7);
			}
			if (!hrefWithoutProtocol.contains("javascript:") && !hrefWithoutProtocol.contains("mailto:")
					&& !hrefWithoutProtocol.contains("@")) {
				String url = URLCanonicalizer.getCanonicalURL(href, contextURL);
				if (url != null) {
					logger.info(String.format("Outgoing URL=%s...", url));
					WebURL webURL = new WebURL();
					webURL.setURL(url);
					webURL.setAnchor(urlAnchorPair.getAnchor());
					outgoingUrls.add(webURL);
					urlCount++;
					if (urlCount > config.getMaxOutgoingLinksToFollow()) {
						break;
					}
				}
			}
		}
				
		parseData.setOutgoingUrls(outgoingUrls);

		try {
			if (page.getContentCharset() == null) {
				parseData.setHtml(new String(page.getContentData()));
			} else {
				parseData.setHtml(new String(page.getContentData(), page.getContentCharset()));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
		
		String pageCnt = parseData.getHtml();
		Matcher mth = scriptSrc.matcher(pageCnt);
		while(mth.find())
		{
			String srcPath = mth.group(1);
			String url = null;
			if(!srcPath.startsWith("http")){
				url = URLCanonicalizer.getCanonicalURL(srcPath, contextURL);
			} 
			else url = srcPath;
			
			WebURL webURL = new WebURL();
			webURL.setURL(url);			
			outgoingUrls.add(webURL);
			urlCount++;
			if (urlCount > config.getMaxOutgoingLinksToFollow()) {
				break;
			}
			
			logger.info(String.format("Has js src file: %s...", url));
			pageCnt = pageCnt.substring(pageCnt.indexOf(srcPath)+srcPath.length()+1, pageCnt.length());
			mth = scriptSrc.matcher(pageCnt);
		}

		page.setParseData(parseData);
		return true;
	}
}
