package dtstack.san.spring.framework.context.support;

import dtstack.san.spring.framework.beans.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
//用来对配置文件进行查找、解析、读取
public class BeanDefinitionReader {

    private Properties config = new Properties();
    private  final String SCAN_PACKAGE="scanPackage";

    List<String> registryBeanClasses = new ArrayList();

    public BeanDefinitionReader(String... locations) {
        try(InputStream is = this.getClass().getClassLoader()
                .getResourceAsStream(locations[0].replace("classpath:",""))){
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    // public static void main(String[] args) {
    //     String packageName = "classpath:dtstack.san.spring.demo";
    //     BeanDefinitionReader beanDefinitionReader = new BeanDefinitionReader(packageName);
    //     URL url = beanDefinitionReader.getClass().getResource("/" + packageName.replaceAll("\\.","/"));
    //     System.out.println(url);
    // }

    private void doScanner(String packageName) {
        URL url = this.getClass().getResource("/" + packageName.replaceAll("\\.","/"));
        File classDir = new File(url.getFile());

        for (File file : classDir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(packageName + "." + file.getName());
            }else {
                registryBeanClasses.add(packageName + "." + file.getName().replaceAll(".class", ""));
            }
        }

    }

    public List<String> loadBeanDefinitions() {
        return  this.registryBeanClasses;
    }

    //每注册一个className，就返回一个BeanDefinition，只是为了对配置信息进行包装
    public BeanDefinition registerBean(String className){
        if (this.registryBeanClasses.contains(className)) {
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setBeanClassName(className);
            beanDefinition.setFactoryBeanName(lowerFirstCase(className.substring(className.lastIndexOf(".") + 1)));
            return beanDefinition;
        }
        return null;
    }

    public Properties getConfig() {
        return this.config;
    }
    private String lowerFirstCase(String string) {
        char [] chars = string.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

}
