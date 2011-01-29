/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2011 Matteo Miraz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package testful.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import testful.runner.TestfulClassLoader;
import testful.utils.Timer;

/**
 * ClazzRegistry is a Façade to the remote class-loader,
 * and allows one to convert testful.model's element into java.lang.reflect ones.
 * @author matteo
 */
public class ClazzRegistry {

	private static final Logger logger = Logger.getLogger("testful.model.clazzRegistry");

	private static final Timer timerClass = Timer.Disabled.singleton; // Timer.getTimer();

	/** When loaded by a TestfulClassLoader, the singleton stores the ClazzRegistry to use. */
	public static final ClazzRegistry singleton;

	static {
		ClassLoader loader = ClazzRegistry.class.getClassLoader();

		ClazzRegistry tmp = null;
		if(loader instanceof TestfulClassLoader) {
			tmp = new ClazzRegistry((TestfulClassLoader) loader);
		}
		singleton = tmp;
	}

	private final TestfulClassLoader loader;
	public ClazzRegistry(TestfulClassLoader loader) {
		this.loader = loader;
	}

	private final Map<Clazz, Class<?>> clazzCache = new HashMap<Clazz, Class<?>>();
	public Class<?> getClass(Clazz clazz) throws ClassNotFoundException {

		timerClass.start("clazzRegistry.getClass");

		Class<?> cache = clazzCache.get(clazz);
		if(cache != null) {
			timerClass.stop();
			return cache;
		}

		final Class<?> ret;
		if(clazz instanceof PrimitiveClazz) {
			switch(((PrimitiveClazz) clazz).getType()) {
			case BooleanClass:
				ret = Boolean.class;
				break;
			case BooleanType:
				ret = Boolean.TYPE;
				break;

			case ByteClass:
				ret = Byte.class;
				break;
			case ByteType:
				ret = Byte.TYPE;
				break;

			case CharacterClass:
				ret = Character.class;
				break;
			case CharacterType:
				ret = Character.TYPE;
				break;

			case DoubleClass:
				ret = Double.class;
				break;
			case DoubleType:
				ret = Double.TYPE;
				break;

			case FloatClass:
				ret = Float.class;
				break;
			case FloatType:
				ret = Float.TYPE;
				break;

			case IntegerClass:
				ret = Integer.class;
				break;
			case IntegerType:
				ret = Integer.TYPE;
				break;

			case LongClass:
				ret = Long.class;
				break;
			case LongType:
				ret = Long.TYPE;
				break;

			case ShortClass:
				ret = Short.class;
				break;
			case ShortType:
				ret = Short.TYPE;
				break;

			default:
				logger.warning("Primitive type not known: " + this);
				ret = null;
				break;
			}

		} else {
			ret = loader.loadClass(clazz.getClassName());
		}

		clazzCache.put(clazz, ret);
		timerClass.stop();

		return ret;
	}

	public Class<?>[] getClasses(Clazz[] c) throws ClassNotFoundException {

		Class<?>[] ret = new Class<?>[c.length];
		for (int i = 0; i < ret.length; i++)
			ret[i] = getClass(c[i]);

		return ret;
	}

	public Field getField(StaticValue value) throws ClassNotFoundException, SecurityException, NoSuchFieldException {

		Class<?> declaringClass = getClass(value.getDeclaringClass());
		Field field = declaringClass.getField(value.getName());

		return field;
	}

	public Method getMethod(Methodz m) throws ClassNotFoundException, SecurityException, NoSuchMethodException {

		Class<?> c = getClass(m.getClazz());
		Class<?>[] params = getClasses(m.getParameterTypes());
		Method method = c.getMethod(m.getName(), params);

		return method;
	}

	public Constructor<?> getConstructor(Constructorz cns) throws ClassNotFoundException, SecurityException, NoSuchMethodException {

		Class<?> c = getClass(cns.getClazz());
		Class<?>[] params = getClasses(cns.getParameterTypes());
		Constructor<?> constructor = c.getConstructor(params);

		return constructor;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ClazzRegistry of " + loader;
	}
}