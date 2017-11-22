package com.whf.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 模拟spring依赖注入的方式读取xml文件.
 * @author whfstudio@163.com
 * @date Aug 23, 2017
 */
public class XMLBeanFactory {

    private String xmlName;
    private SAXReader reader;
    private Document document;

    /**
     * 构造方法.
     * @param xmlName xmlName.
     */
    public XMLBeanFactory(String xmlName) { // 在构造方法中
        try {
            this.xmlName = xmlName;
            reader = new SAXReader();
            document = reader.read(this.getClass().getClassLoader().getResourceAsStream(xmlName));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取相同类型的bean.
     * @param type bean的class type.
     * @return 返回相同类型的bean的list.
     * @throws Exception Exception.
     */
    public <T> List<T> getBeansOfType(Class<T> type) throws Exception {
        List<T> objects = new ArrayList<>();
        try {
            Element root = document.getRootElement();
            List<Element> beans = root.elements();
            if (beans.size() > 0) {
                for (Element bean : beans) {
                    if (bean.attributeValue("class").equals(type.getName())) {
                        T object = null;
                        String clazz = bean.attributeValue("class");
                        // 通过反射来创建对象
                        Class beanClass = Class.forName(clazz);
                        object = (T) beanClass.newInstance();

                        List<Element> propertys = bean.elements();

                        if (propertys.size() > 0) {
                            for (Element property : propertys) {
                                String key = property.attributeValue("name");
                                Field field = beanClass.getDeclaredField(key);
                                field.setAccessible(true);

                                List<Element> childBean = property.elements();

                                // 如果property下内嵌bean
                                if (childBean.size() > 0) {
                                    Object childObject = getBean(key, property);
                                    field.set(object, childObject);
                                } else {
                                    /**
                                     * 此属性值是一个字符串.这里单独处理int,float类型变量.如果不处理,
                                     * 会将String类型直接赋值给int类型,发生ClassCastException
                                     */
                                    String value = property.attributeValue("value");
                                    // 需要对类型进行判断
                                    if (field.getType().getName().equals("int")) {
                                        // 整数
                                        int x = Integer.parseInt(value);
                                        field.set(object, x);
                                        continue;
                                    }
                                    if (field.getType().getName().equals("float")) {
                                        // 浮点数
                                        float y = Float.parseFloat(value);
                                        field.set(object, y); // 注意double可以接受float类型
                                        continue;
                                    }
                                    field.set(object, value);// 处理String类型
                                }
                            }
                        }
                        objects.add(object);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return objects;
    }

    /**
     * 根据id，name获取bean.
     * @param name name.
     * @return 返回一个属性注入完整的实体类.
     * @throws Exception Exception.
     */
    public Object getBean(String name) throws Exception {
        Object object = null;
        Element root = document.getRootElement();
        List<Element> beans = root.elements();
        if (beans.size() > 0) {
            for (Element bean : beans) {
                // 如果bean name相同则开始创建对象
                if (bean.attributeValue("name").equals(name)) {
                    String clazz = bean.attributeValue("class");
                    // 通过反射来创建对象
                    Class beanClass = Class.forName(clazz);
                    object = beanClass.newInstance();

                    List<Element> propertys = bean.elements();

                    if (propertys.size() > 0) {
                        for (Element property : propertys) {
                            String key = property.attributeValue("name");
                            Field field = beanClass.getDeclaredField(key);
                            field.setAccessible(true);

                            List<Element> childBean = property.elements();

                            // 如果property下内嵌bean
                            if (childBean.size() > 0) {
                                Object childObject = getBean(key, property);
                                field.set(object, childObject);
                            } else {
                                /*
                                 * 此属性值是一个字符串.这里单独处理int,float类型变量.如果不处理,
                                 * 会将String类型直接赋值给int类型,发生ClassCastException
                                 */
                                String value = property.attributeValue("value");
                                // 需要对类型进行判断
                                if (field.getType().getName().equals("int")) {
                                    // 整数
                                    int x = Integer.parseInt(value);
                                    field.set(object, x);
                                    continue;
                                }
                                if (field.getType().getName().equals("float")) {
                                    // 浮点数
                                    float y = Float.parseFloat(value);
                                    field.set(object, y); // 注意double可以接受float类型
                                    continue;
                                }
                                field.set(object, value);// 处理String类型
                            }
                        }
                    }

                }
            }
        }

        return object;
    }

    /**
     * 获取property内嵌的bean.
     * @param name id或者bean的name
     * @param root 根节点.
     * @return 返回封装完整的bean.
     * @throws Exception Exception.
     */
    public Object getBean(String name, Element root) throws Exception {

        Object object = null;
        List<Element> beans = root.elements();
        if (beans.size() > 0) {
            for (Element bean : beans) {
                if (bean.attributeValue("name").equals(name)) {
                    // 如果bean name相同则开始创建对象
                    String clazz = bean.attributeValue("class");
                    // 通过反射来创建对象
                    Class beanClass = Class.forName(clazz);
                    object = beanClass.newInstance();

                    List<Element> propertys = bean.elements();

                    if (propertys.size() > 0) {
                        for (Element property : propertys) {
                            String key = property.attributeValue("name");
                            Field field = beanClass.getDeclaredField(key);
                            field.setAccessible(true);

                            List<Element> childBean = property.elements();

                            // 如果property下内嵌bean
                            if (childBean.size() > 0) {
                                field.set(object, getBean(key, property));
                            }

                            if (property.attribute("ref") != null) {
                                /**
                                 * 此属性的值是一个对象.这里由于直接调用getBean方法赋值给对象,返回的对象一定是Bean参数的对象, 因此强制转换不会出问题
                                 */
                                String refid = property.attributeValue("ref");
                                field.set(object, getBean(refid));
                            } else {
                                /**
                                 * 此属性值是一个字符串.这里单独处理int,float类型变量.如果不处理,会将String类型直接赋值给int类型,
                                 * 发生ClassCastException
                                 */
                                String value = property.attributeValue("value");
                                /**
                                 *  需要对类型进行判断,处理String类型.
                                 */
                                field.set(object, value);
                            }
                        }
                    }

                }
            }
        }

        return object;
    }

}
