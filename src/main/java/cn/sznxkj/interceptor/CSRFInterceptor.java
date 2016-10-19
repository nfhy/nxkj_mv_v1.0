package cn.sznxkj.interceptor;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;


public class CSRFInterceptor implements HandlerInterceptor {

public static String _TOKEN="CSRF_TOKEN";
	
	
	public String generateToken()
	{
		String token=UUID.randomUUID().toString();
		return token;
	}
	
	public boolean checkToken(HttpServletRequest request)
	{
		
		String token0=request.getParameter(_TOKEN);
		if(token0==null)
			return false;
		String token1=(String)request.getSession().getAttribute(_TOKEN);
		if(token0.compareTo(token1)!=0)
			return false;
		else
			return true;
	}
	@Override
	public void afterCompletion(HttpServletRequest arg0,
			HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler, ModelAndView modelAndView) throws Exception {
		
		if(modelAndView!=null){
			ModelMap modelMap=modelAndView.getModelMap();
			String token=generateToken();
			modelMap.put(_TOKEN, token);
			request.getSession().setAttribute(_TOKEN, token);
		}
	}

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		// TODO Auto-generated method stub
		//登陆 登出  图片验证码 都进行过滤
		/*if(handler.getClass()==KaoShengController.class||handler.getClass()==LoginController.class||handler.getClass()==UserController.class)
			return true;
		else if(!checkToken(request))
			return false;
		return true;*/
		return true;
	}

}
