package unitest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;
import java.util.Map.Entry;

import ntu.sd.utils.SiTree;
import ntu.sd.utils.SiTree.Node;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.fetcher.PageFetchResult;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.util.IO;
import flib.util.TimeStr;

// Functional Acceptance Simple Test (FAST)
public class FAST {
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
	public void testBasic03()
	{
		// Testing NonBlocking API
		System.out.printf("========== Testing NonBlocking API ==========\n");
		long st = System.currentTimeMillis();
		controller.getConfig().setMaxDownloadSize(10*CrawlConfig.MB);
		controller.addSeed("http://localhost/FF/crawlme/index.html");
		System.out.printf("\t[Info] Starting Crawler...\n");
        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */       
		controller.startNonBlocking(TestCrawler.class, numberOfCrawlers);
		while(!controller.isFinished())
		{
			try{Thread.sleep(1000);} catch(Exception e){e.printStackTrace();}
		}
        //controller.start(TestCrawler.class, numberOfCrawlers);
        System.out.printf("\t[Info] Done! %s\n", TimeStr.ToStringFrom(st));
        Iterator<Entry<String,Page>> iter = TestCrawler.PageMap.entrySet().iterator();        
        assertEquals(9, TestCrawler.PageMap.size());
        for(String p:TestCrawler.FailMap.keySet())
        {
        	System.out.printf("\tFail Page=%s...\n", p);
        }
        assertEquals(1, TestCrawler.FailMap.size());
        assertEquals(404, TestCrawler.FailMap.get("http://localhost/FF/crawlme/notexist.html").getStatusCode());
        while(iter.hasNext())
        {
        	Entry<String,Page> e = iter.next();
        	System.out.printf("\t[Info] URL=%s\n", e.getKey());
        	//e.getValue().get
        }
	}
	
	@Test
	public void testBasic02()
	{
		// Test CrawlConfig.maxDownloadSize setting (Default 1MB)
		System.out.printf("========== Testing CrawlConfig ==========\n");
		String url="http://localhost/FF/mp3/test.mp3";
		String url2="http://localhost/FF/mp3/test2.mp3";
		long st = System.currentTimeMillis();		
		controller.addSeed(url); // 3.9 MB
		System.out.printf("\t[Info] Starting Crawler...\n");
		controller.start(TestCrawler.class, 1);
        System.out.printf("\t[Info] Done! %s\n", TimeStr.ToStringFrom(st));     
        for(PageFetchResult p:TestCrawler.FailMap.values())
        {
        	System.out.printf("\tFail Page=%s...\n", p.getFetchedUrl());
        }
        assertEquals(0, TestCrawler.PageMap.size());
        assertEquals(1, TestCrawler.FailMap.size());
        PageFetchResult pfr = TestCrawler.FailMap.get(url);
        assertTrue(pfr!=null);
        assertEquals(1001, pfr.getStatusCode());
        TestCrawler.Clear();
        
        try
        {
        	controller.shutdown();
        	controller.init();
        }
        catch(Exception e){e.printStackTrace();assertTrue(false);}
        controller.getConfig().setMaxDownloadSize(10*CrawlConfig.MB);
        // "http://localhost/FF/crawlme/test/a.html"
        controller.addSeed(url2); // 3.9 MB
		System.out.printf("\t[Info] Starting Crawler again...\n");
		controller.start(TestCrawler.class, 1);
        System.out.printf("\t[Info] Done! %s\n", TimeStr.ToStringFrom(st));        
        assertEquals(1, TestCrawler.PageMap.size());
        assertEquals(0, TestCrawler.FailMap.size());
	}
	
