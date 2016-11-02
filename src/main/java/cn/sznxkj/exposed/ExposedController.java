package cn.sznxkj.exposed;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.sznxkj.service.MyService;
@Scope("prototype")
@Controller("exposedController")
public class ExposedController {

	private static Map<String, Map<String, Object>> tokenMap = new HashMap<>();
	@Autowired
	private MyService service;

	@RequestMapping("/input.do")
	@ResponseBody
	public void input(HttpServletRequest req, HttpServletResponse res, @RequestBody String json) throws Exception {
		/*
		res.setContentType("text/html;charset=UTF-8");
	    res.addHeader("Access-Control-Allow-Origin","*");//'*'表示允许所有域名访问，可以设置为指定域名访问，多个域名中间用','隔开
	          //如果IE浏览器则设置头信息如下
	    if("IE".equals(req.getParameter("type"))){
	    	res.addHeader("XDomainRequestAllowed","1");
	    }*/
		String msg = "";
		String token = "";
		String userName = "";
		String reMsg = "";
		Map<String, Object> data = null;
		Map<String, Object> reData = new HashMap<>();
		if (StringUtils.isEmpty(json)) {
			reMsg = "请求数据无效";
		}
		else {
			json = URLDecoder.decode(json, "utf-8");
			json = json.substring(0, json.length()-1);
			JSONObject jsono = JSONObject.fromObject(json);
			msg = jsono.getString("msg");
			data = (Map) jsono.get("data");
			userName = data.containsKey("userName")?(String)data.get("userName"):"";
			token = data.containsKey("token")?(String)data.get("token"):"";
			if (StringUtils.isEmpty(msg) || ObjectUtils.isEmpty(data)) {
				reMsg = "请求数据无效";
			}
			else if (!"login".equals(msg)) {
				Map<String, Object> userMap = tokenMap.get(userName);
				if (ObjectUtils.isEmpty(userMap) || !token.equals(userMap.get("token"))) {
					reMsg = "会话超时，请重新登录";
					reData.put("resCode", "100013");
				}
			}
		}
		if (StringUtils.isEmpty(reMsg) && checkRoleRight(msg, userName)) {
			try{
				System.out.println(msg);
				switch (msg) {
					case "login": {
						reData = service.queryLogin(data);
						if ("0".equals(reData.get("resCode"))) {
							userName = (String) data.get("userName");
							tokenMap.put(userName, reData);
						}
						break;
					}
					case "logOut": {
						reData = service.logOut(data);
						if ("0".equals(reData.get("resCode"))) {
							userName = (String) data.get("userName");
							tokenMap.put(userName, reData);
						}
						break;
					}
					case "webGetDevTypeList" : {
						reData = service.queryWebGetDevTypeList();
						break;
					}

					case "webField" : {
						reData = service.queryWebField(userName);
						break;
					}
					case "fieldMgr" : {
						reData = service.updateFieldMgr(data);
						break;
					}
					case "webModifyField" : {
						reData = service.updateWebModifyField(data);
						break;
					}
					case "webDeviceList" : {
						reData = service.queryWebDeviceList();
						break;
					}
					case "devMgr" : {
						reData = service.updateDevMgr(data);
						break;
					}
					case "webUserList" : {
						reData = service.queryWebUserList();
						break;
					}
					case "userMgr" : {
						reData = service.updateUserMgr(data);
						break;
					}
					case "webDevData" : {
						reData = service.queryWebDevData(userName);
						break;
					}
					case "webHistory" : {
						reData = service.queryWebHistory(data);
						break;
					}
					case "appFields" : {
						reData = service.queryAppFields(userName);
						break;
					}
					case "appUserMgr" : {
						reData = service.updateAppUserMgr(data);
						break;
					}
					case "jpushId" : {
						reData = service.updateJpushId(data);
						break;
					}
					case "resetPwd" : {
						reData = service.resetPwd(data);
						break;
					}
					case "changePwd" : {
						reData = service.changePwd(data);
						break;
					}
					default : {
						break;
					}
				}
			}
			catch(Exception e) {
				reMsg = "服务器异常";
				e.printStackTrace();
				System.out.println(e.getMessage());
			}
		}
		else {
			if (StringUtils.isEmpty(reMsg)) {
				reMsg = "越权操作";
				reData.put("resCode", "100015");
			}
		}
		Map<String, Object> reMap = new HashMap<>();
		reMap.put("msg", msg);
		reMap.put("reMsg", reMsg);
		reMap.put("data", reData);
		res.setContentType("text/html;charset=UTF-8");
		res.getWriter().write(JSONObject.fromObject(reMap).toString());
		return;
	}
	private static String[] rights ={"devMgr", "userMgr"};
	private boolean checkRoleRight(String msg, String userName) {
		boolean isadmin = "admin".equals(userName);
		if (!isadmin) {
			for (String right : rights) {
				if (right.equals(msg)) {
					return false;
				}
			}
		}
		return true;
	}
}
