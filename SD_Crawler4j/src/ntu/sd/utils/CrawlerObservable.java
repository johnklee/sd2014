package ntu.sd.utils;

import java.util.Observable;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.fetcher.PageFetchResult;
import edu.uci.ics.crawler4j.url.WebURL;
import flib.util.Tuple;

public class CrawlerObservable extends Observable{
	/**
	 * Callback for event of crawling done.
	 * @param page: Crawling page content.
	 */
	public void pageDone(Page page)
	{
		setChanged();
	    notifyObservers(new Tuple(true, page));
	}
	
	/**
	 * Callback for event of crawling fail.
	 * @param url: Crawling URL
	 * @param pr: Crawling result.
	 */
	public void pageFail(WebURL url, PageFetchResult pr)
	{
		setChanged();
	    notifyObservers(new Tuple(false, url, pr));
	}
}
