package cn.sznxkj.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import cn.sznxkj.service.MyService;

public class InitServlet extends HttpServlet {
	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		WebApplicationContext wac=ContextLoader.getCurrentWebApplicationContext();
		final MyService kaoShengService= (MyService) wac.getBean("myService");
		new Thread(new Runnable() {

			@Override
			public void run() {
			}
			
		}).start();
	}

}
