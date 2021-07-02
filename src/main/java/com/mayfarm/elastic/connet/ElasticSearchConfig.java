package com.mayfarm.elastic.connet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.xml.ws.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.lucene.search.TermQuery;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.search.MatchQueryParser;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.elasticsearch.client.RestHighLevelClient;


public class ElasticSearchConfig {
	
	public static RestHighLevelClient getRestHighLevelClient(List<String> hosts, String username, String password, String ssl) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException{
		
		List<HttpHost> hostList = new ArrayList<HttpHost>();
		
		final String regex = "^([.0-9a-z]+)(?:\\:(\\d+))?";
		String hostname = "", port = "";
		Pattern pattern;
		Matcher matcher;
		
		for(String host : hosts) {
			if(StringUtils.isEmpty(host))
				continue;
			host = host.trim();
			pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(host);
			if (matcher.find()) {
				hostname = matcher.group(1);
				if((hostname.toLowerCase()).equals("localhost")) {
					hostname = "127.0.0.1";
				}
				port = matcher.groupCount() == 2 ? matcher.group(2) : "9200";
			}
			if(!StringUtils.isBlank(ssl)) {
				hostList.add(new HttpHost(hostname, Integer.parseInt(port), "https"));
			}else {
				hostList.add(new HttpHost(hostname, Integer.parseInt(port), "http"));
			}
		}
		
		RestClientBuilder builde = null;
		if (!StringUtils.isBlank(username) && !StringUtils.isBlank(password) && StringUtils.isBlank(ssl)) {
			final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
			
			HttpClientConfigCallback httpClientConfigCallback = new HttpClientConfigCallback() {
				@Override
				public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
					return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
				}
			}
			;
			builde = RestClient.builder(hostList.toArray(new HttpHost[hostList.size()])).setHttpClientConfigCallback(httpClientConfigCallback);
		
		}else if (!StringUtils.isBlank(username) && !StringUtils.isBlank(password) && !StringUtils.isBlank(ssl)) {
			final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
			
			Path caCertificatePath = Paths.get(ssl);
			CertificateFactory factory = CertificateFactory.getInstance("X.509");
			Certificate trustedCa;
			
			try (InputStream is = Files.newInputStream(caCertificatePath)){
				trustedCa = factory.generateCertificate(is);
			}
			KeyStore trustStore = KeyStore.getInstance("pkcs12");
			trustStore.load(null, null);
			trustStore.setCertificateEntry("ca", trustedCa);
			
			SSLContextBuilder sslContextBuilder = SSLContexts.custom().loadTrustMaterial(trustStore, null);
			
			final SSLContext sslContext = sslContextBuilder.build();
			
			HttpClientConfigCallback httpClientConfigCallback = new HttpClientConfigCallback() {
				
				@Override
				public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
					return httpClientBuilder.setSSLContext(sslContext).setDefaultCredentialsProvider(credentialsProvider);
				}
			};
			builde = RestClient.builder(hostList.toArray(new HttpHost[hostList.size()])).setHttpClientConfigCallback(httpClientConfigCallback);
		
		}else {
			builde = RestClient.builder(hostList.toArray(new HttpHost[hostList.size()]));
		}
		System.out.println("ELASTICSEARCH...CONNECT? " + builde);
		return new RestHighLevelClient(builde);
	}
