package cn.sznxkj.interceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;



public class CsrfIntercepter implements HandlerInterceptor {     
public  static final String CSRFNUMBER = "csrfToken";

private static final String[] exceptions=new String[]{"shuJuShu","/kslogin/index.do","/kslogin/login.do","KaoShengDengLu/denglu.do","/login/","/logout/","kscache","KaoShengDengLu/index.do","kaoShengZhuCe","appModule/appModuleTree.do"};

    public boolean preHandle(HttpServletRequest request,HttpServletResponse response, Object handler) throws Exception {
    	/*
    	 * 排除不需要过滤的类
    	 */
    	if(checkException(request.getServletPath())){
    		return true;
    	}
        String keyFromRequestParam = (String) request.getParameter(CSRFNUMBER);
        //String keyFromRequestParam =(String) request.getSession().getAttribute("csrfToken");
        String keyFromCookies=""; 
        boolean result=false;
        Cookie[] cookies = request.getCookies(); 
        if(cookies!=null){
            for (int i = 0; i < cookies.length; i++) {    
                String name = cookies[i].getName(); 
                if(CSRFNUMBER.equals(name) ) {    
                    keyFromCookies= cookies[i].getValue();    
                }    
            }
        }
       
        if((keyFromRequestParam!=null&& keyFromRequestParam.length()>0&&
                keyFromRequestParam.equals(keyFromCookies) &&
                keyFromRequestParam.equals((String)request.getSession().getAttribute(CSRFNUMBER)))) { 
            result=true;
        }else{
            request.getRequestDispatcher("/400error.html").forward(request, response);
        }
        
        return result;
    }
    
    public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1,
            Object arg2, Exception arg3) throws Exception {
        
    }
    
    public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1,
            Object arg2, ModelAndView arg3) throws Exception {
        
    }
    
    private static boolean checkException(String url){
    	for(String exce : exceptions){
    		if(url.indexOf(exce)>=0){
    			return true;
    		}
    	}
    	return false;
    }
    
}
