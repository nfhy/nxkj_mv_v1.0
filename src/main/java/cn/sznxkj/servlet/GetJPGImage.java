package  cn.sznxkj.servlet;

import java.awt.image.ImageProducer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import cn.sznxkj.utils.RemoteUtil;

import com.sun.jimi.core.Jimi;
import com.sun.jimi.core.JimiException;
import com.sun.jimi.core.JimiWriter;
import com.sun.jimi.core.options.JPGOptions;

public class GetJPGImage extends HttpServlet {

	public void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException 
	{
		boolean deleteFlag = true;
        //通过request中的参数取得file   
        String img=request.getParameter("img");  

        response.setContentType("image/JPEG");
        OutputStream out = response.getOutputStream();
        String path = request.getRealPath("/");
        String jpg = this.tif2Jpg(img, path, 75);
        if (jpg == null) {
        	jpg="images//main//error.jpg";
        	deleteFlag = false;
        }
        File file = new File(path + jpg);
        FileInputStream fis = new FileInputStream(file);
        
        byte[] b = new byte[1024];
        int len = -1;
        while ((len = fis.read(b, 0, 1024)) != -1) {
        	out.write(b, 0, len);
        }
        out.flush();
        out.close();
        fis.close();
        
        //显示后删除临时生成的jpg文件
        if (deleteFlag) {
	        boolean flag = false;
	        if (file.exists() && file.isFile()) {
	        	flag = file.delete();
	        }
        }
	}
	
	public void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException 
	{
		doGet(request,response);
	}
	
	
    /**
     * 转换图像格式为 JPG
     * @param    String : url, 其它格式的源图像文件路径
     * @param    String : rootPath,根目录
     * @param    int    : nQuality, 品质, 0-100, 数值越高品质越好
     * @return   String ：返回JPG文件路径，null表示文件不存在
     */
    private String tif2Jpg(String url,String rootPath, int nQuality)
    {
        if (url == null || url.trim().equals(""))
        {
            //System.out.println(" @> GetJPGImage.tif2Jpg() : 要转换的源图像文件路径不能为空！");
            return null;
        }
        //判断指定URL的文件是否存在
        if (!RemoteUtil.isConnected(url)) {
        	return null;
        }

        String path = rootPath + "core//jpg//";
        File f = new File(path);
        f.mkdirs();
        String sDestImage = "core//jpg//" + url.substring(url.lastIndexOf("/")+1, url.lastIndexOf(".")) + ".jpg";

        try
        {
            //long lRunStartTime = System.currentTimeMillis();        	
            JPGOptions tJPGOptions = new JPGOptions();
            if (nQuality < 0 || nQuality > 100)
            {
                tJPGOptions.setQuality(75);
            }
            else
            {
                tJPGOptions.setQuality(nQuality);
            }
            
            URL uri = new URL(url);
            ImageProducer tImageProducer = Jimi.getImageProducer(uri);

            JimiWriter tJimiWriter = Jimi.createJimiWriter(rootPath + sDestImage);
            tJimiWriter.setSource(tImageProducer);
            tJimiWriter.setOptions(tJPGOptions);
            tJimiWriter.putImage(rootPath + sDestImage);
            tImageProducer = null;
            tJimiWriter = null;
            tJPGOptions = null;
            //long lRunEndTime = System.currentTimeMillis();
            //long lRunTime = lRunEndTime - lRunStartTime;
            //System.out.println(" @> GetJPGImage.tif2Jpg() 运行时间 : " + lRunTime + " 毫秒");
        }
        catch (JimiException je)
        {
            //System.out.println(" @> GetJPGImage.tif2Jpg() : 转换图像格式发生异常！");
            je.printStackTrace();
            return null;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }

        return sDestImage;
    }
}
