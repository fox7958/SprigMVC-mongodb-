package com.mayfarm.test;

import java.nio.file.DirectoryStream.Filter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mayfarm.test.vo.BoardVO;
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
public class HomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	/**
	 * MongoDB Connect 
	 */
	MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
	MongoDatabase database = mongoClient.getDatabase("test");
	MongoCollection<Document> collection = database.getCollection("board");
	
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);
		
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		
		String formattedDate = dateFormat.format(date);
		
		model.addAttribute("serverTime", formattedDate );
		
		return "home";
	}

	/**
	 * MongoDB 데이터들 게시판목록에 뿌려줌
	 * cursor로 가져온 jSon형식 데이터를 사용하기 편하게 JSONObject로 옮겨줌
	 * MongoDB에 자동으로 생성된 ID는 _id라는 이름으로 가져와야하고, 앞에 불필요한(?) 부분을 제거해줌
	 * */
	@RequestMapping("/list.do")
	@ResponseBody
	public Map<String, Object> list() throws Exception{
		System.out.println("list.do====");
		Map<String, Object> map = new HashMap<String, Object>();
		List<BoardVO> list = new ArrayList<BoardVO>();

		MongoCursor<Document> cursor = collection.find().iterator();
		
		try {
			for(int i = 0; i < collection.countDocuments(); i++) {
				ArrayList<BoardVO> list1 = new ArrayList<BoardVO>();
				
				while(cursor.hasNext()) {
					JSONObject jsonObject = new JSONObject(cursor.next().toJson()); //jsonobject로 옮겨서 작업

					String title = jsonObject.optString("title");
					String content = jsonObject.optString("content");
					String date = jsonObject.optString("date");
					String id = jsonObject.optString("_id").substring(9,33);
					
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
		return map;
	}
	/**
	 * 사용자가 작성한 내용을 MongoDB에 저장
	 * id값은 데이터베이스에 저장될 때 자동 생성됨
	 * */
	@RequestMapping(value = "/add.do", method = RequestMethod.POST) // POST로만 받겠다.
	@ResponseBody
	public Map<String, Object> add(
			@RequestParam(value="title", required = true) String title,
			@RequestParam(value="content", required = false, defaultValue = "") String content) throws Exception{
		System.out.println("add.do====");
		Map<String, Object> map = new HashMap<String, Object>();
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:MM");
		Date time = new Date();
		String date = format.format(time);
		
		try {	
			Document doc = new Document("title", title).append("content", content).append("date", date);
			collection.insertOne(doc);
			
			map.put("returnCode", "success");
			map.put("returnDesc", "데이터가 정상적으로 등록되었습니다."); 
			System.out.println(map.get("returnCode"));
		} catch (Exception e) {
			map.put("returnCode", "failed");
			map.put("returnDesc", "데이터 등록에 실패하였습니다.");
		}
		return map;
	}
	/**
	 * 해당 게시글의 ID값을 받아와서 삭제
	 * */
	/*@RequestMapping(value = "/del.do", method = RequestMethod.POST)
	public String del(@RequestParam(value="id", required = true) String id) {
		System.out.println("del.do====");
		String result;
		if(id.equals(null)) {
			result = "fail";
		}else {
			System.out.println("id ---> " + id);
			result = "success";
		}
		System.out.println(result);
		return result;
	}*/
	@RequestMapping(value = "/del.do", method = RequestMethod.POST) // POST로만 받겠다.
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
	@RequestMapping(value = "/mod.do", method = RequestMethod.POST) // POST로만 받겠다.
	@ResponseBody
	public Map<String, Object> mod(
			@RequestParam(value="id", required = true) String id,
			@RequestParam(value="title", required = true) String title,
			@RequestParam(value="content", required = false, defaultValue = "") String content) throws Exception{
		System.out.println("mod.do====");
		Map<String, Object> map = new HashMap<String, Object>();
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:MM");
		Date time = new Date();
		String date = format.format(time);
		
		try {
			System.out.println("id--->"+id);
			
			Document doc = new Document();					//Mongodb의 _id는 String타입이 아니라 ObjectId 타입이기 때문에 Document로 ObjectId를 만들어줘서 삭제해야함
			doc.put("_id", new ObjectId(id));
			System.out.println(doc.get("_id"));
			
			collection.updateOne(Filters.eq("_id", doc.get("_id")), new Document("$set", new Document("title", title).append("content", content)));
			
			map.put("returnCode", "success");
			map.put("returnDesc", "데이터가 정상적으로 수정되었습니다."); 
			System.out.println(map.get("returnCode"));
		} catch (Exception e) {
			map.put("returnCode", "failed");
			map.put("returnDesc", "데이터 수정에 실패하였습니다.");
		}
		return map;
	}
}













