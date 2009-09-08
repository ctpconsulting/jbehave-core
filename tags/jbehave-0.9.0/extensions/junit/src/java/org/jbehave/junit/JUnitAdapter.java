/*
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package org.jbehave.junit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jbehave.core.behaviour.BehaviourClass;
import org.jbehave.core.behaviour.BehaviourMethod;
import org.jbehave.core.behaviour.Behaviours;
import org.jbehave.core.exception.JBehaveFrameworkError;


/**
 * <p>
 * Runs behaviours in a JUnit test runner.
 * </p>
 * 
 * <p>
 * The adapter creates a nested JUnit {@link TestSuite} for a given {@link Behaviours} instance.
 * with the behaviour classes and their behaviour methods for a given <tt>Behaviours</tt> class, in which each 
 * nested TestSuite represents a behaviour with all its behaviour methods.
 * </p>
 * 
 * <p>
 * To instantiate the Behaviours, it looks for
 * <ul>
 *  <li>the injected Behaviours instance via the <tt>JUnitAdapter{@link #setBehaviours(Behaviours)}</tt> method</li>
 *  <li>the jbehave properties (either system properties or in a <tt>jbehave.properties</tt> file) in the classloader for the following properties:
 *      <ol>
 *          <li> A property called <tt>behavioursClass</tt> containing the full class name of the Behaviours to run;</li>
 *          <li> A property called <tt>behaviourClass</tt> containing the full class name of the single behaviour to run,
 *               from which the JUnitAdapter creates a <tt>BehavioursAdapter</tt>.</li>
 *      </ol>
 *  </li>
 * </ul>
 * </p>
 * 
 * <p>
 * The adapter supports the injection of the {@link ClassLoader} via the {@link #setClassLoader(ClassLoader)} method.
 * If not injected, the current thread classloader is used.
 * </p>
 * 
 * <p>
 * To used the JUnitAdapter in you project, you need to have the JBehave core and JUnit extension,
 * as well as JUnit jar, in your classpath, and then run <tt>jbehave.junit.JUnitAdapter</tt> as a test inside any
 * old test runner (such as your IDE's). 
 * </p>
 * 
 * @see org.jbehave.core.behaviour.Behaviours
 * @see junit.framework.TestSuite
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 * @author Mauro Talevi
 */
public class JUnitAdapter {

    private static Behaviours behaviours = null;
    private static ClassLoader classLoader = null;
    
    /**
     * Allows to inject the class of the Behaviours to run
     * @param behaviours the Behaviours Class
     */
    public static void setBehaviours(Behaviours behaviours) {
        JUnitAdapter.behaviours = behaviours;
    }
    
    /**
     * Allows to inject the ClassLoader with which to load properties and Behaviours
     * @param classLoader the ClassLoader 
     */
    public static void setClassLoader(ClassLoader classLoader) {
        JUnitAdapter.classLoader = classLoader;
    }

    /**
     * Creates a nested test suite for a given Behaviours instance
     * @return A Test suite
     */
    public static Test suite() {
        return createBehavioursTestSuite(getBehaviours());     
    }

    private static TestSuite createBehavioursTestSuite(Behaviours behaviours) {
        TestSuite suite = new TestSuite(behaviours.toString());         
        Class[] behaviourClasses = behaviours.getBehaviours();
        for ( int i = 0; i < behaviourClasses.length; i++ ){
            suite.addTest(createBehaviourTestSuite(new BehaviourClass(behaviourClasses[i])));
        }
        return suite;
    }

    private static TestSuite createBehaviourTestSuite(BehaviourClass behaviourClass) {
        TestSuite suite = new TestSuite(behaviourClass.toString());         
        BehaviourMethod[] methods = behaviourClass.getBehaviourMethods();
        for ( int i = 0; i < methods.length; i++ ){
            suite.addTest(new JUnitMethodAdapter(methods[i]));
        }
        return suite;
    }

    private static Behaviours getBehaviours() {
        if ( behaviours != null ){
            return behaviours;
        }
        return loadBehaviours();
    }

    private static Behaviours loadBehaviours() {
        ClassLoader behavioursClassLoader = getClassLoader();
        Properties properties = loadProperties(behavioursClassLoader);
        if ( findProperty(properties, "behavioursClass")){
            return (Behaviours) newInstance(loadClass(getProperty(properties, "behavioursClass"), behavioursClassLoader));
        } else if ( findProperty(properties, "behaviourClass")){
            return new BehavioursAdapter(loadClass(getProperty(properties, "behaviourClass"), behavioursClassLoader));
        }
        throw new JBehaveFrameworkError("Could not load behaviours from properties "+properties);
    }

    private static boolean findProperty(Properties properties, String key) {
        return properties.containsKey(key) || System.getProperty(key) != null ;
    }

    private static String getProperty(Properties properties, String key) {
        String property = properties.getProperty(key);
        if ( property == null ) {
            property = System.getProperty(key);
        }
        return property;
    }
    
    private static ClassLoader getClassLoader() {
        if ( classLoader == null ){
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        return classLoader;
    }

    private static Properties loadProperties(ClassLoader classLoader) {
        try {
            // get system properties
            Properties properties = new Properties(System.getProperties());
            // get jbehave.properties 
            InputStream in = classLoader.getResourceAsStream("jbehave.properties");
            if (in != null) {
                properties.load(in);
                in.close();
            }
            return properties;
        } catch ( IOException e) {
            throw new JBehaveFrameworkError("Could not load JBehave properties - either from system or from jbehave.properties");
        }        
    }

    private static Class loadClass(String className, ClassLoader classLoader) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new JBehaveFrameworkError("Could not load class for name " + className + " from classloader "+ classLoader);
        }
    }
    
    private static Object newInstance(Class instantiableClass) {
        try {
            return instantiableClass.newInstance();
        } catch ( Exception e) {
            throw new JBehaveFrameworkError("Could not instantiate class "+instantiableClass);
        }
    }

    /**
     * An adapter that creates a Behaviours instance from a singe behaviour class.
     * 
     * @author Mauro Talevi
     */
    public static final class BehavioursAdapter implements Behaviours {

        final private Class behaviour;

        public BehavioursAdapter(Class behaviour) {
            this.behaviour = behaviour;
        }

        public Class[] getBehaviours() {
            return new Class[]{behaviour};
        }
        
        public String toString() {
            return "BehavioursAdapter: " + behaviour.getName();
        }
        
    }

}