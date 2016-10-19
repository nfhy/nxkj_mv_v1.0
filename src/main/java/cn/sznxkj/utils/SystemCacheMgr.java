/**
 * 
 */
package  cn.sznxkj.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * @Title EasyExamManage 考试管理系统
 * @Copyright Copyright (c) 1997-2013
 * @Company SHENZHEN SEASKYLAND TECHNOLOGIES CO.,LTD
 * @Author 刘红彬 edwin
 * @File SystemCacheMgr.java
 * @Date 2013-1-10 下午 上午09:56:02
 * @Version 1.0.0.0
 * @Description 
 */
public class SystemCacheMgr {
	
	public static List getCodeListByDwr(String codeName, List params)
	{
		return getCodeList(codeName, params);
	}
	/**
	 * 取tsys_cache_code或tsys_parm_data中的数据
	 * @param codeName
	 * @param params
	 * @return
	 */
	public static List getCodeList(String codeName, List params)
	{
		List list = null;
		
		return list;
	}
	
	public static String getNameByCodeValue(String codeName,String codeValue)
	{
		HashMap hash =  getCodeHash(codeName);
		String cname="";
		if(hash!=null)
		{
			List slist =(List)hash.get(codeValue);
			if(slist==null||slist.size()<=0){
				cname=codeValue;
			}else{
				cname=slist.get(1).toString();
			}
		}
		
		return cname;
	}

	
	
	/**
	 * 无参数取tsys_cache_code或tsys_parm_data中的数据
	 * @param codeName
	 * @return
	 */
	public static List getCodeList(String codeName)
	{

		return getCodeList(codeName, null);
	}
	
	/**
	 * 取tsys_cache_code或tsys_parm_data中的hash数据
	 * @param codeName
	 * @param params
	 * @return
	 */
	public static HashMap getCodeHash(String codeName, List params)
	{
		HashMap hash = null;
		
		return hash;
	}
	
	/**
	 * 无参数取tsys_cache_code或tsys_parm_data中的Hash数据
	 * @param codeName
	 * @return
	 */
	public static HashMap getCodeHash(String codeName)
	{

		return getCodeHash(codeName, null);
	}
	
	/**
	 * 根据表对象名称取对象列表
	 * @param className
	 * @return
	 */
	public static List getCodeListByClassName(Class className){
		List list = null;
		
		return list;
	}	
	
	/**
	 * 根据className取对象列表
	 * @param className String类型
	 * @return
	 */
	public static List getCodeListByClassName(String className){
		List list = null;
		
		return list;
	}
	

    /**
     * 根据cache中的Key去清除缓存
     * @param cacheKey
     */
    public static void flushCacheByCacheKey(String cacheKey){
		
    }    
    

    public static void flushCacheByClass(Class classz){
		
    } 
    
    public static void flushAllCache(){
		
    }
    
	public static List getObjectList(Class clazz){
		return getObjectList(clazz,null);
	}
	
	public static List getObjectList(Class clazz,String order){
		List returnList = new ArrayList();
		
		return returnList;			
	}
	
	public static Map getMap(Class clazz){
		return getMap(clazz,null,null);
	}
	
	public static Map getMap(Class clazz,String[] mapKey){
		return getMap(clazz,null,mapKey);
	}
	
	public static Map getMap(Class clazz,String order,String[] mapKey){
		Map returnMap = new HashMap();
		
		return returnMap;	
	}
	
	public static String getConfigValue(String configName){
		return "";
	}
	
	public static Date getEndDate(){
		return null;
	}
	
	public static void put(String key,Object obj){
	}
	
	public static Object get(String key){
		return null;
	}
	
	public static boolean checkRoleMenu(String role,String menu){
		return true;
	}
	
}
