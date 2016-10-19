/**
 * 
 */
package  cn.sznxkj.servlet;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cn.sznxkj.constants.Constants;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * @Title EasyExamManage 考试管理系统
 * @Copyright Copyright (c) 1997-2012
 * @Company SHENZHEN SEASKYLAND TECHNOLOGIES CO.,LTD
 * @Author 刘红彬 edwin
 * @File ValidateCodeServlet.java
 * @Date 2012-12-3 下午 下午04:28:47
 * @Version 1.0.0.0
 * @Description 
 */
public class ValidateCodeServlet extends HttpServlet {
	public void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException{		
		response.setContentType("image/jpeg");

		// 设置页面不缓存
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);

		// 在内存中创建图象
		int width = 60, height = 20;
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);

		// 获取图形上下文
		Graphics g = image.getGraphics();

		// 生成随机类
		Random random = new Random();

		// 设定背景色
		g.setColor(getRandColor(200, 250));
		g.fillRect(0, 0, width, height);

		// 设定字体
		g.setFont(new Font("Times New Roman", Font.PLAIN, 18));

		// 画边框
		// g.setColor(new Color());
		// g.drawRect(0,0,width-1,height-1);

		// 随机产生155条干扰线，使图象中的认证码不易被其它程序探测到
		g.setColor(getRandColor(160, 200));
		for (int i = 0; i < 155; i++) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			int xl = random.nextInt(12);
			int yl = random.nextInt(12);
			try {
				g.drawLine(x, y, x + xl, y + yl);
			} catch (Exception e) {
				//
			}
		}

		// 取随机产生的认证码(4位数字)
		String sRand = "";

		String tmpRand = "abdfghjkmnprstuvwxyABDEFGHJKLMNPQRSTUVWXY345678";

		for (int i = 0; i < 4; i++) {
			String rand = String.valueOf(tmpRand.charAt(Math.round((tmpRand
					.length() - 1)
					* random.nextFloat())));

			sRand += rand;
			// 将认证码显示到图象中
			g.setColor(new Color(20 + random.nextInt(110), 20 + random
					.nextInt(110), 20 + random.nextInt(110)));
			// 调用函数出来的颜色相同，可能是因为种子太接近，所以只能直接生成
			g.drawString(rand, 13 * i + 6, 16);
		}

		// 将认证码存入SESSION
		request.getSession().setAttribute(Constants.VALIDATE_CODE, sRand);

		// 图象生效
		g.dispose();

		// 输出图象到页面
		//ImageIO.write(image, "JPEG", response.getOutputStream());
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(response.getOutputStream()); 
		encoder.encode(image);
	}
	
	public void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException 
	{
		doGet(request,response);
	}
	
	private Color getRandColor(int fc, int bc) {// 给定范围获得随机颜色
		Random random = new Random();
		if (fc > 255)
			fc = 255;
		if (bc > 255)
			bc = 255;
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}
}
