package dtstack.san.spring.framework.aop;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
public class ProxyUtils {
    public static Object getTargetObject(Object proxy) {
        //先判断一下，传递的类这个对象是不是一个代理的对象
        //如果不是一个代理对象就直接返回
        if (!isAopProxy(proxy)) {
            return proxy;
        }
        try {
            return getProxyTargetObject(proxy);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean isAopProxy(Object object) {
        return Proxy.isProxyClass(object.getClass());
    }

    private static Object getProxyTargetObject(Object proxy) throws NoSuchFieldException, IllegalAccessException {

        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        AopProxy o =(AopProxy)h.get(proxy);
        Field target = o.getClass().getDeclaredField("target");
        target.setAccessible(true);
        return target.get(o);
    }
}
