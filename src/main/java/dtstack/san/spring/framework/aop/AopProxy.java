package dtstack.san.spring.framework.aop;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.regex.Matcher;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
public class AopProxy implements InvocationHandler {

    private Object target;
    private AopConfig config;

    public void setConfig(AopConfig config) {
        this.config = config;
    }

    //把原生的对象传递进来
    public  Object getProxy(Object instance){
        this.target = instance;
        Class<?> clazz = instance.getClass();
        return Proxy.newProxyInstance(clazz.getClassLoader(),
                clazz.getInterfaces(),this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Method m = this.target.getClass().getMethod(method.getName(), method.getParameterTypes());

        //在原始方法进行调用之前进行增强
        if (config.contains(m)) {
            AopConfig.Aspect aspect = config.get(m);
            aspect.getPoints()[0].invoke(aspect.getAspect());
        }

        //反射调用原始方法
        Object obj = method.invoke(this.target, args);

        //在原始方法开始之后进行增强
        if (config.contains(m)) {
            AopConfig.Aspect aspect = config.get(m);
            aspect.getPoints()[1].invoke(aspect.getAspect());
        }

        //将最原始的返回值返回出去
        return obj;
    }
}
