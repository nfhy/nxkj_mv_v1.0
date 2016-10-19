package  cn.sznxkj.utils;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

/**
 * created at 2011-9-28
 * 远程内容操作组件
 * @author libx
 *
 */
public class RemoteUtil {
	
	/**
	 * 判断指定URL是否连通
	 * @param url
	 * @return true/false
	 * eg.
	 * <code>
	 *    boolean isConn = new RemoteUtil().isConnected("http://192.168.20.247:7001/hyt");
	 * </code>
	 */
	public static boolean isConnected(String url) {
		boolean flag = false;

    	try {
    		URL urlStr = new URL(url);
    		HttpURLConnection connection = (HttpURLConnection) urlStr.openConnection();
    		int state = connection.getResponseCode();
    		if (state == HttpURLConnection.HTTP_OK) {
    			flag = true;
    		}
	    } catch (Exception ex) {
		    //System.out.println("loop :" + counts);
	    }	
        return flag;
	}
	
	public static String getIpAddr(HttpServletRequest request) {
		String ipAddress = null;
		ipAddress = request.getHeader("x-forwarded-for");
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();
			if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1") ) {
				// 根据网卡取本机配置的IP
				InetAddress inet = null;
				try {
					inet = InetAddress.getLocalHost();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				ipAddress = inet.getHostAddress();
			}
		}
		// 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
		if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
															// = 15
			if (ipAddress.indexOf(",") > 0) {
				ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
			}
		}
		return ipAddress;
	}
}
