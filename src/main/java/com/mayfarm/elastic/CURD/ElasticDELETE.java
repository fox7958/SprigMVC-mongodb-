package com.mayfarm.elastic.CURD;

import java.io.IOException;
import java.util.List;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mayfarm.elastic.connet.ElasticSearchConfig;
import com.mayfarm.util.ElasticsearchConn;

@Repository
public class ElasticDELETE {

	@Autowired
	private ElasticsearchConn conn;
	private RestHighLevelClient client;

	public ElasticDELETE(ElasticsearchConn conn) {
		client = conn.getClient();
	}
	/*public ElasticDELETE() {
		client = conn.getClient();
	}*/

	/**
	 * index 해당아이디의 DOC 삭제
	 */
	public void sourceAsMap(String indexName, String id) throws IOException {

		DeleteResponse response = null;

		try {

			//client = ElasticSearchConfig.getRestHighLevelClient(address, username, password, ssl);
			DeleteRequest request = new DeleteRequest(indexName, id);
			
			response = client.delete(request, RequestOptions.DEFAULT);

			System.out.println(response.getResult() + " 문서 삭제 성공");

		} catch (Exception e) {
			
			e.printStackTrace();
			System.out.println("문서 삭제 실패");

		} finally {
			//client.close();
			//System.out.println("client.close()");
		}
	}
}
