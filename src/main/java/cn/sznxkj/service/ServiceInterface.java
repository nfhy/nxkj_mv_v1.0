package cn.sznxkj.service;

import java.util.Map;

public interface ServiceInterface {

	Map queryLogin(Map data);

	Map logOut(Map data);

	Map queryWebGetDevTypeList();

	Map updateFieldMgr(Map data);

	Map updateWebModifyField(Map data);

	Map queryWebDeviceList();

	Map queryWebUserList();

	Map updateUserMgr(Map data);

	Map queryWebHistory(Map data);

	Map queryWebField(String userName);

	Map queryWebDevData(String userName);

	Map queryAppFields(String userName);

	Map updateAppUserMgr(Map data);
	
	Map updateJpushId(Map data);

	Map resetPwd(Map data);

	Map changePwd(Map data);

}
