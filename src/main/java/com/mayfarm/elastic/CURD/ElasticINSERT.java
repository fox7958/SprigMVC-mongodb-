package com.mayfarm.elastic.CURD;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentFactory;

import com.mayfarm.elastic.connet.ElasticSearchConfig;

public class ElasticINSERT {
	/**
	 * mongoDB에 저장된 _id로 받아와서
	 * ElasticSearch _id로 저장
	 * @throws IOException 
	 */
	public static void sourceAsMap(List<String> address, String username, String password, String ssl, String indexName, Map<String, String> board) throws IOException{
		System.out.println("색인요청...");
		
		IndexRequest request = new IndexRequest(indexName).id(board.get("docId"));
		
		RestHighLevelClient client = null;

		try {
			client = ElasticSearchConfig.getRestHighLevelClient(address,
											username,
											password,
											ssl);
			request.source(XContentFactory.jsonBuilder()
					.startObject()
					.field("type", "board")
					.field("title", board.get("title"))
					.field("content", board.get("content"))
					.field("date", board.get("date"))
					.endObject());
			
			System.out.println("색인 중...");
			
			IndexResponse response = client.index(request, RequestOptions.DEFAULT);
			
			System.out.println("RESPONSE.STATUS..."+response.status());
			System.out.println("색인완료");
		} catch (KeyManagementException | CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
			System.out.println("색인실패");
		} finally {
			//client.close();
			//System.out.println("client.close...");
		}
	}
}
