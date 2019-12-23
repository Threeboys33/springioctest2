package dtstack.san.spring.demo.action;

import dtstack.san.spring.demo.service.IDemoService;
import dtstack.san.spring.framework.annotation.AutoWrite;
import dtstack.san.spring.framework.annotation.Controller;
import dtstack.san.spring.framework.annotation.RequestMapping;
import dtstack.san.spring.framework.annotation.RequestParam;
import dtstack.san.spring.webmvc.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
@Controller
@RequestMapping("/")
public class DemoAction {
    @AutoWrite
    private IDemoService iDemoService;
    @RequestMapping("/")
    public ModelAndView start() throws IOException {
        Map<String, Object> model = new HashMap<>();
        return new ModelAndView("index.jsp", model);
    }
    @RequestMapping("/demo/first.html")
    public ModelAndView queryFirst(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam("teacher") String teacher) throws IOException {
        String result = iDemoService.get(teacher);
        Map<String, Object> model = new HashMap<>();
        model.put("teacher", teacher);
        model.put("data", result);
        model.put("token", "12345");
        return new ModelAndView("first.html", model);
    }
    @RequestMapping("/demo/query*.json")
    public ModelAndView query(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam("name") String name) throws IOException {
        String result = iDemoService.get(name);
        return out(response, result);
    }
    public ModelAndView out( HttpServletResponse response,String str ) throws IOException {
        response.getWriter().write(str);
        return null;
    }
}
