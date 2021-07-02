package com.mayfarm.elastic.CURD;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import com.mayfarm.elastic.connet.ElasticSearchConfig;

public class ElasticGET {
	/**
	 * index docId로 조회
	 * @throws IOException 
	 */
	public static Map<String, Object> sourceAsMap(List<String> address, String username, String password, String ssl, String indexName, String docId) throws IOException{
		//List<String> address = new ArrayList<String>();

		GetRequest request = new GetRequest(indexName).id(docId);
		GetResponse response = null;
		RestHighLevelClient client = null;
		
		try {
			
			client = ElasticSearchConfig.getRestHighLevelClient(address, username, password, ssl);
			response = client.get(request, RequestOptions.DEFAULT);
			
		}catch (Exception e) {
			
			e.printStackTrace();
			System.out.println("ElasticError 조회 실패...");
			
		}finally {
			client.close();
			System.out.println("client.close...");
		}
		Map<String, Object> result = null;
		
		if(response.isExists()) {
			result = response.getSourceAsMap();
		}else {
			System.out.println("조회한 결과가 없습니다.");
		}
		/*System.out.println(result.get("id"));
		System.out.println(result.get("title"));
		System.out.println(result.get("content"));*/
		
		return result;
	}
}
