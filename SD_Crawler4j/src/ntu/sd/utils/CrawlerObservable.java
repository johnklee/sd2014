package ntu.sd.utils;

import java.util.Observable;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.fetcher.PageFetchResult;
import edu.uci.ics.crawler4j.url.WebURL;
import flib.util.Tuple;

public class CrawlerObservable extends Observable{
	public void pageDone(Page page)
	{
		setChanged();
	    notifyObservers(new Tuple(true, page));
	}
	
	public void pageFail(WebURL url, PageFetchResult pr)
	{
		setChanged();
	    notifyObservers(new Tuple(false, url, pr));
	}
}
