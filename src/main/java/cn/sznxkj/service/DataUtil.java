package cn.sznxkj.service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import cn.sznxkj.dao.Dao;
import org.springframework.util.ObjectUtils;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class DataUtil {
	@Resource
	private Dao dao;
	static Map<String, Object> webDevData = new HashMap<>();//fieldid 与 关联设备实时数据的对应表，每分钟更新一次
	static List<Object> staticFieldList = new ArrayList<>();
	static List<Object> devTypeList = new ArrayList<>();
	static List<Object> deviceList = new ArrayList<>();
	static List<Object> auserList = new ArrayList<>();
	@PostConstruct
	public void refreshFieldListForEver() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				while (true) {
					refreshFieldList();
					refreshDevData();
					refreshWebGetDevTypeList();
					refreshWebDeviceList();
					refreshWebUserList();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		new Thread(r).start();
	}

	private void refreshFieldList() {
		List<Object> fieldList = new ArrayList<>();
		List<Map<String, Object>> dbfieldList =
				dao.query("SELECT A.fieldid,A.fieldName,A.fieldDesc,A.userlist FROM ( " +
						" SELECT f.fieldid,f.fieldName,f.fieldDesc,GROUP_CONCAT(u.userName) userList FROM FIELD f LEFT JOIN uservsfield u ON f.fieldid=u.fieldId " +
						" GROUP BY f.fieldid ORDER BY f.fieldid) A ".toUpperCase());
		if (!ObjectUtils.isEmpty(dbfieldList)) {
			for (Map<String, Object> dbfield : dbfieldList) {
				Map<String, Object> fieldMap = new HashMap<>();
				int fieldIndex = (int) dbfield.get("fieldid".toUpperCase());
				String fieldName = (String) dbfield.get("fieldName".toUpperCase());
				String fieldDesc = (String) dbfield.get("fieldDesc".toUpperCase());
				String userList = (String) dbfield.get("userList".toUpperCase());
				fieldMap.put("fieldIndex", fieldIndex);
				fieldMap.put("fieldName", fieldName);
				fieldMap.put("fieldDesc", fieldDesc);
				fieldMap.put("userList", StringUtils.isEmpty(userList)?"":userList.split(","));
				//处理园地关联的设备
				List<Object> devList = new ArrayList<>();
				List<Map<String, Object>> dbfieldDevList =
						dao.query("SELECT v.parentid,v.channelid,v.devName,v.devTypeId,v.fieldId,v.max,v.min FROM v_devices v WHERE v.fieldId="+fieldIndex+" ORDER BY devtypeid".toUpperCase());
				if (!ObjectUtils.isEmpty(dbfieldDevList)) {
					for (Map<String, Object> dbfieldDev : dbfieldDevList) {
						Map<String, Object> dev = new HashMap<>();
						int parentId = (int) dbfieldDev.get("parentId".toUpperCase());
						String channelId = (String) dbfieldDev.get("channelId".toUpperCase());
						String devName = (String) dbfieldDev.get("devName".toUpperCase());
						int devTypeIndex = (int) dbfieldDev.get("devTypeId".toUpperCase());
						double max = (double) dbfieldDev.get("max".toUpperCase());
						double min = (double) dbfieldDev.get("min".toUpperCase());
						int devIndex = parentId*100 + Integer.parseInt(channelId);
						dev.put("devIndex", devIndex);
						dev.put("devName", devName);
						dev.put("devTypeIndex", devTypeIndex);
						dev.put("min", min);
						dev.put("max", max);
						devList.add(dev);
					}
				}
				fieldMap.put("devList", devList);
				fieldList.add(fieldMap);
			}
		}
		staticFieldList.clear();
		staticFieldList.addAll(fieldList);
	}

	private void refreshDevData() {
		Map resultMap = new HashMap<>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sql = "SELECT v.TIME,v.devid,v.devtypeid,v.fieldid,v.val,v.warn FROM v_devdata_now v ".toUpperCase();
		List dataList = dao.query(sql);
		if (!ObjectUtils.isEmpty(dataList)) {
			for (int i = 0; i <= dataList.size() - 1; i++) {
				Map data = (Map) dataList.get(i);
				Date time = (Date) data.get("time".toUpperCase());
				int devIndex = (int) data.get("devid".toUpperCase());
				int fieldIndex = (int) data.get("fieldid".toUpperCase());
				String val = data.get("val".toUpperCase()) + "";
				int warn = (int) data.get("warn");
				Map<String, Object> dev = new HashMap<>();
				dev.put("devIndex", devIndex);
				dev.put("val", Double.parseDouble(val));
				dev.put("warn", warn);
				dev.put("time", sdf.format(time));
				if (resultMap.containsKey(fieldIndex)) {
					List<Object> devList = (List) resultMap.get(fieldIndex);
					devList.add(dev);
				}
				else {
					List<Object> devList = new ArrayList<>();
					devList.add(dev);
					resultMap.put("" + fieldIndex, devList);
				}
			}
		}
		webDevData = resultMap;
	}

	private void refreshWebGetDevTypeList() {
		List<Map<String, Object>> typeList =
				dao.query("SELECT D.DEVTYPEID,D.DEVTYPENAME,D.PARAMNAME,D.MAX,D.MIN FROM DEVICETYPE D ORDER BY D.DEVTYPEID".toUpperCase());
		devTypeList = new ArrayList<>();
		if (!ObjectUtils.isEmpty(typeList)) {
			for (Map<String, Object> devType : typeList) {
				Map<String, Object> devTypeMap = new HashMap<>();
				int devTypeIndex = (int) devType.get("devTypeId".toUpperCase());
				String devTypeName = (String) devType.get("devTypeName".toUpperCase());
				String paramName = (String) devType.get("paramName".toUpperCase());
				double max = (double) devType.get("max".toUpperCase());
				double min = (double) devType.get("min".toUpperCase());
				devTypeMap.put("devTypeIndex", devTypeIndex);
				devTypeMap.put("paramName", paramName);
				devTypeMap.put("devTypeName", devTypeName);
				devTypeMap.put("max", max);
				devTypeMap.put("min", min);
				devTypeList.add(devTypeMap);
			}
		}
	}

	private void refreshWebDeviceList() {
		deviceList = new ArrayList<>();
		String sql = "SELECT devid,devname,devtypeid,devdesc,devlocate,username,devpower,"+
				"c0_devname,c0_devtypeid,c0_fieldid,c0_max,c0_min"+
				",c1_devname,c1_devtypeid,c1_fieldid,c1_max,c1_min"+
				",c2_devname,c2_devtypeid,c2_fieldid,c2_max,c2_min"+
				",c3_devname,c3_devtypeid,c3_fieldid,c3_max,c3_min"+
				",c4_devname,c4_devtypeid,c4_fieldid,c4_max,c4_min"+
				",c5_devname,c5_devtypeid,c5_fieldid,c5_max,c5_min"+
				",c6_devname,c6_devtypeid,c6_fieldid,c6_max,c6_min"+
				",c7_devname,c7_devtypeid,c7_fieldid,c7_max,c7_min "+
				"FROM devices ORDER BY devid".toUpperCase();
		List devList = dao.query(sql);
		if (!ObjectUtils.isEmpty(devList)) {
			for (int i = 0; i <= devList.size()-1; i++) {
				Map<String, Object> dev = (Map) devList.get(i);
				int devIndex = (int) dev.get("devid".toUpperCase());
				int devTypeIndex = (int) dev.get("devTypeId".toUpperCase());
				String devName = (String) dev.get("devName".toUpperCase());
				String devLocate = (String) dev.get("devLocate".toUpperCase());
				String devPower = (String) dev.get("devpower".toUpperCase());
				String devdesc = (String) dev.get("devdesc".toUpperCase());
				dev.put("devIndex", devIndex);
				dev.put("devName", devName);
				dev.put("devLocate", devLocate);
				dev.put("devPower", devPower);
				dev.put("devDesc", devdesc);
				dev.put("devTypeIndex", devTypeIndex);
				List<Object> channelDevList = new ArrayList<>();
				for (int d = 0; d <=7; d++) {
					Map<String, Object> channelDevMap = new HashMap<>();
					int channelTypeIndex = (int) dev.get(("c"+d+"_devtypeid").toUpperCase());
					int channelDevIndex = devIndex*100 + d;
					int fieldIndex = (int) dev.get(("c" + d + "_fieldid").toUpperCase());
					double max = Double.parseDouble(dev.get("c" + d + "_max") + "");
					double min = Double.parseDouble(dev.get("c" + d + "_min") + "");
					channelDevMap.put("devIndex", channelDevIndex);
					channelDevMap.put("devTypeIndex", channelTypeIndex);
					channelDevMap.put("fieldIndex", fieldIndex);
					channelDevMap.put("max", max);
					channelDevMap.put("min", min);
					channelDevList.add(channelDevMap);
				}
				dev.put("channelDevList", channelDevList);
				deviceList.add(dev);
			}
		}
	}

	private void refreshWebUserList() {
		auserList = new ArrayList<>();
		List userList = dao.query("SELECT userid,name,nickName,role,tel,recvWarn,enable,group_concat(s1.fieldid) fieldList FROM users s left join uservsfield s1 on s1.userName = s.name where 1=1 group by s.name".toUpperCase());
		if (!ObjectUtils.isEmpty(userList)) {
			for (int i = 0; i <= userList.size() - 1; i++) {
				Map<String, Object> auser = new HashMap<>();
				Map<String, Object> user = (Map) userList.get(i);
				int userId = (int) user.get("userid".toUpperCase());
				String userName = (String) user.get("name".toUpperCase());
				String nickName = (String) user.get("nickName".toUpperCase());
				int role = (int) user.get("role".toUpperCase());
				String tel = (String) user.get("tel".toUpperCase());
				int recvWarn = (int) user.get("recvWarn".toUpperCase());
				int enable = (int) user.get("enable".toUpperCase());
				String fieldList = valueForKey(user, "fieldList".toUpperCase());
				auser.put("userId", userId);
				auser.put("userName", userName);
				auser.put("nickName", nickName);
				auser.put("role", role);
				auser.put("tel", tel);
				auser.put("recvWarn", recvWarn);
				auser.put("enable", enable);
				auser.put("fieldList", fieldList.split(","));
				auserList.add(auser);
			}
		}
	}

	static String valueForKey(Map data, String key) {
		if (data.containsKey(key)) {
			String value = (String) data.get(key);
			return StringUtils.defaultIfEmpty(value, "");
		}
		return "";
	}
}
