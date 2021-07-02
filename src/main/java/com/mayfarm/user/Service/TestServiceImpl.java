package com.mayfarm.user.Service;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.mayfarm.user.DAO.TestDAO;
import com.mayfarm.user.bean.TestBean;

@Service
public class TestServiceImpl implements TestService{
	
	@Inject
	private TestDAO dao;
	
	@Override
	public List<TestBean> test() throws Exception {
		// TODO Auto-generated method stub
		return dao.test();
	}
}