//	public static void main(String[] args) {
		/**
		 * index 모든 데이터 조회
		 * 공통된 index값을 통해서 조회하기 위해 데이터의 아이디에 모두 1을 넣어봤음
		 * 예를 들어 index에 type이라던지 mode라는 index를 만들고 게시판이면 모두 board값을 준다던지 하면
		 * matchQuery("type","board")로 게시판의 모든 글을 조회하는 기능으로 사용할 수 있을 것 같음 
		 */
		/*List<String> address = new ArrayList<>();
		address.add("localhost:9200");
		
		final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
		SearchRequest searchRequest = new SearchRequest("board-test");
		searchRequest.scroll(scroll);													//최초 한번의 스크롤을 해주고
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchQuery("id", "1"));
		searchRequest.source(searchSourceBuilder);
		
		try {
			
			RestHighLevelClient client = getRestHighLevelClient(address, "elastic", "pwWYgu19mNCcYk7FRi7e", "D:/main/ES-master/config/certs/ca/ca.crt");
			SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
			String scrollId = searchResponse.getScrollId();
			SearchHit[] searchHits = searchResponse.getHits().getHits();				
			
			while (searchHits != null && searchHits.length > 0) {						//스크롤을 내릴때마다 10개씩 데이터를 가져옴, 스크롤을 내려도 데이터가 없으면 멈춤
				System.out.println(searchHits.length);
				for(SearchHit hit : searchHits) {										//한번 내린 스크롤에 있는 10개의 데이터를 출력
					Map<String, Object> map  = hit.getSourceAsMap();					//맵으로 받아서 원하는 키값의 데이터를 골라서 받을 수 있음
					System.out.println(map.get("title"));
				}
				SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);	//새로 스크롤을 내려서 다시 10개의 데이터를 받아옴
				scrollRequest.scroll(scroll);
				searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
				scrollId = searchResponse.getScrollId();
				searchHits = searchResponse.getHits().getHits();
			}
			System.out.println(searchHits.length);
			ClearScrollRequest clearScrollRequest = new ClearScrollRequest();			//스크롤 초기화
			clearScrollRequest.addScrollId(scrollId);
			ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
			boolean succeeded = clearScrollResponse.isSucceeded();
			System.out.println(succeeded);
			
		}catch (KeyManagementException | CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
			System.out.println("전제 조회 실패..");
		}
		*/
		
		
		
		/*List<String> address = new ArrayList<>();
		address.add("localhost:9200");
		
		SearchSourceBuilder builder = new SearchSourceBuilder();
		builder.query(QueryBuilders.matchAllQuery());
		builder.from(0);
		builder.from(5);
		builder.sort(new FieldSortBuilder("id").order(SortOrder.DESC));
		builder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		SearchRequest request = new SearchRequest("board-test");
		request.source(builder);
		SearchResponse response;
		
		try {
			
			RestHighLevelClient client = getRestHighLevelClient(address, "elastic", "pwWYgu19mNCcYk7FRi7e", "D:/main/ES-master/config/certs/ca/ca.crt");
			response = client.search(request, RequestOptions.DEFAULT);
			SearchHits hits = response.getHits();
			System.out.println(hits.getTotalHits());
			
			SearchHit[] searchHits = hits.getHits();
			for(SearchHit hit : searchHits) {
				Map<String, Object> result = hit.getSourceAsMap();
				System.out.println(result.get("title"));
			}
			
		} catch (KeyManagementException | CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
			System.out.println("조회실패...");
		}*/
		
		/**
		 * index 해당아이디에 맞는 결과 조회
		 */
		/*List<String> address = new ArrayList<>();
		address.add("localhost:9200");
		
		Gson gson = new Gson();
		
		String indexName = "board-test";
		String docId = "2";
		
		GetRequest request = new GetRequest(indexName, docId);
		GetResponse response = null;
		
		try {
			
			RestHighLevelClient client = getRestHighLevelClient(address, "elastic", "pwWYgu19mNCcYk7FRi7e", "D:/main/ES-master/config/certs/ca/ca.crt");
			
			response = client.get(request, RequestOptions.DEFAULT);
			
		} catch (KeyManagementException | CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
			System.out.println("조회실패...");
		}
		Map<String, Object> sourceAsMap = null;
		if(response.isExists()) {
			sourceAsMap = response.getSourceAsMap();
		}else {
			System.out.println("문서 없음");
		}
		System.out.println(sourceAsMap.get("id"));
		System.out.println(sourceAsMap.get("title"));
		System.out.println(sourceAsMap.get("content"));
		System.out.println(gson.toJson(sourceAsMap));*/
		
		/**
		 * index 색인
		 * */
		/*List<String> address = new ArrayList<>();
		address.add("localhost:9200");
		String indexName = "board-test";
		
		IndexRequest request = new IndexRequest(indexName).id("4");
		
		RestHighLevelClient client;
			
		try {
			client = getRestHighLevelClient(address, "elastic", "pwWYgu19mNCcYk7FRi7e", "D:/main/ES-master/config/certs/ca/ca.crt");
			request.source(XContentFactory.jsonBuilder().startObject()
					.field("id", request.id()).field("title", "["+request.id()+"]TITLE").field("content", "["+request.id()+"]CONTENT")
					.endObject());
					
			IndexResponse response = client.index(request, RequestOptions.DEFAULT);
			System.out.println("RESPONSE.STATUS..."+response.status());
		} catch (KeyManagementException | CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}*/
		/**
		 * index 해당아이디의 DOC 삭제
		 * */
		/*List<String> address = new ArrayList<>();
		address.add("localhost:9200");
		String indexName = "board-test";
		String docId = "4";
		RestHighLevelClient client;
		DeleteResponse response = null;
		
		try{
			
			client = getRestHighLevelClient(address, "elastic", "pwWYgu19mNCcYk7FRi7e", "D:/main/ES-master/config/certs/ca/ca.crt");
			DeleteRequest request = new DeleteRequest(indexName, docId);
			response = client.delete(request, RequestOptions.DEFAULT);
			
		} catch (KeyManagementException | CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
			System.out.println("문서 삭제에 실패");
		}
		System.out.println(response.getResult());*/
		/**
		 * index 수정
		 * */
		/*List<String> address = new ArrayList<>();
		address.add("localhost:9200");
		String indexName = "board-test";
		String docId = "4";
		RestHighLevelClient client;
		
		UpdateRequest request = new UpdateRequest(indexName, docId);
		UpdateResponse response = null;
		
		try {
			
			client = getRestHighLevelClient(address, "elastic", "pwWYgu19mNCcYk7FRi7e", "D:/main/ES-master/config/certs/ca/ca.crt");
			
			Map<String, Object> parameters = Collections.singletonMap("updateString", (Object)"[4]TITLE");
			Script inline = new Script(ScriptType.INLINE, "painless", "ctx._source.title = params.updateString", parameters);
			request.script(inline);
			
			response = client.update(request, RequestOptions.DEFAULT);
		} catch (KeyManagementException | CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
			System.out.println("문서 수정 실패");
		}
		System.out.println(response.getResult());*/
//	}
}