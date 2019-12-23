package dtstack.san.spring.framework.context;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
public abstract class AbstractApplicationContext {

    protected void onRefresh() {

    }

    protected abstract void refreshBeanFactory();
}
