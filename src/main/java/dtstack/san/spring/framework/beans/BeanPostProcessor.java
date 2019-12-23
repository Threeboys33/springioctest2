package dtstack.san.spring.framework.beans;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
//用户实现时间监听的
public class BeanPostProcessor {
    public Object postProcessBeforeInitialization(Object bean,String beanName) {
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}
