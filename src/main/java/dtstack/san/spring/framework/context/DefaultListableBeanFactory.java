package dtstack.san.spring.framework.context;

import dtstack.san.spring.framework.beans.BeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
public class DefaultListableBeanFactory extends AbstractApplicationContext {
    //beanDefinitionMap用来保存配置信息
    public Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    @Override
    protected void onRefresh() {

    }

    @Override
    protected void refreshBeanFactory() {

    }
}
