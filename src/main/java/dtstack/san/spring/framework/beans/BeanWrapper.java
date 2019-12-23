package dtstack.san.spring.framework.beans;

import dtstack.san.spring.framework.aop.AopConfig;
import dtstack.san.spring.framework.aop.AopProxy;
import dtstack.san.spring.framework.core.FactoryBean;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
public class BeanWrapper extends FactoryBean {
    //还会用到观察者模式
    //1、支持时间响应，会有一个监听
    private BeanPostProcessor beanPostProcessor;

    private AopProxy aopProxy = new AopProxy();

    public BeanPostProcessor getBeanPostProcessor() {
        return beanPostProcessor;
    }

    public void setBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        this.beanPostProcessor = beanPostProcessor;
    }

    private Object wrappedInstance;
    //原始的通过反射new出来的，要把他包装起来，存下来
    private Object originalInstance;
    public BeanWrapper(Object instance) {
        this.wrappedInstance = aopProxy.getProxy(instance);
        this.originalInstance = instance;
    }

    public Object getWrappedInstance() {
        return wrappedInstance;
    }

    //返回代理以后的class
    //可能会是这个$Proxy0
    public Class<?> getWrappedClass() {
        return this.wrappedInstance.getClass();
    }

    public void setAopConfig(AopConfig aopConfig) {
        aopProxy.setConfig(aopConfig);
    }

    public Object getOriginalInstance() {
        return originalInstance;
    }
}
