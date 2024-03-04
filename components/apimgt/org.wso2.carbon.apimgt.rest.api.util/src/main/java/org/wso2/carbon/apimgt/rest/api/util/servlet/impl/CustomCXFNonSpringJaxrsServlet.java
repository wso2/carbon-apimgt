package org.wso2.carbon.apimgt.rest.api.util.servlet.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.ws.rs.core.Application;
import org.apache.cxf.Bus;
import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.PrimitiveUtils;
import org.apache.cxf.common.util.PropertyUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.PerRequestResourceProvider;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.jaxrs.model.ApplicationInfo;
import org.apache.cxf.jaxrs.model.ProviderInfo;
import org.apache.cxf.jaxrs.provider.ProviderFactory;
import org.apache.cxf.jaxrs.utils.InjectionUtils;
import org.apache.cxf.jaxrs.utils.ResourceUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.transport.http.DestinationRegistry;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

public class CustomCXFNonSpringJaxrsServlet extends CXFNonSpringServlet {
    private static final long serialVersionUID = -8916352798780577499L;
    private static final Logger LOG = LogUtils.getL7dLogger(CustomCXFNonSpringJaxrsServlet.class);
    private static final String USER_MODEL_PARAM = "user.model";
    private static final String SERVICE_ADDRESS_PARAM = "jaxrs.address";
    private static final String IGNORE_APP_PATH_PARAM = "jaxrs.application.address.ignore";
    private static final String SERVICE_CLASSES_PARAM = "jaxrs.serviceClasses";
    private static final String PROVIDERS_PARAM = "jaxrs.providers";
    private static final String FEATURES_PARAM = "jaxrs.features";
    private static final String OUT_INTERCEPTORS_PARAM = "jaxrs.outInterceptors";
    private static final String OUT_FAULT_INTERCEPTORS_PARAM = "jaxrs.outFaultInterceptors";
    private static final String IN_INTERCEPTORS_PARAM = "jaxrs.inInterceptors";
    private static final String INVOKER_PARAM = "jaxrs.invoker";
    private static final String SERVICE_SCOPE_PARAM = "jaxrs.scope";
    private static final String EXTENSIONS_PARAM = "jaxrs.extensions";
    private static final String LANGUAGES_PARAM = "jaxrs.languages";
    private static final String PROPERTIES_PARAM = "jaxrs.properties";
    private static final String SCHEMAS_PARAM = "jaxrs.schemaLocations";
    private static final String DOC_LOCATION_PARAM = "jaxrs.documentLocation";
    private static final String STATIC_SUB_RESOLUTION_PARAM = "jaxrs.static.subresources";
    private static final String SERVICE_SCOPE_SINGLETON = "singleton";
    private static final String SERVICE_SCOPE_REQUEST = "prototype";
    private static final String PARAMETER_SPLIT_CHAR = "class.parameter.split.char";
    private static final String DEFAULT_PARAMETER_SPLIT_CHAR = ",";
    private static final String SPACE_PARAMETER_SPLIT_CHAR = "space";
    private static final String JAXRS_APPLICATION_PARAM = "javax.ws.rs.Application";

    private static Map<String, String> systemPropMap = new HashMap();
    private ClassLoader classLoader;
    private Application application;

    static {
        systemPropMap.put("rest.api.admin.attachment.max.size", "10485760");
        systemPropMap.put("rest.api.devportal.attachment.max.size", "10485760");
        systemPropMap.put("rest.api.publisher.attachment.max.size", "10485760");
        systemPropMap.put("rest.api.service.catalog.attachment.max.size", "10485760");
    }

    public CustomCXFNonSpringJaxrsServlet() {
    }

    public CustomCXFNonSpringJaxrsServlet(Application app) {
        this.application = app;
    }

