package dtstack.san.spring.webmvc;

import dtstack.san.spring.webmvc.servlet.ModelAndView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
//设计这个类的主要目的，将一个静态的模板文件转化成动态的文件
//将一个静态文件传递的参数不同，而产生不同的结果
//最终输出字符串，交给Response输出
public class ViewResolver {
    public String getViewName;
    private String viewName;
    private File templateFile;

    public ViewResolver(String name, File template) {
        this.viewName = name;
        this.templateFile = template;
    }

    public String viewResolver(ModelAndView mv) throws IOException {
        StringBuffer sb = new StringBuffer();
        RandomAccessFile ra = new RandomAccessFile(this.templateFile,"r");
        String line = null;

        while (null != (line = ra.readLine())) {
            Matcher m = matcher(line);
            while (m.find()) {
                for (int i = 1; i <=m.groupCount(); i++) {
                    //把￥{}中中间的字符串取出来
                    String paramName = m.group(i);
                    Object paramValue = mv.getModel().get(paramName);
                    if (paramValue == null) {
                        continue;
                    }
                    line = line.replaceAll("&\\{"+paramName + "\\}", paramValue.toString());
                }
            }
            sb.append(line);
        }
        return sb.toString();
    }

    private Matcher matcher(String str) {
        Pattern pattern = Pattern.compile("&\\{(.+?)\\}", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return matcher;
    }

    public String getGetViewName() {
        return getViewName;
    }

    public void setGetViewName(String getViewName) {
        this.getViewName = getViewName;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public File getTemplateFile() {
        return templateFile;
    }

    public void setTemplateFile(File templateFile) {
        this.templateFile = templateFile;
    }
}
