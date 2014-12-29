package utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Class to create a callback to a function
 * @author Kennard
 *
 */
public class Callback {
	
	private String methodName;
    private Object scope;
    
    /**
     * 
     * @param scope Object that contains the method you want to callback
     * @param methodName Method name without "()"
     */
    public Callback(Object scope, String methodName) {
        this.methodName = methodName;
        this.scope = scope;
    }
    
    /**
     * Invoke the callback
     * @param parameters 
     * @return Result of callback method
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     */
    public Object invoke(Object... parameters) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = this.scope.getClass().getMethod(this.methodName, getParameterClasses(parameters));
        return method.invoke(this.scope, parameters);
    }
    
    private Class[] getParameterClasses(Object... parameters) {
        Class[] classes = new Class[parameters.length];
        for (int i=0; i < classes.length; i++) {
            classes[i] = parameters[i].getClass();
        }
        return classes;
    }
}
