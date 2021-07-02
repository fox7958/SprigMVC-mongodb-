package com.mayfarm.util;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;

public class ElasticsearchConn {

	private RestHighLevelClient restHighLevelClient;

	
	public ElasticsearchConn(List<String> hosts, String username, String password) {
		try {
			restHighLevelClient = getRestHighLevelClient(hosts, username, password);
			System.out.println("hosts : " + hosts.get(0) + " username : " + username + " password : " + password);
		} catch (KeyManagementException | CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
	}

	public RestHighLevelClient getClient() {
		return restHighLevelClient;
	}

	public RestHighLevelClient getRestHighLevelClient(List<String> hosts, String username, String password)
			throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException,
			KeyManagementException {

		List<HttpHost> hostList = new ArrayList<HttpHost>();

		final String regex = "^([.0-9a-z]+)(?:\\:(\\d+))?";
		String hostname = "", port = "";
		Pattern pattern;
		Matcher matcher;

		for (String host : hosts) {
			if (StringUtils.isEmpty(host))
				continue;
			host = host.trim();
			pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(host);
			if (matcher.find()) {
				hostname = matcher.group(1);
				if ((hostname.toLowerCase()).equals("localhost")) {
					hostname = "127.0.0.1";
				}
				port = matcher.groupCount() == 2 ? matcher.group(2) : "9200";
			}
			hostList.add(new HttpHost(hostname, Integer.parseInt(port), "http"));
		}
		RestClientBuilder builde = null;
		
		if (!StringUtils.isBlank(username) && !StringUtils.isBlank(password)) {
			final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

			HttpClientConfigCallback httpClientConfigCallback = new HttpClientConfigCallback() {
				@Override
				public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
					return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
				}
			};
			builde = RestClient.builder(hostList.toArray(new HttpHost[hostList.size()]))
					.setHttpClientConfigCallback(httpClientConfigCallback);

		} else {
			builde = RestClient.builder(hostList.toArray(new HttpHost[hostList.size()]));
		}
		System.out.println("ELASTICSEARCH...CONNECT? " + builde);
		return new RestHighLevelClient(builde);
	}
}
