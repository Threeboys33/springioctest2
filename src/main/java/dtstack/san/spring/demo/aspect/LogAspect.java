package dtstack.san.spring.demo.aspect;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
public class LogAspect {
    //调用方法执行这个方法
    public void before() {
        //这个方法中的逻辑自己定义，spring中就是datasourceManager
        //在查询之前建立连接，在查询之后关闭连接，在commit之前开启事务
        System.out.println("invoke before");
    }

    //调用这个方法之后执行这个after方法
    public void after() {
        System.out.println("invoke after");
    }
}
