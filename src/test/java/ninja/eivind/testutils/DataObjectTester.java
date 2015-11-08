// Copyright 2015 Eivind Vegsundvåg
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
