package unitest;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;

import ntu.sd.utils.SiTree;
import ntu.sd.utils.SiTree.EIterWay;
import ntu.sd.utils.SiTree.Node;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import flib.util.TimeStr;

// Force Error Testing
public class FET {
	CrawlController 			controller;
	int 						numberOfCrawlers = 7;
	String 						crawlStorageFolder = "C:/tmp/crawler_tmp/";
	public static int			TC=0;

	static{
		CrawlController.CheckOthersWait=CrawlController.CleanUpWait=CrawlController.DConfirmWait=1;
		File log4j = new File("unitest_log4j.properties");
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
	public void fetSiTree03()
	{
		System.out.printf("========== FET SiTree Observer API ==========\n");
		long st = System.currentTimeMillis();
		controller.addSeed("http://localhost/FF/EchoServlet/a1.html");
		SiTree siTree = new SiTree();
        controller.addObserver(siTree);
		System.out.printf("\t[Info] Starting Crawler...\n");
		controller.start(TestCrawler.class, numberOfCrawlers);
		System.out.printf("\t[Info] Done! %s\n", TimeStr.ToStringFrom(st));
        controller.deleteObserver(siTree);
        File dumpDir = new File("test");
        siTree.root.page=null;// Force error
        assertTrue(siTree.outputTo(dumpDir)==-1);
	}
	
	@Test
	public void fetSiTree02()
	{
		System.out.printf("========== FET SiTree Observer API ==========\n");
		long st = System.currentTimeMillis();
		controller.addSeed("http://localhost/FF/crawlme/test/notexist");
		SiTree siTree = new SiTree();
        controller.addObserver(siTree);
		System.out.printf("\t[Info] Starting Crawler...\n");
		controller.start(TestCrawler.class, numberOfCrawlers);
		System.out.printf("\t[Info] Done! %s\n", TimeStr.ToStringFrom(st));
        controller.deleteObserver(siTree);        
	}
	
	@Test
	public void fetSiTree01()
	{
		System.out.printf("========== FET SiTree Iterate/Search API ==========\n");
		SiTree siTree = new SiTree();
		Iterator<Node> iter = null;
		try
		{
			iter = siTree.traverseBFS();
			iter.remove();	/*Force error*/
			assertTrue(false);
		}
		catch(Exception e){}
		try
		{
			iter = siTree.traverseDFS();
			iter.remove();	/*Force error*/
			assertTrue(false);
		}
		catch(Exception e){}
		siTree.iterWay = EIterWay.OTH;
		iter = siTree.iterator();
		assertTrue(iter!=null);
	}
	
	@Before  
    public void setUp() throws Exception {  
		// crawlStorageFolder is a folder where intermediate crawl data is stored.
        
        
        // numberOfCrawlers shows the number of concurrent threads that should
        // be initiated for crawling.
        

        CrawlConfig config = new CrawlConfig();        
        config.setCrawlStorageFolder(String.format("%s%d", crawlStorageFolder, TC++));
        
        /*
         * Be polite: Make sure that we don't send more than 1 request per
         * second (1000 milliseconds between requests).
         */
        config.setPolitenessDelay(0);
        
        /*
         * You can set the maximum number of pages to crawl. The default value
         * is -1 for unlimited number of pages
         */
        config.setMaxPagesToFetch(100);
        config.setMaxDownloadSize(1*CrawlConfig.MB);
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
    	//System.out.printf("\t[Test] Tear down...\n");    	
    	if(controller!=null)
    	{
    		controller.shutdown();
    		controller.deleteObservers();
    	}
    	controller = null;
    	TestCrawler.Clear();
    	//try{Thread.sleep(5000);}catch(Exception e){e.printStackTrace();}
    	//IO.deleteFolder(new File(crawlStorageFolder));
    } 
}
