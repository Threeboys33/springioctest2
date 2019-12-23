package dtstack.san.spring.framework.aop;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
//只是对application中的expression的封装
//目标代理对象的一个方法要增强
//用自己实现的业务逻辑去增强
//配置文件的目的，告诉spring，哪些类的哪些方法需要增强，增强的类容是什么
//对配置文件中所体现的内容进行封装
public class AopConfig {

    //以目标对象需要增强的Method作为key，需要增强的代码内容作为value
    private Map<Method, Aspect> points  =new HashMap<>();

    public void put(Method target, Object aspect, Method[] points) {
        this.points.put(target,new Aspect(aspect,points));
    }

    public Aspect get(Method method) {
        return this.points.get(method);
    }

    public boolean contains(Method method) {
        return this.points.containsKey(method);
    }

    //对增强的代理代码封装
    public class Aspect{
        private Object aspect;//本demo中是将LogAspect的这个对象赋值给他
        private Method[] points;//会将LogAspect的before和after方法赋值进来

        public Aspect(Object aspect, Method[] points) {
            this.aspect = aspect;
            this.points = points;
        }

        public Object getAspect() {
            return aspect;
        }

        public void setAspect(Object aspect) {
            this.aspect = aspect;
        }

        public Method[] getPoints() {
            return points;
        }

        public void setPoints(Method[] points) {
            this.points = points;
        }
    }

}