    public CustomCXFNonSpringJaxrsServlet(Application app, DestinationRegistry destinationRegistry, Bus bus) {
        super(destinationRegistry, bus);
        this.application = app;
    }

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        if (this.getApplication() != null) {
            this.createServerFromApplication(servletConfig);
        } else {
            String applicationClass = servletConfig.getInitParameter("javax.ws.rs.Application");
            if (applicationClass != null) {
                this.createServerFromApplication(applicationClass, servletConfig);
            } else {
                String splitChar = this.getParameterSplitChar(servletConfig);
                JAXRSServerFactoryBean bean = new JAXRSServerFactoryBean();
                bean.setBus(this.getBus());
                String address = servletConfig.getInitParameter("jaxrs.address");
                if (address == null) {
                    address = "/";
                }

                bean.setAddress(address);
                bean.setStaticSubresourceResolution(this.getStaticSubResolutionValue(servletConfig));
                String modelRef = servletConfig.getInitParameter("user.model");
                if (modelRef != null) {
                    bean.setModelRef(modelRef.trim());
                }

                this.setDocLocation(bean, servletConfig);
                this.setSchemasLocations(bean, servletConfig);
                this.setAllInterceptors(bean, servletConfig, splitChar);
                this.setInvoker(bean, servletConfig);
                Map<Class<?>, Map<String, List<String>>> resourceClasses = this.getServiceClasses(servletConfig, modelRef != null, splitChar);
                Map<Class<?>, ResourceProvider> resourceProviders = this.getResourceProviders(servletConfig, resourceClasses);
                List<?> providers = this.getProviders(servletConfig, splitChar);
                bean.setResourceClasses(new ArrayList(resourceClasses.keySet()));
                bean.setProviders(providers);
                Iterator var10 = resourceProviders.entrySet().iterator();

                while(var10.hasNext()) {
                    Map.Entry<Class<?>, ResourceProvider> entry = (Map.Entry)var10.next();
                    bean.setResourceProvider((Class)entry.getKey(), (ResourceProvider)entry.getValue());
                }

                this.setExtensions(bean, servletConfig);
                List<? extends Feature> features = this.getFeatures(servletConfig, splitChar);
                bean.getFeatures().addAll(features);
                bean.create();
            }
        }
    }

    protected String getParameterSplitChar(ServletConfig servletConfig) {
        String param = servletConfig.getInitParameter("class.parameter.split.char");
        return !StringUtils.isEmpty(param) && "space".equals(param.trim()) ? " " : ",";
    }

    protected boolean getStaticSubResolutionValue(ServletConfig servletConfig) {
        String param = servletConfig.getInitParameter("jaxrs.static.subresources");
        return param != null ? Boolean.valueOf(param.trim()) : false;
    }

    protected void setExtensions(JAXRSServerFactoryBean bean, ServletConfig servletConfig) {
        bean.setExtensionMappings(CastUtils.cast(parseMapSequence(servletConfig.getInitParameter("jaxrs.extensions"))));
        bean.setLanguageMappings(CastUtils.cast(parseMapSequence(servletConfig.getInitParameter("jaxrs.languages"))));
        Map<String, Object> properties = CastUtils.cast(parseMapSequence(servletConfig.getInitParameter("jaxrs.properties")), String.class, Object.class);

        //Custom impl to allow property values to be defined as system properties
        for (Map.Entry<String, Object> entry: properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().toString();

            if (value.startsWith("{systemProperties")) {
                int begin = value.indexOf("'");
                int end = value.lastIndexOf("'");
                String propertyKey = value.substring(begin + 1, end);
                String systemPropValue = System.getProperty(propertyKey);
                if (systemPropValue != null && !systemPropValue.isEmpty()) {
                    properties.put(key, systemPropValue);
                } else {
                    properties.put(key, systemPropMap.get(propertyKey));
                }
            }
        }

        if (properties != null && !properties.isEmpty()) {
            bean.getProperties(true).putAll(properties);
        }
    }

    protected void setAllInterceptors(JAXRSServerFactoryBean bean, ServletConfig servletConfig, String splitChar) throws ServletException {
        this.setInterceptors(bean, servletConfig, "jaxrs.outInterceptors", splitChar);
        this.setInterceptors(bean, servletConfig, "jaxrs.outFaultInterceptors", splitChar);
        this.setInterceptors(bean, servletConfig, "jaxrs.inInterceptors", splitChar);
    }

    protected void setSchemasLocations(JAXRSServerFactoryBean bean, ServletConfig servletConfig) {
        String schemas = servletConfig.getInitParameter("jaxrs.schemaLocations");
        if (schemas != null) {
            String[] locations = schemas.split(" ");
            List<String> list = new ArrayList();
            String[] var6 = locations;
            int var7 = locations.length;

            for(int var8 = 0; var8 < var7; ++var8) {
                String loc = var6[var8];
                String theLoc = loc.trim();
                if (!theLoc.isEmpty()) {
                    list.add(theLoc);
                }
            }

            if (!list.isEmpty()) {
                bean.setSchemaLocations(list);
            }

        }
    }

    protected void setDocLocation(JAXRSServerFactoryBean bean, ServletConfig servletConfig) {
        String wadlLoc = servletConfig.getInitParameter("jaxrs.documentLocation");
        if (wadlLoc != null) {
            bean.setDocLocation(wadlLoc);
        }

    }

    protected void setInterceptors(JAXRSServerFactoryBean bean, ServletConfig servletConfig, String paramName, String splitChar) throws ServletException {
        String value = servletConfig.getInitParameter(paramName);
        if (value != null) {
            String[] values = value.split(splitChar);
            List<Interceptor<? extends Message>> list = new ArrayList();
            String[] var8 = values;
            int var9 = values.length;

            for(int var10 = 0; var10 < var9; ++var10) {
                String interceptorVal = var8[var10];
                Map<String, List<String>> props = new HashMap();
                String theValue = this.getClassNameAndProperties(interceptorVal, props);
                if (!theValue.isEmpty()) {
                    try {
                        Class<?> intClass = this.loadClass(theValue, "Interceptor");
                        Object object = intClass.getDeclaredConstructor().newInstance();
                        this.injectProperties(object, props);
                        list.add((Interceptor)object);
                    } catch (ServletException var16) {
                        throw var16;
                    } catch (Exception var17) {
                        LOG.warning("Interceptor class " + theValue + " can not be created");
                        throw new ServletException(var17);
                    }
                }
            }

            if (!list.isEmpty()) {
                if ("jaxrs.outInterceptors".equals(paramName)) {
                    bean.setOutInterceptors(list);
                } else if ("jaxrs.outFaultInterceptors".equals(paramName)) {
                    bean.setOutFaultInterceptors(list);
                } else {
                    bean.setInInterceptors(list);
                }
            }

        }
    }

    protected void setInvoker(JAXRSServerFactoryBean bean, ServletConfig servletConfig) throws ServletException {
        String value = servletConfig.getInitParameter("jaxrs.invoker");
        if (value != null) {
            Map<String, List<String>> props = new HashMap();
            String theValue = this.getClassNameAndProperties(value, props);
            if (!theValue.isEmpty()) {
                try {
                    Class<?> intClass = this.loadClass(theValue, "Invoker");
                    Object object = intClass.getDeclaredConstructor().newInstance();
                    this.injectProperties(object, props);
                    bean.setInvoker((Invoker)object);
                } catch (ServletException var8) {
                    throw var8;
                } catch (Exception var9) {
                    LOG.warning("Invoker class " + theValue + " can not be created");
                    throw new ServletException(var9);
                }
            }

        }
    }

    protected Map<Class<?>, Map<String, List<String>>> getServiceClasses(ServletConfig servletConfig, boolean modelAvailable, String splitChar) throws ServletException {
        String serviceBeans = servletConfig.getInitParameter("jaxrs.serviceClasses");
        if (serviceBeans == null) {
            if (modelAvailable) {
                return Collections.emptyMap();
            } else {
                throw new ServletException("At least one resource class should be specified");
            }
        } else {
            String[] classNames = serviceBeans.split(splitChar);
            Map<Class<?>, Map<String, List<String>>> map = new HashMap();
            String[] var7 = classNames;
            int var8 = classNames.length;

            for(int var9 = 0; var9 < var8; ++var9) {
                String cName = var7[var9];
                Map<String, List<String>> props = new HashMap();
                String theName = this.getClassNameAndProperties(cName, props);
                if (!theName.isEmpty()) {
                    Class<?> cls = this.loadClass(theName);
                    map.put(cls, props);
                }
            }

            if (map.isEmpty()) {
                throw new ServletException("At least one resource class should be specified");
            } else {
                return map;
            }
        }
    }

    protected List<? extends Feature> getFeatures(ServletConfig servletConfig, String splitChar) throws ServletException {
        String featuresList = servletConfig.getInitParameter("jaxrs.features");
        if (featuresList == null) {
            return Collections.emptyList();
        } else {
            String[] classNames = featuresList.split(splitChar);
            List<Feature> features = new ArrayList();
            String[] var6 = classNames;
            int var7 = classNames.length;

            for(int var8 = 0; var8 < var7; ++var8) {
                String cName = var6[var8];
                Map<String, List<String>> props = new HashMap();
                String theName = this.getClassNameAndProperties(cName, props);
                if (!theName.isEmpty()) {
                    Class<?> cls = this.loadClass(theName);
                    if (Feature.class.isAssignableFrom(cls)) {
                        features.add((Feature)this.createSingletonInstance(cls, props, servletConfig));
                    }
                }
            }

            return features;
        }
    }

    protected List<?> getProviders(ServletConfig servletConfig, String splitChar) throws ServletException {
        String providersList = servletConfig.getInitParameter("jaxrs.providers");
        if (providersList == null) {
            return Collections.emptyList();
        } else {
            String[] classNames = providersList.split(splitChar);
            List<Object> providers = new ArrayList();
            String[] var6 = classNames;
            int var7 = classNames.length;

            for(int var8 = 0; var8 < var7; ++var8) {
                String cName = var6[var8];
                Map<String, List<String>> props = new HashMap();
                String theName = this.getClassNameAndProperties(cName, props);
                if (!theName.isEmpty()) {
                    Class<?> cls = this.loadClass(theName);
                    providers.add(this.createSingletonInstance(cls, props, servletConfig));
                }
            }

            return providers;
        }
    }

    private String getClassNameAndProperties(String cName, Map<String, List<String>> props) {
        String theName = cName.trim();
        int ind = theName.indexOf(40);
        if (ind != -1 && theName.endsWith(")")) {
            props.putAll(parseMapListSequence(theName.substring(ind + 1, theName.length() - 1)));
            theName = theName.substring(0, ind).trim();
        }

        return theName;
    }

    protected static Map<String, List<String>> parseMapListSequence(String sequence) {
        if (sequence != null) {
            sequence = sequence.trim();
            Map<String, List<String>> map = new HashMap();
            String[] pairs = sequence.split(" ");
            String[] var3 = pairs;
            int var4 = pairs.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                String pair = var3[var5];
                String thePair = pair.trim();
                if (thePair.length() != 0) {
                    String[] values = thePair.split("=");
                    String key;
                    String value;
                    if (values.length == 2) {
                        key = values[0].trim();
                        value = values[1].trim();
                    } else {
                        key = thePair;
                        value = "";
                    }

                    List<String> list = (List)map.get(key);
                    if (list == null) {
                        list = new LinkedList();
                        map.put(key, list);
                    }

                    ((List)list).add(value);
                }
            }

            return map;
        } else {
            return Collections.emptyMap();
        }
    }

    protected Map<Class<?>, ResourceProvider> getResourceProviders(ServletConfig servletConfig, Map<Class<?>, Map<String, List<String>>> resourceClasses) throws ServletException {
        String scope = servletConfig.getInitParameter("jaxrs.scope");
        if (scope != null && !"singleton".equals(scope) && !"prototype".equals(scope)) {
            throw new ServletException("Only singleton and prototype scopes are supported");
        } else {
            boolean isPrototype = "prototype".equals(scope);
            Map<Class<?>, ResourceProvider> map = new HashMap();
            Iterator var6 = resourceClasses.entrySet().iterator();

            while(var6.hasNext()) {
                Map.Entry<Class<?>, Map<String, List<String>>> entry = (Map.Entry)var6.next();
                Class<?> c = (Class)entry.getKey();
                map.put(c, isPrototype ? new PerRequestResourceProvider(c) : new SingletonResourceProvider(this.createSingletonInstance(c, (Map)entry.getValue(), servletConfig), true));
            }

            return map;
        }
    }

    protected boolean isAppResourceLifecycleASingleton(Application app, ServletConfig servletConfig) {
        String scope = servletConfig.getInitParameter("jaxrs.scope");
        if (scope == null) {
            scope = (String)app.getProperties().get("jaxrs.scope");
        }

        return "singleton".equals(scope);
    }

    protected Object createSingletonInstance(Class<?> cls, Map<String, List<String>> props, ServletConfig sc) throws ServletException {
        Constructor<?> c = ResourceUtils.findResourceConstructor(cls, false);
        if (c == null) {
            throw new ServletException("No valid constructor found for " + cls.getName());
        } else {
            boolean isApplication = Application.class.isAssignableFrom(c.getDeclaringClass());

            try {
                Object provider;
                if (c.getParameterTypes().length == 0) {
                    if (isApplication) {
                        provider = new ApplicationInfo((Application)c.newInstance(), this.getBus());
                    } else {
                        provider = new ProviderInfo(c.newInstance(), this.getBus(), false, true);
                    }
                } else {
                    Map<Class<?>, Object> values = new HashMap();
                    values.put(ServletContext.class, sc.getServletContext());
                    values.put(ServletConfig.class, sc);
                    provider = ProviderFactory.createProviderFromConstructor(c, values, this.getBus(), isApplication, true);
                }

                Object instance = ((ProviderInfo)provider).getProvider();
                this.injectProperties(instance, props);
                this.configureSingleton(instance);
                return isApplication ? provider : instance;
            } catch (InstantiationException var8) {
                var8.printStackTrace();
                throw new ServletException("Resource class " + cls.getName() + " can not be instantiated");
            } catch (IllegalAccessException var9) {
                var9.printStackTrace();
                throw new ServletException("Resource class " + cls.getName() + " can not be instantiated due to IllegalAccessException");
            } catch (InvocationTargetException var10) {
                var10.printStackTrace();
                throw new ServletException("Resource class " + cls.getName() + " can not be instantiated due to InvocationTargetException");
            }
        }
    }

    private void injectProperties(Object instance, Map<String, List<String>> props) {
        if (props != null && !props.isEmpty()) {
            Method[] methods = instance.getClass().getMethods();
            Map<String, Method> methodsMap = new HashMap();
            Method[] var5 = methods;
            int var6 = methods.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                Method m = var5[var7];
                methodsMap.put(m.getName(), m);
            }

            Iterator var10 = props.entrySet().iterator();

            while(var10.hasNext()) {
                Map.Entry<String, List<String>> entry = (Map.Entry)var10.next();
                Method m = (Method)methodsMap.get("set" + StringUtils.capitalize((String)entry.getKey()));
                if (m != null) {
                    Class<?> type = m.getParameterTypes()[0];
                    Object value;
                    if (InjectionUtils.isPrimitive(type)) {
                        value = PrimitiveUtils.read((String)((List)entry.getValue()).get(0), type);
                    } else {
                        value = entry.getValue();
                    }

                    InjectionUtils.injectThroughMethod(instance, m, value);
                }
            }

        }
    }

    protected void configureSingleton(Object instance) {
    }

    protected void createServerFromApplication(String applicationNames, ServletConfig servletConfig) throws ServletException {
        boolean ignoreApplicationPath = this.isIgnoreApplicationPath(servletConfig);
        String[] classNames = applicationNames.split(this.getParameterSplitChar(servletConfig));
        if (classNames.length > 1 && ignoreApplicationPath) {
            throw new ServletException("\"jaxrs.application.address.ignore\" parameter must be set to false for multiple Applications be supported");
        } else {
            String[] var5 = classNames;
            int var6 = classNames.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                String cName = var5[var7];
                ApplicationInfo providerApp = this.createApplicationInfo(cName, servletConfig);
                Application app = (Application)providerApp.getProvider();
                JAXRSServerFactoryBean bean = ResourceUtils.createApplication(app, ignoreApplicationPath, this.getStaticSubResolutionValue(servletConfig), this.isAppResourceLifecycleASingleton(app, servletConfig), this.getBus());
                String splitChar = this.getParameterSplitChar(servletConfig);
                this.setAllInterceptors(bean, servletConfig, splitChar);
                this.setInvoker(bean, servletConfig);
                this.setExtensions(bean, servletConfig);
                this.setDocLocation(bean, servletConfig);
                this.setSchemasLocations(bean, servletConfig);
                List<?> providers = this.getProviders(servletConfig, splitChar);
                bean.setProviders(providers);
                List<? extends Feature> features = this.getFeatures(servletConfig, splitChar);
                bean.getFeatures().addAll(features);
                bean.setBus(this.getBus());
                bean.setApplicationInfo(providerApp);
                bean.create();
            }

        }
    }

    protected boolean isIgnoreApplicationPath(ServletConfig servletConfig) {
        String ignoreParam = servletConfig.getInitParameter("jaxrs.application.address.ignore");
        return ignoreParam == null || PropertyUtils.isTrue(ignoreParam);
    }

    protected void createServerFromApplication(ServletConfig servletConfig) throws ServletException {
        Application app = this.getApplication();
        JAXRSServerFactoryBean bean = ResourceUtils.createApplication(app, this.isIgnoreApplicationPath(servletConfig), this.getStaticSubResolutionValue(servletConfig), this.isAppResourceLifecycleASingleton(app, servletConfig), this.getBus());
        String splitChar = this.getParameterSplitChar(servletConfig);
        this.setAllInterceptors(bean, servletConfig, splitChar);
        this.setInvoker(bean, servletConfig);
        this.setExtensions(bean, servletConfig);
        this.setDocLocation(bean, servletConfig);
        this.setSchemasLocations(bean, servletConfig);
        List<?> providers = this.getProviders(servletConfig, splitChar);
        bean.setProviders(providers);
        List<? extends Feature> features = this.getFeatures(servletConfig, splitChar);
        bean.getFeatures().addAll(features);
        bean.setBus(this.getBus());
        bean.setApplication(this.getApplication());
        bean.create();
    }

    protected Application createApplicationInstance(String appClassName, ServletConfig servletConfig) throws ServletException {
        return null;
    }

    protected ApplicationInfo createApplicationInfo(String appClassName, ServletConfig servletConfig) throws ServletException {
        Application customApp = this.createApplicationInstance(appClassName, servletConfig);
        if (customApp != null) {
            return new ApplicationInfo(customApp, this.getBus());
        } else {
            Map<String, List<String>> props = new HashMap();
            appClassName = this.getClassNameAndProperties(appClassName, props);
            Class<?> appClass = this.loadApplicationClass(appClassName);
            ApplicationInfo appInfo = (ApplicationInfo)this.createSingletonInstance(appClass, props, servletConfig);
            Map<String, Object> servletProps = new HashMap();
            ServletContext servletContext = servletConfig.getServletContext();
            Enumeration<String> names = servletContext.getInitParameterNames();

            String name;
            while(names.hasMoreElements()) {
                name = (String)names.nextElement();
                servletProps.put(name, servletContext.getInitParameter(name));
            }

            names = servletConfig.getInitParameterNames();

            while(names.hasMoreElements()) {
                name = (String)names.nextElement();
                servletProps.put(name, servletConfig.getInitParameter(name));
            }

            appInfo.setOverridingProps(servletProps);
            return appInfo;
        }
    }

    protected Class<?> loadApplicationClass(String appClassName) throws ServletException {
        return this.loadClass(appClassName, "Application");
    }

    protected Class<?> loadClass(String cName) throws ServletException {
        return this.loadClass(cName, "Resource");
    }

    protected Class<?> loadClass(String cName, String classType) throws ServletException {
        try {
            Class cls;
            if (this.classLoader == null) {
                cls = ClassLoaderUtils.loadClass(cName, org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet.class);
            } else {
                cls = this.classLoader.loadClass(cName);
            }

            return cls;
        } catch (ClassNotFoundException var4) {
            throw new ServletException("No " + classType + " class " + cName.trim() + " can be found", var4);
        }
    }

    public void setClassLoader(ClassLoader loader) {
        this.classLoader = loader;
    }

    protected Application getApplication() {
        return this.application;
    }

    private static class ApplicationImpl extends Application {
        private Set<Object> applicationSingletons;

        ApplicationImpl(Set<Object> applicationSingletons) {
            this.applicationSingletons = applicationSingletons;
        }

        public Set<Object> getSingletons() {
            return this.applicationSingletons;
        }
    }
}
