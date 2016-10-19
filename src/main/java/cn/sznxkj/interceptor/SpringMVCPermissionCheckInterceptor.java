/**
 * 
 */
package  cn.sznxkj.interceptor;


import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import cn.sznxkj.constants.Constants;
import cn.sznxkj.utils.SystemCacheMgr;

/**
 * @author walulu edwin
 *
 */
public class SpringMVCPermissionCheckInterceptor implements HandlerInterceptor{	
	private static final String USER_PERMISSION_SESSION = Constants.USER_PERMISSION_SESSION;
	
    private int OPTR_TYPE=0;
	private static final String ERROR_RESULT = "error";
	public static final String MGR = "MGR";
	public static final String KS = "KS";
	private static final String _dispatcher_URL ="/sessionError.jsp?userType=";
	private static Map functionMap = null;
	private final static String[] noChecks={"checkValidCode.do","refreshCache.do","/login","/logout","selXtreeDlg","loadXtree"
		,"zytb/ks.do","kslogin"};
	
	
	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
	}

	
	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}

	/**
	 * 主要逻辑代码搬运自com.hyt.core.interceptor.PermissionCheckInterceptor,这里进行了适应springMVC的修改
	 * @author walulu
	 */
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
//		LogUtil.log4Platform(request, false,null);
		String contextPath = request.getContextPath().toString();
		String reqURL = request.getRequestURI().toString();
		reqURL = reqURL.substring(contextPath.length());
		
		String reqIP = request.getRemoteAddr();
		
		// 判断请求的URL中有没有.action的字符串，如果没有则不做权限验证
		if (reqURL.indexOf(".do") != -1) {
			
			reqURL = reqURL.substring(0,reqURL.indexOf(".do"))+".do";
			if (!reqURL.startsWith("/")){
				reqURL = "/" + reqURL;
			}
			
			if (!isOpenReqUrl(reqURL))
			{
			    HttpSession session=request.getSession();
			    Object loginObj = session.getAttribute(Constants.LOGIN_USER);
				if (loginObj == null) {
					// 判断session中是否有User的存在，如果没有则返回一个HTTP forbidden的页面
					//req.setAttribute("sessionTimeoutURL",((HttpServletRequest) req).getRequestURL().toString());					
					Object loginType=request.getParameter("loginType");
					String dispatcher_URL=null;
					if(loginType==null){
						dispatcher_URL=_dispatcher_URL+MGR;
					}else{
						dispatcher_URL=_dispatcher_URL+KS;
					}
	            	request.getRequestDispatcher(dispatcher_URL).forward(request, response);
	            	return false;
				}else{
					String roleCode=(String) session.getAttribute("roleCode");
					if("admin".equals(roleCode)||"mainMgr".equals(roleCode)||reqURL.contains("commonExport")){}
					else{
						return SystemCacheMgr.checkRoleMenu(roleCode, reqURL.substring(1,reqURL.lastIndexOf("/")));
					}
				}
			}
		}
		return true;
	}
	
	private boolean isOpenReqUrl(String reqUrl){
		for(int i=0,size=noChecks.length-1;i<=size;i++){
			if(reqUrl.indexOf(noChecks[i])>=0){
				return true;
			}
		}
		return false;
	}
	
}
