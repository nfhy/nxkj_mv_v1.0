package cn.sznxkj.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.Resource;

import com.google.common.collect.ImmutableMap;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import cn.sznxkj.dao.Dao;
@Service
public class MyService implements ServiceInterface {
	@Resource
	private Dao dao;
	
	private static Map<String, Object> prototypeReMap = new HashMap<>();
	static {
		prototypeReMap.put("resCode", "0");
		prototypeReMap.put("desc", "");
		prototypeReMap.put("cmdToken", "");
	}

	/*
	 * 通知场景：2管理员or技术人员,3超级管理员
请求包体：
{"msg":"login","data":{"userName":"zhenglei","passWord":"1"}}
响应：
{"msg":"login","data":{"resCode":"0","desc":"",”role”:2,”bRecvWarn”:1,”token”:”zhenglei”}}}

	 */
	@Override
	public Map queryLogin(Map data) {
		Map<String, Object> reMap = new HashMap<>();
		reMap.putAll(prototypeReMap);
		String userName = (String) data.get("userName");
		String passWord = (String) data.get("passWord");
		if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(passWord)) {
			reMap.put("resCode", "100007");//参数不能为空，疑似攻击
			return reMap;
		}
		List<Map<String, Object>> userList = dao.query("SELECT u.userId,u.nickName,u.role,u.recvWarn,u.enable,u.tel FROM users u where u.name=? and u.pwd=?", userName, passWord);
		if (ObjectUtils.isEmpty(userList)) {
			reMap.put("resCode", "100003");//用户名密码错误
			return reMap;
		}
		Map<String, Object> user = userList.get(0);
		int enable = (int) user.get("enable");
		if (enable == 0) {
			reMap.put("resCode", "100012");//用户停用，无法登陆
			return reMap;
		}
		int role = (int) user.get("role");
		int bRecvWarn = (int) user.get("recvWarn");
		String nickName = (String) user.get("nickName");
		String tel = (String) user.get("tel");
		reMap.put("role", role);
		reMap.put("bRecvWarn", bRecvWarn);
		reMap.put("nickName", nickName);
		reMap.put("tel", tel);
		reMap.put("enable", enable);
		reMap.put("token", UUID.randomUUID().toString());
		return reMap;
	}

	@Override
	public Map updateJpushId(Map data) {
		Map<String, Object> reMap = new HashMap<>();
		reMap.putAll(prototypeReMap);
		String id = (String) data.get("jpushId");
		String userName = (String) data.get("userName");
		System.out.println(id + ":" + userName);
		dao.update("update users u set u.jpushid=? where u.name=?", id, userName);
		return reMap;
	}

	/*
	 * {"msg":"webGetDevTypeList",
	 * "data":{"resCode":"0","desc":"操作完成",”cmdToken”:”xxxxx”,
	 * "devTypeList":[{“devTypeIndex”:1,”devTypeName”:”DO”,”paramName”:”mg/L”,”min”:-40,”max”:100}
	 * @see cn.sznxkj.service.ServiceInterface#webGetDevTypeList()
	 */
	@Override
	public Map queryWebGetDevTypeList() {
		Map<String, Object> reMap = new HashMap<>();
		reMap.putAll(prototypeReMap);
		reMap.put("devTypeList", DataUtil.devTypeList);
		return reMap;
	}

	/*
	 * {"msg":"webField",
	 * "data":{"resCode":"0","desc":"操作完成","fieldIndex":0,”cmdToken”:”xxxxx”,
	 * "result":
	 * [{"fieldIndex":1,"fieldName":"甲鱼塘1",”fieldDesc”:”区域1描述”,
	 * "devList":
	 * [{"devIndex":100100,"devName":"设备1","devTpeIndex":4,”min”:5.0,”max”:8.0 },
	 * {"devIndex":100101, "devName":"设备2","devTypeIndex":4,”min”:5.0,”max”:8.0 }]}
	 */
	@Override
	public Map queryWebField(String userName) {
		Map<String, Object> reMap = new HashMap<>();
		reMap.putAll(prototypeReMap);
		List<Object> fieldList = new ArrayList<>();
		for (int i=0;i<=DataUtil.staticFieldList.size()-1; i++) {
			Map fieldMap = (Map) DataUtil.staticFieldList.get(i);
			Object o = fieldMap.get("userList");
			if ("admin".equals(userName)) {
				fieldList.add(fieldMap);
			}
			else {
				if (o.equals("")) continue;
				String[] userList = (String[]) o;
				if (Arrays.asList(userList).contains(userName)) {
					fieldList.add(fieldMap);
				}
			}
		}
		reMap.put("result", fieldList);
		return reMap;
	}

	/**
	 * {"msg":"fieldMgr","data":
	 * {"token”:”xxxx” ,”cmd”:1,
	 * ”detail”:
	 * { ”userName”:”zhenglei”,”fieldIndex”:1,”fieldName”:”区域1”,”fieldDesc”:”区域描述”}}}
	 */
	@Override
	public Map updateFieldMgr(Map data) {
		Map<String, Object> reMap = new HashMap<>();
		reMap.putAll(prototypeReMap);
		Map detail = (Map) data.get("detail");
		if (ObjectUtils.isEmpty(detail)) {
			reMap.put("resCode", "100007");//数据格式错误
			return reMap;
		}
		boolean isEdit = detail.containsKey("fieldIndex");
		String fieldDesc = (String) detail.get("fieldDesc");
		String fieldName = (String) detail.get("fieldName");
		//List userList = (List) detail.get("userList");
		if (StringUtils.isEmpty(fieldName) || StringUtils.isEmpty(fieldDesc)) {
			reMap.put("resCode", "100007");//数据格式错误
			return reMap;
		}
		if (!isEdit) {
			boolean update;
			update = dao.update("insert into field(fieldName, fieldDesc) values(?,?)", fieldName, fieldDesc);
			if (!update) {
				reMap.put("resCode", "100009");//修改失败
				return reMap;
			}
			List fieldList = dao.query("select max(fieldId) id from field");
			Map newField = (Map) fieldList.get(0);
			int newFieldIndex = (int) newField.get("id");
			List<String> sqlList = new ArrayList<>();
			String sql2 = "delete from uservsfield where fieldid='"+newFieldIndex+"'";
			sqlList.add(sql2);
			/*
			if (null != userList) {
				for (Object userName : userList) {
					sqlList.add("insert into uservsfield(userName,fieldId) values('"+userName+"', "+newFieldIndex+")");
				}
			}*/
			dao.batchUpdate(sqlList);
		}
		return reMap;
	}
	/**
	 * {"msg":"webModifyField",
	 * ”data”:{"fieldIndex":1,"fieldName":"甲鱼塘1",”fieldDesc”:”区域描述”, ”userName”:”zhenglei”, ”token”:”zhenglei”,
	 * "devList":[{"devIndex":10100,”min”:5.5,”max”:8.0 },{"devIndex":10101,”min”:5.5,”max”:8.0 }]}}
	 */
	@Override
	public Map updateWebModifyField(Map data) {
		Map<String, Object> reMap = new HashMap<>();
		reMap.putAll(prototypeReMap);
		if (ObjectUtils.isEmpty(data)) {
			reMap.put("resCode", "100007");//数据格式错误
			return reMap;
		}
		int fieldIndex = (int) data.get("fieldIndex");
		String fieldDesc = (String) data.get("fieldDesc");
		String fieldName = (String) data.get("fieldName");
		List devList = (List) data.get("devList");
		List userList = (List) data.get("userList");
		if (fieldIndex <= 0 || StringUtils.isEmpty(fieldName) || StringUtils.isEmpty(fieldDesc)) {
			reMap.put("resCode", "100007");//数据格式错误
			return reMap;
		}
		int count = dao.count("select 1 from field where fieldId = ?", fieldIndex);
		if (count <= 0) {
			reMap.put("resCode", "100014");//园地不存在
			return reMap;
		}
		List<String> sqlList = new ArrayList<>();
		//园地更新语句
		String fieldSql = ("update field set fieldName='"+fieldName+"', fieldDesc='"+fieldDesc+"' where fieldId="+fieldIndex);
		sqlList.add(fieldSql);

		List<Object> devIndexList = new ArrayList<>();
		List<Object> tmp = new ArrayList<>();
		//处理园地关联设备的标准值
		if (!ObjectUtils.isEmpty(devList)) {
			for (int i = 0; i <= devList.size()-1; i++) {
				Map<String, Object> dev = (Map<String, Object>) devList.get(i);
				int devIndex = (int) dev.get("devIndex");
				int devId = devIndex/100;
				if (!tmp.contains(devId)) {
					Map<String, Object> devMap = new HashMap<>();
					devMap.put("devId", devId);
					devIndexList.add(devMap);
					tmp.add(devId);
				}
				int channelId = devIndex%100;
				String sql = "update devices set c:index_fieldid=" + fieldIndex + ", c:index_max=" + dev.get("max") + ", c:index_min=" + dev.get("min") + " where devId="+devId;
				sql = sql.replaceAll(":index", ""+channelId);
				sqlList.add(sql);
				System.out.println(sql);
			}
		}
		String sql2 = "delete from uservsfield where fieldid='"+fieldIndex+"'";
		sqlList.add(sql2);
		for (Object userName : userList) {
			sqlList.add("insert into uservsfield(userName,fieldId) values('"+userName+"', "+fieldIndex+")");
		}
		boolean update = dao.batchUpdate(sqlList);
		if (!update) {
			reMap.put("resCode", "100009");//修改失败
			return reMap;
		}
//		else {//旧代码，暂留
//			postMsgDev(devIndexList);
//		}
		return reMap;
	}
	/**
	 * "result":[
	 * {"devIndex":1001,"devName":"1号数据采集仪","devLocate":"1号农田",
	 * "devTypeIndex":6,"devPower":"1","devDesc":"设备描述1",
	 * ”channelDevList”:[{“devIndex”:100100,”devTypeIndex”:5,”fieldIndex”:1}
	 *
	 * devIndex=0 没有通道设备
	 * fieldIndex=0没有关联园地
	 */
	@Override
	public Map queryWebDeviceList() {
		Map<String, Object> reMap = new HashMap<>();
		reMap.putAll(prototypeReMap);
		reMap.put("result", DataUtil.deviceList);
		return reMap;
	}

	/*
	 * //{"msg":"devMgr",
    // "data":{"token”:”xxxx” ,”cmd”:1,
    // ”detail”:{ ”userName”:”zhenglei”,”devIndex”:101,”devName”:”设备1”,”devTypeIndex”:2,”devDesc”:”设备描述”,”devLocate”:”位置信息”,
    // ”channelDevList”:[{“devIndex”: 0,”devTypeIndex”:2,”devName”:”PH检测”,”fieldIndex”:1, ”min”:5.5,”max”:8.0},
    // {“devIndex”: 1,”devTypeIndex”:3, ”devName”:”PH检测”,”fieldIndex”:1, ”min”:5.5,”max”:8.0}]}}}

//c0_devName,c0_devTypeId,c0_fieldId,c0_max,c0_min
	 */
	public Map updateDevMgr(Map data) {
		Map<String, Object> reMap = new HashMap<>();
		reMap.putAll(prototypeReMap);
		if (ObjectUtils.isEmpty(data)) {
			reMap.put("resCode", "100007");//数据格式错误
			return reMap;
		}
		int cmd = (int) data.get("cmd");
		boolean isEdit = cmd == 2;
		data = (Map) data.get("detail");
		boolean update;
		String devName = DataUtil.valueForKey(data, "devName");
		String devDesc = DataUtil.valueForKey(data, "devDesc");
		String devLocate = DataUtil.valueForKey(data, "devLocate");
		int devIndex = 0;
		if (!isEdit) {
			StringBuilder sb = new StringBuilder("insert into devices(devName,devTypeId,devDesc,devLocate,userName,devPower,tag)");
			StringBuilder sb1 = new StringBuilder("values(?,1,?,?,'',0,?)");
			String sql = sb.toString() + sb1.toString();
			String tag = "" + System.currentTimeMillis() + new java.util.Random().nextInt(1000);
			update = dao.update(sql, devName, devDesc, devLocate, tag);
			if (update) {
				List devList = dao.query("select devid id from devices where tag=?", tag);
				Map idMap = (Map) devList.get(0);
				devIndex = (int) idMap.get("ID");
			}
		}else {
			devIndex = (int) data.get("devIndex");
		}
		List channelDevList = (List) data.get("channelDevList");
		if (ObjectUtils.isEmpty(channelDevList)) {
			channelDevList = new ArrayList<>();
		}
		StringBuilder sb = new StringBuilder("update devices set devName='"+devName+"', devDesc='"+devDesc+"', devLocate='"+devLocate+"'");
		List<Integer> existChannels = new ArrayList<>();
		String template = ",c:index_devName='',c:index_devTypeId=:devTypeId,c:index_fieldId=:fieldId,c:index_max=:max,c:index_min=:min";
		for (int i = 0; i <= channelDevList.size() - 1; i++) {
			Map<String, Object> channelDev = (Map) channelDevList.get(i);
			int channelDevIndex = (int) channelDev.get("devIndex");
			int devTypeIndex = (int) channelDev.get("devTypeIndex");
			int fieldIndex = (int) channelDev.get("fieldIndex");
			String min = "" + channelDev.get("min");
			String max = "" + channelDev.get("max");
			existChannels.add(channelDevIndex);
			String tmp = template.replaceAll(":index", "" + channelDevIndex);
			tmp = tmp.replace(":devTypeId", ""+devTypeIndex);
			tmp = tmp.replace(":fieldId", ""+fieldIndex);
			tmp = tmp.replace(":max", max);
			tmp = tmp.replace(":min", min);
			sb.append(tmp);
		}
		for (int i = 0; i <= 7; i++) {
			if (existChannels.contains(i)) {
				continue;
			}
			String tmp = ",c:index_devName='',c:index_devTypeId=0,c:index_fieldId=0";
			sb.append(tmp.replaceAll(":index", ""+i));
		}
		sb.append(" where devId = " + devIndex);
		update = dao.update(sb.toString());
		if (update) {
			List<Object> devIndexList = new ArrayList<>();
			Map<String, Object> devMap = new HashMap<>();
			devMap.put("devId", devIndex);
			devIndexList.add(devMap);
		}
		return reMap;
	}

	/**
	 * {"msg":"webDevData",
	 * "data":{"resCode":"0","desc":"操作完成",”cmdToken”:”xxxxx”,
	 * "result":[{“fieldIndex”:1,
	 * ”devList”:[{ ”devIndex”:100100, "val":5.0,”warn”:0,"time":"2016-01-14 15:30:30"},
	 * { ”devIndex”:100101,"val":55, ”warn”:1,"time":"2016-01-14 15:30:30"}]},
	 * @return
	 */
	@Override
	public Map queryWebDevData(String userName) {
		Map<String, Object> reMap = new HashMap<>();
		reMap.putAll(prototypeReMap);
		List result = queryDevDataByUser(userName);
		reMap.put("result", result);
		return reMap;
	}

	/**
	 * {"msg”:"webUserList",
	 * "data":{"resCode":"0","desc":"操作完成",”cmdToken”:”xxxxx”,
	 * "result":[{“userId":1,”name”:"admin”,"nickName”:"admin”,”role”:1,”tel":"1333333333”,"recvWarn”:”1”,“enable”:1，fieldList:[1,2]]}
	 */
	@Override
	public Map queryWebUserList() {
		Map<String, Object> reMap = new HashMap<>();
		reMap.putAll(prototypeReMap);
		reMap.put("result", DataUtil.auserList);
		return reMap;
	}
	/**
	 * {"msg”:"userEdit","data":{"token”:”xxxx” ,”cmd”:1,
	 * ”detail”:{ “userId”:1,”name”:”zhenglei”,”nickName”:”zhenglei”,”role”:1,”tel”:”13333333”,”recvWarn”:”1”,”enable”:1,fieldList:’1,2'}}}
	 */
	@Override
	public Map updateUserMgr(Map data) {
		Map<String, Object> reMap = new HashMap<>();
		reMap.putAll(prototypeReMap);
		if (ObjectUtils.isEmpty(data)) {
			reMap.put("resCode", "100007");//数据格式错误
			return reMap;
		}
		int cmd = (int) data.get("cmd");
		boolean isEdit = cmd == 2;
		data = (Map) data.get("detail");
		String userName = DataUtil.valueForKey(data, "userName");
		String nickName = DataUtil.valueForKey(data, "nickName");
		String tel = DataUtil.valueForKey(data, "tel");
		int role = (int) data.get("role");
		int recvWarn = (int) data.get("recvWarn");
		int enable = (int) data.get("enable");
		List<Object> fieldList = (List) data.get("fieldList");
		List<String> sqlList = new ArrayList<>();
		if (isEdit) {
			String sql1 = "update users s set s.nickname=':nickName',s.role=:role,s.tel=':tel',s.recvwarn = :recvWarn,s.enable=:enable where s.name=':userName'";
			sql1 = sql1.replace(":nickName", nickName);
			sql1 = sql1.replace(":role", ""+role);
			sql1 = sql1.replace(":tel", tel);
			sql1 = sql1.replace(":recvWarn", ""+recvWarn);
			sql1 = sql1.replace(":enable", ""+enable);
			sql1 = sql1.replace(":userName", userName);
			sqlList.add(sql1);
			String sql2 = "delete from uservsfield where username='"+userName+"'";
			sqlList.add(sql2);
			for (Object fieldIndex : fieldList) {
				sqlList.add("insert into uservsfield(userName,fieldId) values('"+userName+"', "+fieldIndex+")");
			}
		}
		else {
			String sql1 = "insert into users(name,pwd, nickname,role,tel,recvwarn,enable) values(':userName','666666', ':nickName', :role, ':tel', :recvWarn, :enable)";
			sql1 = sql1.replace(":nickName", nickName);
			sql1 = sql1.replace(":role", ""+role);
			sql1 = sql1.replace(":tel", tel);
			sql1 = sql1.replace(":recvWarn", ""+recvWarn);
			sql1 = sql1.replace(":enable", ""+enable);
			sql1 = sql1.replace(":userName", userName);
			sqlList.add(sql1);
			String sql2 = "delete from uservsfield where username='"+userName+"'";
			sqlList.add(sql2);
			for (Object fieldIndex : fieldList) {
				sqlList.add("insert into uservsfield(userName,fieldId) values('"+userName+"', "+fieldIndex+")");
			}
		}
		dao.batchUpdate(sqlList);
		return reMap;
	}
	/**
	 *{"msg":"webHistory","data":{"devIndex":100100,"startTime":"2016-01-26 16:20:10","endTime":"",”space”:1,”token”:”zhenglei”}}
	 *
	 * "result":[{"average":10.0 , “min”:9.0,”max”:11.0,"time":"2016-01-26 16:20:00"} space = 2 | 3
     * ":[{"val":10.0 ,”warn”:0,"time":"2016-01-26 16:20:00"} space = 1
	 */
	@Override
	public Map queryWebHistory(Map data) {
		Map<String, Object> reMap = new HashMap<>();
		reMap.putAll(prototypeReMap);
		if (ObjectUtils.isEmpty(data)) {
			reMap.put("resCode", "100007");//数据格式错误
			return reMap;
		}
		if (!(data.containsKey("devIndex")&&data.containsKey("startTime")&&data.containsKey("endTime")&&data.containsKey("space"))) {
			reMap.put("resCode", "100007");//数据格式错误
			return reMap;
		}
		List result = new ArrayList<>();
		int devIndex = (int) data.get("devIndex");
		String startTime = (String) data.get("startTime");
		String endTime = (String) data.get("endTime");
		int space = Integer.parseInt("" + data.get("space"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (space == 1) {
			String sql = "SELECT val,warn,TIME FROM :devType v WHERE devid="+devIndex+" AND UNIX_TIMESTAMP(TIME) > UNIX_TIMESTAMP('"+startTime+"') ORDER BY TIME LIMIT 2048";
			List dataList = dao.query(sql.replace(":devType", "DO"));
			dataList.addAll(dao.query(sql.replace(":devType", "PH")));
			for (int i = 0; i <= dataList.size() - 1; i++) {
				Map<String, Object> adata = (Map) dataList.get(i);
				Map<String, Object> aresult = new HashMap<>();
				Double val = Double.parseDouble("" + adata.get("val"));
				int warn = (int) adata.get("warn");
				Date dtime = (Date) adata.get("time");
				String time = sdf.format(dtime);
				aresult.put("val", val);
				aresult.put("warn", warn);
				aresult.put("time", time);
				result.add(aresult);
			}
		}
		else {
			String sql;
			if (space == 2) {
				sql = "SELECT MAX(val) MAX, MIN(val) MIN, AVG(val) AVG,CONCAT(SUBSTR(TIME,1,13),':00:00') TIME FROM :devType v WHERE devid="+devIndex+" AND UNIX_TIMESTAMP(TIME) > UNIX_TIMESTAMP('"+startTime+"') AND UNIX_TIMESTAMP(TIME) < UNIX_TIMESTAMP('"+endTime+"') GROUP BY SUBSTR(TIME,1,13) ORDER BY TIME LIMIT 10000;";
			}
			else {
				sql = "SELECT MAX(val) MAX, MIN(val) MIN, AVG(val) AVG,CONCAT(SUBSTR(TIME,1,13),' 00:00:00') TIME FROM :devType v WHERE devid="+devIndex+" AND UNIX_TIMESTAMP(TIME) > UNIX_TIMESTAMP('"+startTime+"') AND UNIX_TIMESTAMP(TIME) < UNIX_TIMESTAMP('"+endTime+"') GROUP BY SUBSTR(TIME,1,10) ORDER BY TIME LIMIT 10000;";
			}
			List dataList = dao.query(sql.replace(":devType", "DO"));
			dataList.addAll(dao.query(sql.replace(":devType", "PH")));
			for (int i = 0; i <= dataList.size() - 1; i++) {
				Map<String, Object> adata = (Map) dataList.get(i);
				Map<String, Object> aresult = new HashMap<>();
				Double max = Double.parseDouble("" + adata.get("max"));
				Double min = Double.parseDouble("" + adata.get("min"));
				Double average = Double.parseDouble("" + adata.get("avg"));
				String time = (String) adata.get("time");
				aresult.put("max", max);
				aresult.put("min", min);
				aresult.put("average", roundDouble(average));
				aresult.put("time", time);
				result.add(aresult);
			}
		}
		reMap.put("result", result);
		return reMap;
	}
	/*
	 * {"msg":"webField",
	 * "data":{"resCode":"0","desc":"操作完成","fieldIndex":0,”cmdToken”:”xxxxx”,
	 * "result":
	 * [{"fieldIndex":1,"fieldName":"甲鱼塘1",”fieldDesc”:”区域1描述”,
	 * "devList":
	 * [{"devIndex":100100,"devName":"设备1","devTpeIndex":4,”min”:5.0,”max”:8.0 ,"val":55, ”warn”:1,"time":"2016-01-14 15:30:30"},
	 * {"devIndex":100101, "devName":"设备2","devTypeIndex":4,”min”:5.0,”max”:8.0,"val":55, ”warn”:1,"time":"2016-01-14 15:30:30" }]}
	 * ,
	 * "devTypeList":
	 * [{“devTypeIndex”:1,”devTypeName”:”DO”,”paramName”:”mg/L”,”min”:-40,”max”:100}]
	 *
	 */
	@Override
	public Map queryAppFields(String userName) {
		Map<String, Object> tmpFields = this.queryWebField(userName);
		Map<String, Object> tmpDevTypes = this.queryWebGetDevTypeList();
		List fields = (List) tmpFields.get("result");
		for (int i = 0; i<= fields.size()-1; i++) {
			Map field = (Map) fields.get(i);
			int fieldIndex = (int) field.get("fieldIndex");
			List devList = (List) field.get("devList");
			List devDataList = (List) DataUtil.webDevData.get(fieldIndex);
			for (int j = 0; j<=devList.size()-1; j++) {
				Map<String, Object> dev = (Map) devList.get(j);
				int devIndex = (int) dev.get("devIndex");
				for (int k = 0; k <= devDataList.size()-1; k++) {
					Map<String, Object> devData = (Map) devDataList.get(k);
					if (devIndex == (int)devData.get("devIndex")) {
						dev.put("val", devData.get("val"));
						dev.put("warn", devData.get("warn"));
						dev.put("time", devData.get("time"));
					}
				}
			}
		}
		tmpFields.put("devTypeList", tmpDevTypes.get("devTypeList"));
		tmpFields.put("msg", "appFields");
		return tmpFields;
	}

	/**
	 * 发送HttpPost请求
	 *
	 * @param strURL
	 *            服务地址
	 * @param params
	 *            json字符串,例如: "{ \"id\":\"12345\" }" ;其中属性名必须带双引号<br/>
	 * @return 成功:返回json字符串<br/>
	 *
	 * {"message":"devModify","detail":[{"devId":101},{"devId":102}]}
	 * {"message":"fieldModify","detail":[{"fieldId":1}]}
	 */
	private static final String strURL = "http://119.29.148.244:9990";

	private static String post(JSONObject json) {
		PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(strURL);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(json.toString());
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
	}

	private Double roundDouble(Double d) {
		java.text.DecimalFormat myformat=new java.text.DecimalFormat("0.00");
		String str = myformat.format(d);
		return Double.parseDouble(str);
	}

	private List queryDevDataByUser(String userName) {
		Map resultMap = new HashMap<>();
		List result = new ArrayList<>();
		String sql = "SELECT fieldid FROM uservsfield WHERE username=?";
		List fieldidList = dao.query(sql, userName);
		if (ObjectUtils.isEmpty(fieldidList)) {
			return new ArrayList<>();
		}
		for (int i = 0; i <= fieldidList.size()-1; i++) {
			Map<String, Object> fieldIdMap = (Map) fieldidList.get(i);
			int fieldid = (int) fieldIdMap.get("fieldid");
			resultMap.put(fieldid, DataUtil.webDevData.get(fieldid));
		}

		for (Object key : resultMap.keySet()) {
			int fieldIndex = (int) key;
			Map<String, Object> tmp = new HashMap<>();
			tmp.put("fieldIndex", fieldIndex);
			tmp.put("devList", resultMap.get(key));
			result.add(tmp);
		}
		return result;
	}

	/**
	 * {"msg”:"userEdit","data":{"token”:”xxxx” ,”cmd”:1,
	 * ”detail”:{ “userId”:1,”name”:”zhenglei”,”nickName”:”zhenglei”,”role”:1,”tel”:”13333333”,”recvWarn”:”1”,”enable”:1}}}
	 */
	@Override
	public Map updateAppUserMgr(Map data) {
		Map<String, Object> reMap = new HashMap<>();
		reMap.putAll(prototypeReMap);
		if (ObjectUtils.isEmpty(data)) {
			reMap.put("resCode", "100007");//数据格式错误
			return reMap;
		}
		data = (Map) data.get("detail");
		String userName = DataUtil.valueForKey(data, "userName");
		String tel = DataUtil.valueForKey(data, "tel");
		int recvWarn = (int) data.get("recvWarn");
		String sql1 = "update users s set s.tel=':tel',s.recvwarn = :recvWarn where s.name=':userName'";
		sql1 = sql1.replace(":tel", tel);
		sql1 = sql1.replace(":recvWarn", ""+recvWarn);
		sql1 = sql1.replace(":userName", userName);
		dao.update(sql1);
		return reMap;
	}
}
