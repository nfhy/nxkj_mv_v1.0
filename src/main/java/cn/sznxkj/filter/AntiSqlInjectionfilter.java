package cn.sznxkj.filter;

import java.io.IOException; 
import java.io.PrintWriter;
import java.util.Enumeration; 

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest; 
import javax.servlet.ServletResponse; 
import javax.servlet.http.HttpServletRequest; 
import javax.servlet.http.HttpServletResponse;

public class AntiSqlInjectionfilter implements Filter { 
 
	public  String 		badStr;
	public  String[] 		badStrs;
	
   public void destroy() {
       // TODO Auto-generated method stub    
	   } 
     
   public void init(FilterConfig arg0) throws ServletException { 
       //初始化过滤串
	   String sqlBadStr = 
    	   "'| and|exec|insert|select|delete|update|count|*|chr| mid|master|truncate|" + 
           "char|declare|sitename|net user|xp_cmdshell|;| or |-|+|,| like | create | drop |" + 
           "table| from| grant| use |group_concat|column_name|" + 
           "schema|union|where|order| by|" + 
           "%|#|database |view|account|password";//  /|
       
       
       //xss
       String  xssBadStr="|'|<|>|(|)|script";
       badStr=sqlBadStr+xssBadStr;
       badStrs = badStr.split("\\|"); 
	 } 
     
   public void doFilter(ServletRequest args0, ServletResponse args1, FilterChain chain) throws IOException, ServletException { 
       HttpServletRequest req=(HttpServletRequest)args0; 
       HttpServletResponse res=(HttpServletResponse) args1;
       
       String url = req.getRequestURI();
       Enumeration params = req.getParameterNames(); 
       String sql = ""; 
       while (params.hasMoreElements()) { 
           String name = params.nextElement().toString(); 
           String[] value = req.getParameterValues(name); 
           for (int i = 0; i < value.length; i++) { 
               sql = sql + value[i]; 
           } 
       } 
       if (sqlValidate(sql)) { 
    	   System.out.println("您发送请求中的参数中含有非法字符"+sql);
    	   PrintWriter writer=res.getWriter();
    	   writer.print("<font color='red'>您发送请求中的参数中含有非法字符</font>");
    	   writer.close();
    	  // throw new IOException("您发送请求中的参数中含有非法字符"+sql); 
           } else {
        	  // System.out.println("in");
        	 /*  Cookie[] cookies=req.getCookies();
        	   if(null!=cookies&&1<=cookies.length&&null!=cookies[0]){
        		   Cookie cookie=cookies[0];
        		   StringBuilder builder=new StringBuilder();
        		   Calendar cal=Calendar.getInstance();
        		   cal.add(Calendar.HOUR, 1);
        		   builder.append(cookie.getName())
        		   .append("=")
        		   .append(cookie.getValue())
        		   .append(";HttpOnly;");
        		  // .append("Expires=")
        		 //  .append(sdf.format(cal.getTime()));
        		   
        		  // System.out.println(builder.toString());
        		   res.setHeader("Set-Cookie", builder.toString());
        		  // res.setHeader("Set-Cookie", "B=2;HttpOnly");
        	   }*/
        	   chain.doFilter(args0,args1); 
           } 
       }
   
     
   //效验     
   protected  boolean sqlValidate(String str) { 
       str = str.toLowerCase();
       //统一转为小写      
       for (int i = 0; i < badStrs.length; i++) { 
           if (str.indexOf(badStrs[i]) >= 0) { 
        	   System.out.println(badStrs[i]);
               return true; 
           } 
       } 
       return false; 
   } 
	
} 
