package dtstack.san.spring.webmvc.servlet;

import java.util.Map;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
public class ModelAndView {

    private final String viewName;
    private Map<String, Object> model;
    public ModelAndView(String viewName, Map<String, Object> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, Object> getModel() {
        return model;
    }

    public void setModel(Map<String, Object> model) {
        this.model = model;
    }
}
