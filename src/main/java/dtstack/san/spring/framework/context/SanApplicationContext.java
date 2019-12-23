package dtstack.san.spring.framework.context;

import dtstack.san.spring.demo.action.DemoAction;
import dtstack.san.spring.framework.annotation.AutoWrite;
import dtstack.san.spring.framework.annotation.Controller;
import dtstack.san.spring.framework.annotation.Service;
import dtstack.san.spring.framework.aop.AopConfig;
import dtstack.san.spring.framework.beans.BeanDefinition;
import dtstack.san.spring.framework.beans.BeanPostProcessor;
import dtstack.san.spring.framework.beans.BeanWrapper;
import dtstack.san.spring.framework.context.support.BeanDefinitionReader;
import dtstack.san.spring.framework.core.BeanFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
public class SanApplicationContext extends DefaultListableBeanFactory implements BeanFactory {
    private String[] configLocations;
    private BeanDefinitionReader reader;

    //用来保证注册式单例的容器
    private Map<String, Object> beanCacheMap = new HashMap<>();
    //用来存储所有的被代理的对象
    private Map<String, BeanWrapper> beanWrapperMap = new ConcurrentHashMap<>();
    public SanApplicationContext(String... locations) {
        this.configLocations = locations;
        this.refresh();
    }

    public void refresh() {
        //定位
        this.reader = new BeanDefinitionReader(configLocations);

        //加载
        List<String> beanDefinitions = reader.loadBeanDefinitions();

        //注册
        doRegisty(beanDefinitions);

        //依赖注入
        doAutoWriter();


        //自动调入getBean方法

    }
    private void doAutoWriter(){
        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : this.beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            if (!beanDefinitionEntry.getValue().isLazyInit()) {
                //getbean操作将实例进行封装
                getBean(beanName);
            }
        }

        for (Map.Entry<String, BeanWrapper> beanWrapperEntry : this.beanWrapperMap.entrySet()){
            populateBean(beanWrapperEntry.getKey(),beanWrapperEntry.getValue().getOriginalInstance());
        }
    }

    public void populateBean(String beanName,Object instance) {
        Class<?> clazz = instance.getClass();
        //在controller注解和service注解上进行注入
        if (!(clazz.isAnnotationPresent(Controller.class)) || clazz.isAnnotationPresent(Service.class)) {
            return;
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(AutoWrite.class)) {
                continue;
            }
            AutoWrite autoWrite = field.getAnnotation(AutoWrite.class);
            String autoWriteBeanName = autoWrite.value();
            if ("".equals(autoWriteBeanName)) {
                autoWriteBeanName= field.getType().getName();
            }
            field.setAccessible(true);
            try {
                //service中和controller中的对象为 代理对象
                field.set(instance, this.beanWrapperMap.get(autoWriteBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }


    }

    //将BeanDefinition注册到beanDefinitionMap容器中
    private void doRegisty(List<String> beanDefinitions) {
        //beanName有三种情况
        //默认是类名首字母小写
        //自定义
        //接口注入
        try {
            for (String className : beanDefinitions) {
                Class<?> beanClass = Class.forName(className);

                //如果是一个接口，是不能实例化的，用他的实现类进行实例化
                if (beanClass.isInterface()) {
                    continue;
                }
                BeanDefinition beanDefinition = reader.registerBean(className);
                if (beanDefinition != null) {
                    this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
                }

                Class<?>[] interfaces = beanClass.getInterfaces();
                for (Class<?> i : interfaces) {
                    this.beanDefinitionMap.put(i.getName(), beanDefinition);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        String className = beanDefinition.getBeanClassName();
        try {
            //生成通知事件
            BeanPostProcessor beanPostProcessor = new BeanPostProcessor();
            Object instance = instantionBean(beanDefinition);
            if (null == instance) {
                return null;
            }
            //在实例初始化以前调用一次
            beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            BeanWrapper beanWrapper = new BeanWrapper(instance);
            beanWrapper.setAopConfig(instanntionAopConfig(beanDefinition));
            beanWrapper.setBeanPostProcessor(beanPostProcessor);
            this.beanWrapperMap.put(beanName, beanWrapper);

            //在实例初始化以后调用一次
            beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            // populateBean(beanName,instance);
            //通过这样一调用，相当于给我们自己留有了可操作的空间
            return this.beanWrapperMap.get(beanName).getWrappedInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private AopConfig instanntionAopConfig(BeanDefinition beanDefinition) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        AopConfig config = new AopConfig();
        String expression = reader.getConfig().getProperty("pointCut");
        String[] before = reader.getConfig().getProperty("aspectBefore").split("\\s");
        String[] after = reader.getConfig().getProperty("aspectAfter").split("\\s");

        String className = beanDefinition.getBeanClassName();
        Class<?> clazz = Class.forName(className);
        Pattern pattern = Pattern.compile(expression);

        Class<?> aspectClaszz = Class.forName(before[0]);
        for (Method m: clazz.getMethods()) {
            //dtstack.san.spring.demo.service.DemoServiceImpl.get(String name)
            //public .* dtstack\\.san\\.spring\\.demo\\.service\\..*Impl\\..*\\(.*\\)
            Matcher matcher = pattern.matcher(m.toString());
            if (matcher.matches()) {
                //能满足切面规则的类，添加到aop配置中去
                config.put(m,aspectClaszz.newInstance(),new Method[]{aspectClaszz.getMethod(before[1]),aspectClaszz.getMethod(after[1])});
            }
        }

        return config;
    }

    //传入一个BeanDefinition，返回一个实例Bean
    private Object instantionBean(BeanDefinition beanDefinition) {
        Object instance = null;
        String className = beanDefinition.getBeanClassName();

        try {
            if (this.beanCacheMap.containsKey(className)) {
                instance = this.beanCacheMap.get(className);
            } else {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();
                this.beanCacheMap.put(className, instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return instance;
    }


    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.keySet().size();
    }

    public String[] getBeanDefinitionNames() {
        // return this.getBeanFactory().getBeanDefinitionNames();
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public Properties getConfig() {
        return this.reader.getConfig();
    }
}
