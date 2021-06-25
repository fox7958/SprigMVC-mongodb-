package com.mayfarm.security.DAO;

import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mayfarm.security.CustomUserDetails;

@Repository("userAuthDAO")
public class UserAuthDAO {
	
	@Autowired
	private SqlSessionTemplate sqlSession;
	
	public CustomUserDetails getUserById(String username) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("username", username);
		//System.out.println(username+"++++++");
		//return sqlSession.selectOne("user.selectuser", username);
		return sqlSession.selectOne("user_sql.selectuser", map);
	}
	 
}
