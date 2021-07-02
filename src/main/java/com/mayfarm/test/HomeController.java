package com.mayfarm.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.elasticsearch.client.RestHighLevelClient;
import org.json.JSONObject;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mayfarm.elastic.CURD.ElasticDELETE;
import com.mayfarm.elastic.CURD.ElasticGET;
import com.mayfarm.elastic.CURD.ElasticINSERT;
import com.mayfarm.elastic.CURD.ElasticSEARCH;
import com.mayfarm.test.vo.BoardVO;
import com.mayfarm.user.Service.TestService;
import com.mayfarm.user.bean.TestBean;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController{
	
	/**
	 * Elasticsearch host, username, password, ssl 위치 정보
	 * */
	final List<String> esAddress = new ArrayList<String>();
	final String esHost = "localhost:9200";
	final String esUserName = "elastic";
	final String esPassword = "pwWYgu19mNCcYk7FRi7e";
	final String esSSl = "D:/main/ES-master/config/certs/ca/ca.crt";
	
	/**
	 * MariaDB Connect
	 * */
	@Inject
	TestService service;
	
	@Autowired
	private ElasticDELETE elasticDELETE;
	
	
	
	@RequestMapping(value="/test", method=RequestMethod.GET)
	public String test(Model model) throws Exception{
		List<TestBean> list;
		list = service.test();
		model.addAttribute("list", list);
		return "test";
	}
	/**
	 * 각 권한 별 페이지 이동
	 * */
	@RequestMapping(value = "/loginPage", method = RequestMethod.GET)
	public String loginPage() {
		return "loginPage";
	}
	@RequestMapping(value="/page")
	public String page() throws Exception{
		return "/page";
	}
	@RequestMapping(value="/user/page")
	public String userPage() throws Exception{
		return "/user/page";
	}
	@RequestMapping(value="/member/page")
	public String memberPage() throws Exception{
		return "/member/page";
	}
	@RequestMapping(value="/admin/page")
	public String adminPage() throws Exception{
		return "/admin/page";
	}
	/**
	 * Error페이지
	 * */
	@RequestMapping(value = "/access_denied_page")
	public String accessDeniedPage() throws Exception{
		return "/access_denied_page";
	}
	
	/**
	 * MongoDB Connect 
	 */
	MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
	MongoDatabase database = mongoClient.getDatabase("test");
	MongoCollection<Document> collection = database.getCollection("board");
	
	/**
	 * 초기 Main화면으로 이동
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home() {
		esAddress.add(esHost);
		return "index";
	}
	@RequestMapping(value = "/uesr/main.do")
	public String login() {
		return "user/main";
	}
	
	/*@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String login(Locale locale, Model model) {
		System.out.println("login---success");
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(principal instanceof UserDetails) {
			String username = ((UserDetails)principal).getUsername();
			System.out.println("if==="+username);
		}else {
			String username = principal.toString();
			System.out.println("else==="+username);
		}
		return "user/main";
	}*/
	/**
	 * MongoDB 데이터들 게시판목록에 뿌려줌
	 * cursor로 가져온 jSon형식 데이터를 사용하기 편하게 JSONObject로 옮겨줌
	 * MongoDB에 자동으로 생성된 ID는 _id라는 이름으로 가져와야하고, 앞에 불필요한(?) 부분을 제거해줌
	 * */
	@RequestMapping("list.do")
	@ResponseBody
	public Map<String, Object> list() throws Exception{
		System.out.println("list.do====");
		Map<String, Object> map = new HashMap<String, Object>();
		List<BoardVO> list = new ArrayList<BoardVO>();
		
		MongoCursor<Document> cursor = collection.find().sort(Filters.eq("date", -1)).iterator();	//전체 데이터를 조회할때 날짜순으로 가져옴
		
		String elasticID = null; 
		
		try {
			for(int i = 0; i < collection.countDocuments(); i++) {
				ArrayList<BoardVO> list1 = new ArrayList<BoardVO>();
				
				while(cursor.hasNext()) {
					JSONObject jsonObject = new JSONObject(cursor.next().toJson()); //jsonobject로 옮겨서 작업

					String title = jsonObject.optString("title");
					String content = jsonObject.optString("content");
					String date = jsonObject.optString("date");
					String id = jsonObject.optString("_id").substring(9,33);
					elasticID = id;
					
					System.out.println(jsonObject.optString("_id"));
					
					BoardVO board = new BoardVO();	
					board.setTitle(title);
					board.setContent(content);
					board.setDate(date);
					board.setId(id);
					
					list1.add(board);
				}
				list.addAll(list1);
			}
		}finally {
			cursor.close();
		}
		map.put("list", list);
		
		/**
		 * ElasticSearch TEST
		 * */
		//System.out.println("elasitcID ==== "+elasticID);
		//ElasticSEARCH.sourceAsMap(esAddress, esUserName, esPassword, esSSl, "board-test", "안녕");
		return map;
	}
	/**
	 * 사용자가 작성한 내용을 MongoDB에 저장
	 * id값은 데이터베이스에 저장될 때 자동 생성됨
	 * */
	@RequestMapping(value = "add.do", method = RequestMethod.POST) // POST로만 받겠다.
	@ResponseBody
	public Map<String, Object> add(
			@RequestParam(value="title", required = true) String title,
			@RequestParam(value="content", required = false, defaultValue = "") String content) throws Exception{
		System.out.println("add.do====");
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, String> elasticBoard = new HashMap<String, String>();
		
		long systemTime = System.currentTimeMillis();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
		String date = formatter.format(systemTime);
		
		try {	
			Document doc = new Document("title", title).append("content", content).append("date", date);
			collection.insertOne(doc);
			
			elasticBoard.put("title", title);
			elasticBoard.put("content", content);
			elasticBoard.put("date", date);
			
			map.put("returnCode", "success");
			map.put("returnDesc", "Mongo:데이터가 정상적으로 등록되었습니다."); 
			System.out.println(map.get("returnCode"));
			
			ObjectId mongoId = doc.getObjectId("_id");
			System.out.println("MongoID : " + mongoId);
			elasticBoard.put("docId", mongoId.toString());
			
			ElasticINSERT.sourceAsMap(esAddress, esUserName, esPassword, esSSl, "board-test", elasticBoard);
			
		} catch (Exception e) {
			map.put("returnCode", "failed");
			map.put("returnDesc", "데이터 등록에 실패하였습니다.");
		}
		return map;
	}
	/**
	 * 해당 게시글의 ID값을 받아와서 삭제
	 * */
	@RequestMapping(value = "del.do", method = RequestMethod.POST) // POST로만 받겠다.
	@ResponseBody
	public Map<String, Object> del(@RequestParam(value="id", required = true) String id) throws Exception{
		System.out.println("del.do====");
		Map<String, Object> map = new HashMap<String, Object>();
		
		try {	
			if(id.equals(null)) {
				map.put("returnCode", "failed");
				map.put("returnDesc", "데이터 삭제에 실패하였습니다.");
			}else {
				System.out.println("id--->"+id);
				
				Document doc = new Document();					//Mongodb의 _id는 String타입이 아니라 ObjectId 타입이기 때문에 Document로 ObjectId를 만들어줘서 삭제해야함
				doc.put("_id", new ObjectId(id));
				System.out.println(doc.get("_id"));
				
				collection.deleteOne(doc);
				
				map.put("returnCode", "success");
				map.put("returnDesc", "데이터가 정상적으로 삭제되었습니다.");
				
				
				elasticDELETE.sourceAsMap("board-test", id);
			}
		} catch (Exception e) {
			map.put("returnCode", "failed");
			map.put("returnDesc", "데이터 삭제에 실패하였습니다.");
		}
		return map;		
	}
	/**
	 * MongoDB 같은 Id값 찾아서 수정
	 * */
	@RequestMapping(value = "mod.do", method = RequestMethod.POST) // POST로만 받겠다.
	@ResponseBody
	public Map<String, Object> mod(
			@RequestParam(value="id", required = true) String id,
			@RequestParam(value="title", required = true) String title,
			@RequestParam(value="content", required = false, defaultValue = "") String content) throws Exception{
		System.out.println("mod.do====");
		Map<String, Object> map = new HashMap<String, Object>();
		
		long systemTime = System.currentTimeMillis();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
		String date = formatter.format(systemTime);
		
		try {
			System.out.println("id--->"+id);
			
			Document doc = new Document();					//id값 가져와서 같은 아이디인 부분 삭제
			doc.put("_id", new ObjectId(id));
			System.out.println(doc.get("_id"));
			
			if(doc.get("_id").equals("")) {
				map.put("returnCode", "null");
				map.put("returnDesc", "수정할 게시글 없음");
			}else {
				/** 처음 _id값을 비교하여 삭제할 DB문서를 찾고 new Document ("$set", 으로 수정할 것이라는 쿼리를 알림, 다음 new Document에 수정 할 내용 담아서 update */
				collection.updateOne(Filters.eq("_id", doc.get("_id")), new Document("$set", new Document("title", title).append("content", content).append("date", date)));
				
				map.put("returnCode", "success");
				map.put("returnDesc", "데이터가 정상적으로 수정되었습니다."); 
				System.out.println(map.get("returnCode"));
			}
		} catch (Exception e) {
			map.put("returnCode", "failed");
			map.put("returnDesc", "데이터 수정에 실패하였습니다.");
		}
		return map;
	}
}












