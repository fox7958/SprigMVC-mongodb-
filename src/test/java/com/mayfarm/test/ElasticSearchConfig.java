package com.mayfarm.test;

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
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.junit.Test;

import com.google.gson.Gson;

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
			};
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
	@Test
	public static void main(String[] args) {
		/**
		 * index 조회
		 */
		List<String> address = new ArrayList<>();
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
			long version = response.getVersion();
			sourceAsMap = response.getSourceAsMap();
			System.out.println("version : "+version);
		}
		System.out.println(sourceAsMap.get("id"));
		System.out.println(sourceAsMap.get("title"));
		System.out.println(sourceAsMap.get("content"));
		System.out.println(gson.toJson(sourceAsMap));
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
	}
}