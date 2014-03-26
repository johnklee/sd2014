package unitest;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import flib.util.TimeStr;

// Functional Acceptance Simple Test (FAST)
public class FAST {
	CrawlController 	controller;
	int 				numberOfCrawlers = 7;
	
	static{
		CrawlController.CheckOthersWait=CrawlController.CleanUpWait=CrawlController.DConfirmWait=1;
		File log4j = new File("log4j.properties");
		if(log4j.exists())
		{
			PropertyConfigurator.configure(log4j.getAbsolutePath()); 
		}
		else
		{
			BasicConfigurator.configure();  
		}
	}
	
	@Test
	public void testBasic01()
	{
		long st = System.currentTimeMillis();
		controller.addSeed("http://localhost/FF/crawlme/index.html");
		System.out.printf("\t[Info] Starting Crawler...\n");
        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */       
        controller.start(TestCrawler.class, numberOfCrawlers);
        System.out.printf("\t[Info] Done! %s\n", TimeStr.ToStringFrom(st));
        Iterator<Entry<String,Page>> iter = TestCrawler.PageMap.entrySet().iterator();        
        assertEquals(5, TestCrawler.PageMap.size());
        assertEquals(1, TestCrawler.FailMap.size());
        assertEquals(404, TestCrawler.FailMap.get("http://localhost/FF/crawlme/notexist.html").getStatusCode());
        while(iter.hasNext())
        {
        	Entry<String,Page> e = iter.next();
        	System.out.printf("\t[Info] URL=%s\n", e.getKey());
        	//e.getValue().get
        }
	}
	
	@Before  
    public void setUp() throws Exception {  
		// crawlStorageFolder is a folder where intermediate crawl data is stored.
        String crawlStorageFolder = "C:/tmp/crawler_tmp/";
        
        // numberOfCrawlers shows the number of concurrent threads that should
        // be initiated for crawling.
        

        CrawlConfig config = new CrawlConfig();        
        config.setCrawlStorageFolder(crawlStorageFolder);
        
        /*
         * Be polite: Make sure that we don't send more than 1 request per
         * second (1000 milliseconds between requests).
         */
        config.setPolitenessDelay(1000);
        
        /*
         * You can set the maximum number of pages to crawl. The default value
         * is -1 for unlimited number of pages
         */
        config.setMaxPagesToFetch(1000);
        
        /*
         * Do you need to set a proxy? If so, you can use:
         * config.setProxyHost("proxyserver.example.com");
         * config.setProxyPort(8080);
         * 
         * If your proxy also needs authentication:
         * config.setProxyUsername(username); config.getProxyPassword(password);
         */
        
        /*
         * This config parameter can be used to set your crawl to be resumable
         * (meaning that you can resume the crawl from a previously
         * interrupted/crashed crawl). Note: if you enable resuming feature and
         * want to start a fresh crawl, you need to delete the contents of
         * rootFolder manually.
         */
        config.setResumableCrawling(false);
        config.setIncludeBinaryContentInCrawling(true);
        
        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        controller = new CrawlController(config, pageFetcher, robotstxtServer);
    }  
  
    @After  
    public void tearDown() {  
    	//Tear down
    	if(controller!=null)
    	{
    		controller.shutdown();
    	}
    	controller = null;
    	TestCrawler.Clear();
    } 
}