	@Test
	public void testBasic01()
	{
		System.out.printf("========== Testing Blocking API ==========\n");
		long st = System.currentTimeMillis();
		controller.getConfig().setMaxDownloadSize(10*CrawlConfig.MB);
		controller.addSeed("http://localhost/FF/crawlme/index.html");
		System.out.printf("\t[Info] Starting Crawler...\n");
        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */       
        controller.start(TestCrawler.class, numberOfCrawlers);
        System.out.printf("\t[Info] Done! %s\n", TimeStr.ToStringFrom(st));
        Iterator<Entry<String,Page>> iter = TestCrawler.PageMap.entrySet().iterator();        
        assertEquals(9, TestCrawler.PageMap.size());
        for(String p:TestCrawler.FailMap.keySet())
        {
        	System.out.printf("\tFail Page=%s...\n", p);
        }
        assertEquals(1, TestCrawler.FailMap.size());
        assertEquals(404, TestCrawler.FailMap.get("http://localhost/FF/crawlme/notexist.html").getStatusCode());
        while(iter.hasNext())
        {
        	Entry<String,Page> e = iter.next();
        	System.out.printf("\t[Info] URL=%s\n", e.getKey());
        	//e.getValue().get
        }
	}
	
	@Test
	public void testSiTree02()
	{
		System.out.printf("========== Testing SiTree API ==========\n");
		long st = System.currentTimeMillis();
		controller.addSeed("http://localhost/FF/crawlme/test/main.html");
		SiTree siTree = new SiTree();
        controller.addObserver(siTree);
		System.out.printf("\t[Info] Starting Crawler...\n");      
		controller.startNonBlocking(TestCrawler.class, numberOfCrawlers);
		while(!controller.isFinished())
		{
			try{Thread.sleep(1000);} catch(Exception e){e.printStackTrace();}
		}
        //controller.start(TestCrawler.class, numberOfCrawlers);
        System.out.printf("\t[Info] Done! %s\n", TimeStr.ToStringFrom(st));
        controller.deleteObserver(siTree);
        
        assertEquals(10, siTree.nodeMap.size());
        // Retrieve main page
        Node node = siTree.nodeMap.get("http://localhost/FF/crawlme/test/main.html");
        assertTrue(node!=null);
        assertEquals(true, node.isValid);  		// Page is retrieved successfully
        assertEquals(2, node.childs.size());	// Page has two child pages
        
        // Retrieve child page
        Node aNode = node.childs.get("http://localhost/FF/crawlme/test/a.html");
        assertTrue(aNode!=null);
        assertEquals(true, aNode.isValid);
        assertEquals(4, aNode.childs.size());
        
        // Retrieve child page of child page
        Node n404 = aNode.childs.get("http://localhost/FF/SCServlet/404");
        assertTrue(n404!=null);
        assertEquals(false, n404.isValid);
        assertEquals(404, n404.statusCode);
        assertEquals(0, n404.childs.size());
        
        // Free resource
        siTree.close();
        assertEquals(0, siTree.nodeMap.size());
        assertEquals(null, siTree.root);
	}
	
	@Test
	public void testSiTree01()
	{
		System.out.printf("========== Testing SiTree API ==========\n");
		long st = System.currentTimeMillis();
		controller.addSeed("http://localhost/FF/crawlme/test/main.html");
		SiTree siTree = new SiTree();
        controller.addObserver(siTree);
		System.out.printf("\t[Info] Starting Crawler...\n");
		controller.start(TestCrawler.class, numberOfCrawlers);
		System.out.printf("\t[Info] Done! %s\n", TimeStr.ToStringFrom(st));
        controller.deleteObserver(siTree);
        
        assertEquals(10, siTree.nodeMap.size());
        // Retrieve main page
        Node node = siTree.nodeMap.get("http://localhost/FF/crawlme/test/main.html");
        assertTrue(node!=null);
        assertEquals(true, node.isValid);  		// Page is retrieved successfully
        assertEquals(2, node.childs.size());	// Page has two child pages
        
        // Retrieve child page
        Node aNode = node.childs.get("http://localhost/FF/crawlme/test/a.html");
        assertTrue(aNode!=null);
        assertEquals(true, aNode.isValid);
        assertEquals(4, aNode.childs.size());
        
        // Retrieve child page of child page
        Node n404 = aNode.childs.get("http://localhost/FF/SCServlet/404");
        assertTrue(n404!=null);
        assertEquals(false, n404.isValid);
        assertEquals(404, n404.statusCode);
        assertEquals(0, n404.childs.size());
        
        // Free resource
        siTree.close();
        assertEquals(0, siTree.nodeMap.size());
        assertEquals(null, siTree.root);
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
        config.setPolitenessDelay(100);
        
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
