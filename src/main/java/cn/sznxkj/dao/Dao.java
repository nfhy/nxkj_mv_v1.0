package cn.sznxkj.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ObjectUtils;
@Repository
public class Dao implements DaoInterface{
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private TransactionTemplate transactionTemplate;
	
	@Override
	public List<Map<String, Object>> query(String sql, Object ...args) {
		List<Map<String, Object>> reList = null;
		try{
			reList = jdbcTemplate.queryForList(sql, args);
		}
		catch (Exception e) {
			System.out.println("查询出错，" + sql);
			e.printStackTrace();
		}
		return reList;
	}
	
	@Override
	public boolean update(String sql, Object ... args) {
		try {
			jdbcTemplate.update(sql, args);
			return true;
		}
		catch (Exception e) {
			System.out.println("更新出错，" + sql);
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public int count(String sql, Object ... args) {
		int count = -1;
		try {
			List list = jdbcTemplate.queryForList(sql, args);
			count = list.size();
		}
		catch(Exception e) {
			System.out.println("count查询出错," + sql);
			e.printStackTrace();
		}
		return count;
	}
	
	@Override
	public boolean batchUpdate(final List<String> sqlList) {
		if (ObjectUtils.isEmpty(sqlList)) {
			return false;
		}
		return transactionTemplate.execute(new TransactionCallback<Boolean>() {
			public Boolean doInTransaction(TransactionStatus status) {
				try{
					for (String sql : sqlList) {
						jdbcTemplate.update(sql);
					}
					return true;
				}
				catch (Exception e) {
					System.out.println(e.getMessage());
					throw new RuntimeException();
				}
			}  
		});
	}
}
