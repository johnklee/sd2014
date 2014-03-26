package unitest;

import java.util.HashMap;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetchResult;
import edu.uci.ics.crawler4j.url.WebURL;

public class TestCrawler extends WebCrawler{
	public static HashMap<String,Page> 				PageMap = new HashMap<String,Page>();
	public static HashMap<String,PageFetchResult> 	FailMap = new HashMap<String,PageFetchResult>();
	
	@Override
    public boolean shouldVisit(WebURL url) {return true;}
	
	@Override
	public void visit(Page page) 
	{
		synchronized(TestCrawler.class)
		{
			PageMap.put(page.getWebURL().getURL(), page);
		}
	}
	
	@Override
	public void fail(WebURL curURL, PageFetchResult fetchResult)
	{
		synchronized(TestCrawler.class)
		{
			FailMap.put(curURL.getURL(), fetchResult);
		}
	}
		
	public static void Clear()
	{
		PageMap.clear();
	}
}
