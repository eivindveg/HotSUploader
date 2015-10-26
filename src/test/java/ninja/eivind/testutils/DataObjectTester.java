package ninja.eivind.testutils;

import ninja.eivind.hotsreplayuploader.models.Account;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.Assert.*;

public class DataObjectTester<T> {

    private Class<T> clazz;
    private T object;

    public DataObjectTester(Class<T> clazz, T object) {
        this.clazz = clazz;
        this.object = object;
    }

    public void testAllFieldsArePrivate() {
        final Field[] fields = Account.class.getDeclaredFields();

        for (final Field field : fields) {
            assertFalse("Field is not accessible", field.isAccessible());
        }
    }

    public void testAllFieldsHaveProperGetters() throws Exception {
        final Field[] fields = clazz.getDeclaredFields();

        for (final Field field : fields) {
            final String fieldName = field.getName();
            char[] fieldNameChars = fieldName.toCharArray();
            fieldNameChars[0] = Character.toUpperCase(fieldNameChars[0]);
            String upperCasedFieldName = new String(fieldNameChars);

            Method method = clazz.getMethod("get" + upperCasedFieldName);
            boolean getterIsAccessible = method.getModifiers() == Modifier.PUBLIC;
            if (getterIsAccessible) {
                field.setAccessible(true);
                Object invoke = method.invoke(object);
                assertEquals("Getter returns an equal object to the field object", field.get(object), invoke);
            }
        }
    }

    public void run() throws Exception {
        testAllFieldsArePrivate();
        testAllFieldsHaveProperGetters();
    }
}
