package com.mayfarm.user.DAO;

import java.util.List;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.mayfarm.user.bean.TestBean;

@Repository
public class TestDAOImpl implements TestDAO{
	private static final String namespace = "com.mayfarm.test.user.memberMapper";
	
	@Inject
	private SqlSession splSession;
	
	@Override
	public List<TestBean> test() throws Exception {
		// TODO Auto-generated method stub
		return splSession.selectList(namespace + ".test");
	}
}
