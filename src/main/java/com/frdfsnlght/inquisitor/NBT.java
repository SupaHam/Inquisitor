/*
 * Copyright 2012 frdfsnlght <frdfsnlght@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frdfsnlght.inquisitor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.server.NBTBase;
import net.minecraft.server.NBTTagByte;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagDouble;
import net.minecraft.server.NBTTagFloat;
import net.minecraft.server.NBTTagInt;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagLong;
import net.minecraft.server.NBTTagShort;
import net.minecraft.server.NBTTagString;

/**
 * 
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class NBT {

	private static Method NBTTCCollection = null;

	private static Method getCollectionMethod() {
		if (NBTTCCollection != null)
			return NBTTCCollection;
		Utils.debug("looking for NBTTagCompound collection method...");
		for (Method method : NBTTagCompound.class.getMethods()) {
			if (method.getParameterTypes().length != 0) {
				Utils.debug("rejected NBTTagCompound.%s: too many parameters",
						method.getName());
				continue;
			}
			if (method.getReturnType() != Collection.class) {
				Utils.debug("rejected NBTTagCompound.%s: wrong return type",
						method.getName());
				continue;
			}
			if (NBTTCCollection == null) {
				NBTTCCollection = method;
				Utils.debug("found NBTTagCompound.%s", method.getName());
			} else
				Utils.warning("found additional NBTTagCompound.%s",
						method.getName());
		}
		if (NBTTCCollection == null)
			throw new UnsupportedOperationException(
					"Unable to find collection method in NBTTagCompound!!!");
		return NBTTCCollection;
	}

	public static TypeMap encodeNBT(NBTTagCompound tag) {
		Method method = getCollectionMethod();

		if (tag == null)
			return null;

		TypeMap map = new TypeMap();
		Collection col = null;
		try {
			col = (Collection) method.invoke(tag, new Object[] {});
			Utils.debug("successfully called NBTTagCompound.%s",
					method.getName());
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}

		for (Object object : col) {
			if (object instanceof NBTBase) {
				if (object instanceof NBTTagCompound) {
					map.set(((NBTTagCompound) object).getName(),
							encodeNBT((NBTTagCompound) object));
				} else {
					map.set(((NBTBase) object).getName(),
							encodeNBTValue(((NBTBase) object)));
				}
			}
		}
		return map;
	}

	public static NBTTagCompound decodeNBT(TypeMap map) {
		if (map == null)
			return null;
		NBTTagCompound tag = new NBTTagCompound();
		for (String key : map.getKeys()) {
			Object value = map.get(key);
			tag.set(key, decodeNBTValue(value));
		}
		return tag;
	}

	private static Object encodeNBTValue(NBTBase tag) {
		if (tag instanceof NBTTagCompound)
			return encodeNBT((NBTTagCompound) tag);
		if (tag instanceof NBTTagString)
			return ((NBTTagString) tag).data;
		if (tag instanceof NBTTagList) {
			List<Object> list = new ArrayList<Object>();
			try {
				Field field = tag.getClass().getDeclaredField("list");
				field.setAccessible(true);
				List<?> values = (ArrayList<?>) field.get(tag);
				for (Object object : values) {
					if (object instanceof NBTBase)
						list.add(encodeNBTValue((NBTBase) object));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return list;
		}

		if (tag instanceof NBTTagLong) {
			TypeMap map = new TypeMap();
			map.set("_type_", "long");
			map.set("value", ((NBTTagLong) tag).data);
			return map;
		}
		if (tag instanceof NBTTagInt) {
			TypeMap map = new TypeMap();
			map.set("_type_", "int");
			map.set("value", ((NBTTagInt) tag).data);
			return map;
		}
		if (tag instanceof NBTTagShort) {
			TypeMap map = new TypeMap();
			map.set("_type_", "short");
			map.put("value", ((NBTTagShort) tag).data);
			return map;
		}
		if (tag instanceof NBTTagByte) {
			TypeMap map = new TypeMap();
			map.set("_type_", "byte");
			map.put("value", ((NBTTagByte) tag).data);
			return map;
		}
		if (tag instanceof NBTTagDouble) {
			TypeMap map = new TypeMap();
			map.set("_type_", "double");
			map.put("value", ((NBTTagDouble) tag).data);
			return map;
		}
		if (tag instanceof NBTTagFloat) {
			TypeMap map = new TypeMap();
			map.set("_type_", "float");
			map.put("value", ((NBTTagFloat) tag).data);
			return map;
		}

		return null;
	}

	private static NBTBase decodeNBTValue(Object value) {
		if (value instanceof String)
			return new NBTTagString(null, (String) value);

		if (value instanceof Collection) {
			Collection<?> list = (Collection<?>) value;
			NBTTagList tagList = new NBTTagList();
			Class<?> type = null;
			for (Object object : list) {
				NBTBase tagValue = decodeNBTValue(object);
				if (type == null)
					type = tagValue.getClass();
				else if (type != tagValue.getClass())
					continue;
				tagList.add(tagValue);
			}
			return tagList;
		}

		if (value instanceof TypeMap) {
			TypeMap map = (TypeMap) value;
			String type = map.getString("_type_");
			if (type == null) {
				NBTTagCompound tag = new NBTTagCompound();
				for (String key : map.getKeys())
					tag.set(key, decodeNBTValue(map.get(key)));
				return tag;
			}

			if (type.equals("long"))
				return new NBTTagLong(null, map.getLong("value"));
			if (type.equals("int"))
				return new NBTTagInt(null, map.getInt("value"));
			if (type.equals("short"))
				return new NBTTagShort(null, map.getShort("value"));
			if (type.equals("byte"))
				return new NBTTagByte(null, map.getByte("value"));
			if (type.equals("double"))
				return new NBTTagDouble(null, map.getDouble("value"));
			if (type.equals("float"))
				return new NBTTagFloat(null, map.getFloat("value"));

		}

		return null;
	}

}
