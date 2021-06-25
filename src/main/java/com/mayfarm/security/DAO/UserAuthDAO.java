package com.mayfarm.security.DAO;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mayfarm.security.CustomUserDetails;

@Repository("userAuthDAO")
public class UserAuthDAO {
	
	@Autowired
	private SqlSessionTemplate sqlSession;
	
	public CustomUserDetails getUserById(String username) {
		System.out.println(username+"++++++");
		return sqlSession.selectOne("user.selectUserById", username);
	}
}
