package it.stefano.machinesimulator.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

//@Getter
public class JsonHelper
{	
	private JsonHelper() {
	}

	public static String objectToJson(Object object, boolean prettyFormat) throws JsonProcessingException
	{
		JsonMapper mapper = buildMapper(prettyFormat);
		return mapper.writeValueAsString(object);
	}

	public static void objectToJson(OutputStream os, Object object, boolean prettyFormat) throws IOException
	{
		JsonMapper mapper = buildMapper(prettyFormat);
		mapper.writeValue(os, object);
	}
	
	private static JsonMapper buildMapper(boolean prettyFormat)
	{
		JsonMapper mapper = JsonMapper.builder().configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false).build();

		mapper.configure(SerializationFeature.INDENT_OUTPUT, prettyFormat);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
		mapper.configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true);
		mapper.registerModule(new JavaTimeModule());
		
		return mapper;
	}

	public static <I> I jsonToObject(InputStream is, Class<I> clazz) throws IOException
	{
		JsonMapper mapper = buildMapper(false);
		return mapper.readValue(is, clazz);
	}

	public static <I> I jsonToObject(String value, Class<I> clazz) throws IOException
	{
		JsonMapper mapper = buildMapper(false);
		return mapper.readValue(value, clazz);
	}
}
