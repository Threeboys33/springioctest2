package dtstack.san.spring.framework.webmvc.servlet;

import dtstack.san.spring.framework.annotation.Controller;
import dtstack.san.spring.framework.annotation.RequestMapping;
import dtstack.san.spring.framework.annotation.RequestParam;
import dtstack.san.spring.framework.aop.AopProxy;
import dtstack.san.spring.framework.aop.ProxyUtils;
import dtstack.san.spring.framework.context.SanApplicationContext;
import dtstack.san.spring.webmvc.HandlerAdapter;
import dtstack.san.spring.webmvc.HandlerMapping;
import dtstack.san.spring.webmvc.ViewResolver;
import dtstack.san.spring.webmvc.servlet.ModelAndView;
import org.springframework.aop.framework.AopProxyUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
public class DispatchServlet extends HttpServlet {
    private final String LOCATION = "contextConfigLocation";

    private List<HandlerMapping> handlerMappings = new ArrayList<>();
    //
    private Map<HandlerMapping,HandlerAdapter> handlerAdapters = new HashMap<>();
    private List<ViewResolver> viewResolvers = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //         // String url = req.getRequestURI();
        // String contextPath = req.getContextPath();
        // url = url.replace(contextPath, "").replaceAll("/+", "/");
        try {
            doDisptcher(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception,Details:\r\n" +
                    Arrays.toString(e.getStackTrace())
                            .replaceAll("\\[\\]","")
                            .replaceAll("\\s","\r\n"));
        }
    }

    private void doDisptcher(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        //根据用户请求的url来获得一个Handler
        HandlerMapping handler = getHandler(req);

        if (handler == null) {
            resp.getWriter().write("404 Not Found");return;
        }
        HandlerAdapter ha = getHandlerAdapter(handler);

        ModelAndView mv = ha.handle(req,resp,handler);

        processDispathResult(resp,mv);
    }

    private void processDispathResult(HttpServletResponse resp, ModelAndView mv) throws IOException {
        //调用ViewResolver的viewResolveName
        if (null == mv) {
            return;
        }

        if (this.viewResolvers.isEmpty()) {
            return;
        }
        for (ViewResolver viewResolver : viewResolvers) {
            if (!mv.getViewName().equals(viewResolver.getViewName())) {
                continue;
            }
            String out = viewResolver.viewResolver(mv);
            if (out != null) {
                resp.getWriter().write(out);
                break;
            }
        }


    }

    private HandlerAdapter getHandlerAdapter(HandlerMapping handler) {
        if (this.handlerAdapters.isEmpty()) {
            return null;
        }
        return this.handlerAdapters.get(handler);
    }

    private HandlerMapping getHandler(HttpServletRequest req) {
        if (this.handlerMappings.isEmpty()) {
            return null;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+","/");
        for (HandlerMapping handler : this.handlerMappings) {
            Matcher matcher = handler.getUrl().matcher(url);
            if (!matcher.matches()) {
                continue;
            }
            return handler;
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        SanApplicationContext context = new SanApplicationContext(config.getInitParameter(LOCATION));
        initStrategies(context);

    }

    protected void initStrategies(SanApplicationContext context) {
        initMultipartResolver(context);
        initLocaleResolver(context);
        initThemeResolver(context);
        initHandlerMappings(context);
        initHandlerAdapters(context);
        initHandlerExceptionResolvers(context);
        initRequestToViewNameTranslator(context);
        initViewResolvers(context);
        initFlashMapManager(context);
    }

    //主要讲controller的url配置和methode进行一一对应
    private void initHandlerMappings(SanApplicationContext context) {
        String[] beanNames = context.getBeanDefinitionNames();
        //收线从容器中取到所有的实例
        for (String beanName : beanNames) {
            //到了mvc层，堆外提供的方法只有一个getbean方法
            //返回的对象不是BeanWrapper，怎么办？
            //返回的是一个proxy对象，proxy对象么办法获取注解信息
            Object proxy = context.getBean(beanName);
            //通过proxy对象里面的原生对象获取原生对象上的注解信息
            Object controller = ProxyUtils.getTargetObject(proxy);
            //对原生对象进行判断
            Class<?> clazz = controller.getClass();
            if (!clazz.isAnnotationPresent(Controller.class)) {continue;}
            String baseUrl = "";

            if (clazz.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping annotation = clazz.getAnnotation(RequestMapping.class);
                baseUrl = annotation.value();
            }

            //扫描所有的public方法
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(RequestMapping.class)) {
                    continue;
                }

                RequestMapping req = method.getAnnotation(RequestMapping.class);
                String regex = ("/" + baseUrl + req.value().replaceAll("\\*",".*")).replaceAll("/+", "/");
                Pattern compile = Pattern.compile(regex);
                this.handlerMappings.add(new HandlerMapping(compile, controller, method));
                System.out.println("Mapping " + regex + "," + method);
            }
        }

    }
    private void initHandlerAdapters(SanApplicationContext context) {
        //在初始化阶段就是将这些参数的名字或者类型按一定的顺序保存下来
        //因为后边用反射调用的时候传递的形参是一个数组
        //可以通过记录参数的位置index，按个从数组中填值，这样就和参数的顺序无关
        for (HandlerMapping handlerMapping : this.handlerMappings) {
            //每一个方法有一个参数列表
            Map<String, Integer> paramMapping = new HashMap<>();
            //一个参数可以加多个Annotation
            Annotation[][] pa = handlerMapping.getMethod().getParameterAnnotations();
            for (int i = 0; i < pa.length; i++) {
                for (Annotation a : pa[i]) {
                    if (a instanceof RequestParam) {
                        String paraName = ((RequestParam) a).value();
                        if(!"".equals(paraName.trim())){
                            paramMapping.put(paraName, i);
                        }
                    }
                }
            }

            //接下来处理非命名参数
            Class<?>[] parameterTypes = handlerMapping.getMethod().getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> type = parameterTypes[i];
                if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                    paramMapping.put(type.getName(),i);
                }
            }
            this.handlerAdapters.put(handlerMapping,new HandlerAdapter(paramMapping));
        }

    }

    private void initViewResolvers(SanApplicationContext context) {
        //解决界面名字和模板文件关联的问题
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        File templateRootDir = new File(templateRootPath);
        for (File template : templateRootDir.listFiles()) {
            this.viewResolvers.add(new ViewResolver(template.getName(),template));
        }
    }

    private void initThemeResolver(SanApplicationContext context) {

    }

    private void initFlashMapManager(SanApplicationContext context) {

    }

    private void initRequestToViewNameTranslator(SanApplicationContext context) {

    }

    private void initHandlerExceptionResolvers(SanApplicationContext context) {

    }

    private void initLocaleResolver(SanApplicationContext context) {

    }

    private void initMultipartResolver(SanApplicationContext context) {
    }
}
