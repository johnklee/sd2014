package edu.uci.ics.crawler4j.url;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;


public class TLDList {
	public static String 	TldFilePath = "tld-names.txt";	
	private static TLDList 	instance = null;
	Logger  				logger = Logger.getLogger(TLDList.class);
	private Set<String> 	tldSet = new HashSet<>();	

	private TLDList() {
		try {
			// Change to search working folder
			//InputStream stream = this.getClass().getClassLoader().getResourceAsStream(tldNamesFileName);
			InputStream stream = new FileInputStream(new File(TldFilePath));						
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String line;
			while ((line = reader.readLine()) != null) 
			{
				line = line.trim();
				if (line.isEmpty() || line.startsWith("//")) {
					continue;
				}
				tldSet.add(line);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(String.format("Error: %s", e.toString()));
			System.exit(-1);
		}
	}

	public static TLDList getInstance() {
		if(instance==null)
		{
			synchronized(TLDList.class){
				instance = new TLDList();
			}
		}
		return instance;
	}

	public boolean contains(String str) {
		return tldSet.contains(str);
	}

}
