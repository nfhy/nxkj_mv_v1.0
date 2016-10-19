package cn.sznxkj.dao;

import java.util.List;
import java.util.Map;

public interface DaoInterface {

	List<Map<String, Object>> query(String sql, Object... args);

	boolean update(String sql, Object ... args);

	int count(String sql, Object... args);

	boolean batchUpdate(List<String> sqlList);

}
