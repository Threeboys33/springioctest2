package dtstack.san.spring.webmvc;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
public class HandlerMapping {

    private Pattern url ;
    private Object controller;
    private Method method;

    public HandlerMapping(Pattern url, Object controller, Method method) {
        this.url = url;
        this.controller = controller;
        this.method = method;
    }

    public Pattern getUrl() {
        return url;
    }

    public void setUrl(Pattern url) {
        this.url = url;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
