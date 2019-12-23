package dtstack.san.spring.webmvc;

import dtstack.san.spring.webmvc.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.net.BindException;
import java.util.Arrays;
import java.util.Map;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
public class HandlerAdapter {
    private Map<String, Integer> paramMapping;
    public HandlerAdapter( Map<String, Integer> paramMapping) {
        this.paramMapping = paramMapping;
    }

    //handler中包含了controller、method、url信息
    //根据用户请求的url参数信息根method中的参数信息进行动态匹配
    //resp穿进来的目的，只是为了将其赋值给方法参数进行传递
    public ModelAndView handle(HttpServletRequest req, HttpServletResponse resp, HandlerMapping handler) throws InvocationTargetException, IllegalAccessException {
        //只有当用户传递过来的modelandview为null的时候才会new一个

        //1 准备好这个方法的形参列表
        Class<?>[] parameterTypes = handler.getMethod().getParameterTypes();

        //2 拿到自定义的参数所在的位置
        //用户传递过来的参数列表
        Map<String,String[]> reqParameterMap = req.getParameterMap();
        //3 构造实参列表
        Object[] paramValues = new Object[parameterTypes.length];
        for (Map.Entry<String, String[]> param:reqParameterMap.entrySet()){
            String value = Arrays.toString(param.getValue()).replaceAll("\\[]\\]","").replaceAll("\\s","");
            if (!this.paramMapping.containsKey(param.getKey())) {
                continue;
            }
            Integer index = this.paramMapping.get(param.getKey());
            //要针对传递过来的参数进行类型转化
            paramValues[index] = caseStringValue(value,parameterTypes[index]);
        }
        if(this.paramMapping.get(HttpServletRequest.class.getName())!=null){
            int reqIndex = this.paramMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = req;
        }

        if (this.paramMapping.get(HttpServletResponse.class.getName()) != null) {
            int repsIndex = this.paramMapping.get(HttpServletResponse.class.getName());
            paramValues[repsIndex] = resp;
        }

        //4 从handler 中取出controller、method、然后反射调用
        Object result = handler.getMethod().invoke(handler.getController(), paramValues);
        if (result == null) {
            return null;
        }
        boolean isModelAndView = handler.getMethod().getReturnType() == ModelAndView.class;
        if (isModelAndView) {
            return (ModelAndView)result;
        }else {
            return null;
        }
    }

    private Object caseStringValue(String value,Class<?> clazz) {
        if (clazz == String.class) {
            return value;
        } else if (clazz == Integer.class) {
            return Integer.valueOf(value);
        } else if (clazz == int.class) {
            return Integer.valueOf(value).intValue();
        }else
            return null;
    }
}
