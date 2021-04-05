package com.newsapi.fetchsources;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.TreeSet;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class FetchSourcesApplication {

	static void SaveAndLoadDisctint() {
		try {
			Scanner sc=new Scanner(new File("src.txt"));
			TreeSet<String> list=new TreeSet<>();
			while(sc.hasNextLine()) {
				list.add(sc.nextLine());
			}
			sc.close();
			System.out.println(list.size()); 
			
			for(String url:list) {
				try {
					FileWriter fw = new FileWriter(new File("src2.txt"), true);
					fw.append("\n" + url);
					fw.close();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
		
	}
	
	public static void main(String[] args) {

		//SaveAndLoadDisctint();
		//if(true)return;
		String baseURL = "https://newsapi.org/v2/";
		String everythingEndpoint = "everything?";
		String sourcesEndpoint = "sources?";
		KeysCall arrKey[]=getKeysSorted();
		
		Stack<String> keywords=LoadKeywords();
		System.out.println(keywords.size());
	//	if(true)return;
		
		//params
		int pageCount=1;
		TreeSet<String> sourceSet=LoadSource();
		String excludeDomains=getDomainsAsCommaSeparated(sourceSet);
		
		TreeSet<String> currentExtractedDomainsList=new TreeSet<>();
		
		System.out.println(excludeDomains);
		
		String addCountry="language=en";
		
		
// <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN""http://www.w3.org/TR/html4/strict.dtd"><HTML><HEAD><TITLE>Request URL Too Long</TITLE><META HTTP-EQUIV="Content-Type" Content="text/html; charset=us-ascii"></HEAD><BODY><h2>Request URL Too Long</h2><hr><p>HTTP Error 414. The request URL is too long.</p></BODY></HTML>
		
		
		String keyword=keywords.pop();
		
		while(true) {
			
		try {
			System.out.println(excludeDomains.split(",").length);
			
		//	int breaker=0;if(true)return;
			
				//breaker++;if(breaker==10)break;
				try {
					
					String urlString=baseURL+everythingEndpoint+"apiKey="+arrKey[0].key;
					String pageParam="page=";
					
					pageParam+=pageCount;
					urlString+="&"+pageParam;
					
					String qParam="q=";qParam+=keyword;
					//String qParam="q=";qParam+=toBeSearched;
					urlString+="&"+qParam;
					
					String excludeDomainsParam="excludeDomains="+excludeDomains;
					urlString+="&"+excludeDomainsParam;
					
					System.out.println("url is "+urlString);
					System.out.println(urlString.length());
					String res = getResponse(urlString);
					System.out.println(res);
					
					
					
					JSONObject jsonObject = new JSONObject(res);
					
					
					if(jsonObject.getString("status").equals("error")) {
						String code=jsonObject.getString("code");
						System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx   "+code);
						if(code.equals("rateLimited")) {
							arrKey[0].noOfCalls=100000;
							Arrays.sort(arrKey,new Comparator<KeysCall>() {

								@Override
								public int compare(KeysCall o1, KeysCall o2) {
									if(o1.noOfCalls>o2.noOfCalls)return 1;
									else if(o1.noOfCalls<o2.noOfCalls)return -1;
									return 0;
								}
							});
						}
						else if(code.equals("parametersMissing")) {
							
						}
						else if(code.equals("maximumResultsReached")) {
							//System.out.println("debug "+currentExtractedDomainsList);if(true)break;
							
							//set page to 1
							//add a new domain to be excluded
							if(currentExtractedDomainsList.size()==0) {
								pageCount=1;
								keyword=keywords.pop();
								continue;
							}
							pageCount=1;
							keyword=keywords.pop();
							
							String currDomains=getDomainsAsCommaSeparatedFromCurrent(currentExtractedDomainsList);
							System.out.println("new domains as comma separated "+currDomains);
							excludeDomains+=","+currDomains;
							System.out.println("excuded new domains "+excludeDomains);
							currentExtractedDomainsList=new TreeSet<>();
						}
						
						continue;
					}
					
					JSONArray jsonArray = jsonObject.getJSONArray("articles");

					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject2 = jsonArray.getJSONObject(i);
						String cleaned=getCleanedURL(jsonObject2.getString("url"));
						System.out.println(cleaned);
						if(sourceSet.add(cleaned)) {
							SaveSource(cleaned);
							currentExtractedDomainsList.add(cleaned);
						}
						
						
					}
					System.out.println();
					arrKey[0].noOfCalls++;
					pageCount++;
					
					//testing
					System.out.println(currentExtractedDomainsList);
					if(currentExtractedDomainsList.size()==0) {
						keyword=keywords.pop();
						pageCount=1;
					}
					//break;
					
					
				} catch (Exception e0) {
					e0.printStackTrace();
					keyword=keywords.pop();
					excludeDomains=excludeDomains.substring(0,excludeDomains.length()/2);
					if(excludeDomains.charAt(excludeDomains.length())==',')
						excludeDomains=excludeDomains.substring(0,excludeDomains.length()-1);
				}
				
				
			
			
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println("otside while");
			excludeDomains=excludeDomains.substring(0,excludeDomains.length()/2);
			if(excludeDomains.charAt(excludeDomains.length()-1)==',')
				excludeDomains=excludeDomains.substring(0,excludeDomains.length()-1);
			
		}
		
		
	}
		// SpringApplication.run(FetchSourcesApplication.class, args);
	}

	//static void
	
	
	//https://newsapi.org/v2/sources?apiKey=ec684edea47d4a0d96c4e9338151ca0f
	static String getResponse(String url) throws ClientProtocolException, IOException, JSONException {
		 CloseableHttpClient httpclient = HttpClients.createDefault();

	      //Creating a HttpGet object
	      HttpGet httpget = new HttpGet(url);

	      HttpResponse httpresponse = httpclient.execute(httpget);
	      
	     // System.out.println(httpresponse.getEntity().);
	      
	      Scanner sc = new Scanner(httpresponse.getEntity().getContent());

	      //Printing the status line
	      System.out.println(httpresponse.getStatusLine());
	      String string="";
	      while(sc.hasNext()) {
	         string+=sc.nextLine();
	      }
	      return string;
	      
	}

	static TreeSet<String> LoadSource() {
		try {
			Scanner sc=new Scanner(new File("src.txt"));
			TreeSet<String> list=new TreeSet<>();
			while(sc.hasNextLine()) {
				list.add(sc.nextLine());
			}
			sc.close();
			System.out.println(list.size()); 
			return list;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	static Stack<String> LoadKeywords() {
		try {
			Scanner sc=new Scanner(new File("keywords2.txt"));
			Stack<String> stack=new Stack<>();
			while(sc.hasNextLine()) {
				stack.add(sc.nextLine());
			}
			sc.close();
			return stack;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	static void SaveSource(String url) {

		try {
			FileWriter fw = new FileWriter(new File("src.txt"), true);
			fw.append("\n" + url);
			fw.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	static String getCleanedURL(String url) {
		int index = url.indexOf("//");
		url = url.substring(index + 2, url.length());

		if (url.substring(0, 3).equals("www")) {
			url = url.substring(4, url.length());
		}

		try {
			int index2 = url.indexOf("/");
			url = url.substring(0, index2);
		} catch (Exception e) {
			// TODO: handle exception
		}

		return url;
	}

	static class KeysCall {
		String key;
		int noOfCalls;public KeysCall(String key,int noOfCalls){
			this.key=key;this.noOfCalls=noOfCalls;
		}
	}
	
	static KeysCall[] getKeysSorted() {
		try {
			Scanner sc=new Scanner(new File("keys.txt"));
			TreeSet<String> list=new TreeSet<>();
			while(sc.hasNextLine()) {
				list.add(sc.nextLine());
			}
			sc.close();
			KeysCall arr[]=new KeysCall[list.size()];int i=0;
			for(String key:list) {
				arr[i]=new KeysCall(key,0);i++;
			}
			
			return arr;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	static void displayKeysStatus(KeysCall arr[]) {
		for(KeysCall key:arr) {
			System.out.println(key.key+" "+key.noOfCalls);
		}
	}
	
	static String getDomainsAsCommaSeparated(TreeSet<String> list) {
		String reString="";
		int counter=0;
		for(String s:list) {
			reString+=s+",";
			counter++;
			if(counter>=list.size()/2)break;
			
		}
		if(reString.length()==0)return "";	
		
		return reString.substring(0,reString.length()-1);
	}
	static String getDomainsAsCommaSeparatedFromCurrent(TreeSet<String> list) {
		String reString="";
		int counter=0;
		for(String s:list) {
			reString+=s+",";
			
		}
		if(reString.length()==0)return "";	
		
		return reString.substring(0,reString.length()-1);
	}
}
