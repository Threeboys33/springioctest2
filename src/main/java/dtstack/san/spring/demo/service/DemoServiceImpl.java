package dtstack.san.spring.demo.service;

import dtstack.san.spring.framework.annotation.Service;

/**
 * <p>
 *
 * @author 33
 * @version 1.0.0
 */
@Service
public class DemoServiceImpl implements IDemoService {
    @Override
    public String get(String name) {
        return "My name is " + name;
    }
}
