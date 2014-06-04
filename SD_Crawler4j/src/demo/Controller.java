package demo;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import flib.util.TimeStr;

public class Controller {	
	
	/**
	 * BD: You should also implement a controller class which specifies the seeds 
	 *     of the crawl, the folder in which intermediate crawl data should be stored 
	 *     and number of concurrent threads.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		/* 0) Configure Log4j*/
		File log4j = new File("log4j.properties");
		if(log4j.exists())
		{
			PropertyConfigurator.configure(log4j.getAbsolutePath()); 
		}
		else
		{
			BasicConfigurator.configure();  
		}
		
		long st = System.currentTimeMillis();

		// crawlStorageFolder is a folder where intermediate crawl data is stored.
        String crawlStorageFolder = "C:/tmp/crawler_tmp/";
        
        // numberOfCrawlers shows the number of concurrent threads that should
        // be initiated for crawling.
        int numberOfCrawlers = 7;

        CrawlConfig config = new CrawlConfig();        
        config.setCrawlStorageFolder(crawlStorageFolder);
        
        /*
         * Be polite: Make sure that we don't send more than 1 request per
         * second (1000 milliseconds between requests).
         */
        config.setPolitenessDelay(100);
        
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
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        controller.addSeed("http://localhost/FF/crawlme/index.html");
        //controller.addSeed("http://localhost/FF/redir/documentloc.html");

        
        System.out.printf("\t[Info] Starting Crawler...\n");
        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */       
        controller.start(MyCrawler.class, numberOfCrawlers);
        System.out.printf("\t[Info] Done! %s\n", TimeStr.ToStringFrom(st));
        
	}
}
