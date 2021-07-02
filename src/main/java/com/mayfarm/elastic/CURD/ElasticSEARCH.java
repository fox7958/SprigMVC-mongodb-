package com.mayfarm.elastic.CURD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.mayfarm.elastic.DTO.ElasticDTO;
import com.mayfarm.elastic.connet.ElasticSearchConfig;


public class ElasticSEARCH {
	
	public static Map<String, Object> sourceAsMap(List<String> address, String username, String password, String ssl, String indexName, String keyword) throws IOException{		
		RestHighLevelClient client = null;
		
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		
		sourceBuilder.query(QueryBuilders.matchQuery("title", keyword));
		sourceBuilder.from(0);
		sourceBuilder.size(5);
//		sourceBuilder.sort(new FieldSortBuilder("date").order(SortOrder.DESC));
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		
		SearchRequest searchRequest = new SearchRequest(indexName);
		searchRequest.source(sourceBuilder);
		
		SearchResponse searchResponse = null;
		SearchHits searchHits = null;
		List<ElasticDTO> result = new ArrayList<>();
		
		Map<String, Object> resultQuery = new HashMap<String, Object>();
	
		try {
			client = ElasticSearchConfig.getRestHighLevelClient(address, username, password, ssl);
			searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
			searchHits = searchResponse.getHits();
			
			for(SearchHit hit : searchHits) {
				Map<String, Object> sourceAsMap = hit.getSourceAsMap();
				ElasticDTO answer = new ElasticDTO();
				answer.setTitle(sourceAsMap.get("title")+"");
				answer.setContent(sourceAsMap.get("content")+"");
				answer.setDate(sourceAsMap.get("date")+"");
				result.add(answer);
			}
			for(int i = 0; i < result.size(); i++) {
				System.out.println(result.get(i).getAll());
			}
			
			System.out.println("MatchAllQuery 조회 성공...");
		}catch (Exception e) {
			e.printStackTrace();
			System.out.println("MatchAllQuery 조회 실패...");
		}finally {
			client.close();
			System.out.println("client.close()");
		}
		
		return resultQuery;
	}
}
