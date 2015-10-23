package com.fw.persistence.repository.executors;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.ccg.util.CCGUtility;
import com.fw.persistence.EntityDetails;
import com.fw.persistence.FieldDetails;
import com.fw.persistence.ICrudRepository;
import com.fw.persistence.IDataStore;
import com.fw.persistence.InvalidMappingException;
import com.fw.persistence.Record;
import com.fw.persistence.RecordCountMistmatchException;
import com.fw.persistence.conversion.ConversionService;
import com.fw.persistence.query.FinderQuery;
import com.fw.persistence.repository.InvalidRepositoryException;
import com.fw.persistence.repository.annotations.Field;
import com.fw.persistence.repository.annotations.ResultMapping;
import com.fw.persistence.repository.annotations.SearchResult;

@QueryExecutorPattern(prefixes = {"find", "fetch"})
public class FinderQueryExecutor extends QueryExecutor
{
	private static Logger logger = LogManager.getLogger(FinderQueryExecutor.class);
	
	private Class<?> returnType;
	private Class<?> collectionReturnType = null;
	
	private ReentrantLock queryLock = new ReentrantLock();
	
	/**
	 * Keeps track of different parts required by query
	 */
	private ConditionQueryBuilder conditionQueryBuilder;
	
	/**
	 * Description of the method
	 */
	private String methodDesc;
	
	public FinderQueryExecutor(Class<?> repositoryType, Method method, EntityDetails entityDetails)
	{
		super.repositoryType = repositoryType;
		super.entityDetails = entityDetails;

		conditionQueryBuilder = new ConditionQueryBuilder(entityDetails);
		methodDesc = String.format("finder method '%s' of repository - '%s'", method.getName(), repositoryType.getName());

		Class<?> paramTypes[] = method.getParameterTypes();

		if(paramTypes.length == 0)
		{
			throw new InvalidRepositoryException("No-parameter finder method '" + method.getName() + "' in repository: " + repositoryType.getName());
		}
		
		fetchReturnDetails(method);
		
		if(!fetchConditonsByAnnotations(method, true, conditionQueryBuilder, methodDesc, true) && 
				!fetchConditionsByName(method, conditionQueryBuilder, methodDesc))
		{
			throw new InvalidRepositoryException("Failed to determine parameter conditions for finder method '" 
							+ method.getName() + "' of repository - " + repositoryType.getName());
		}
	}

	/**
	 * Fetches/populates entity fields as result fields
	 */
	private void fetchEntityResultFields()
	{
		logger.trace("Started method: setFullEntityDetails");
		
		//loop through entity details
		for(FieldDetails field: entityDetails.getFieldDetails())
		{
			//if the field is not owned by this table
			if(!field.isTableOwned())
			{
				continue;
			}

			//adds the current field as result field
			conditionQueryBuilder.addResultField(field.getName(), field.getField().getType(), field.getName(), methodDesc);
		}
		
		this.returnType = entityDetails.getEntityType();
	}
	
	/**
	 * Fetches result field from return object type
	 * @param returnType
	 * @param query
	 * @param index
	 */
	private void fetchResultFieldsFromObject(Class<?> returnType)
	{
		java.lang.reflect.Field fields[] = returnType.getDeclaredFields();
		Field resultField = null;
		String name = null;
		
		//loop through query object type fields 
		for(java.lang.reflect.Field objField : fields)
		{
			resultField = objField.getAnnotation(Field.class);
			
			//if field is not marked as condition
			if(resultField == null)
			{
				continue;
			}
			
			//fetch entity field name
			name = resultField.value();
			
			//if name is not specified in condition
			if(name.trim().length() == 0)
			{
				//use field name
				name = objField.getName();
			}
			
			conditionQueryBuilder.addResultField(objField.getName(), objField.getType(), name, methodDesc);
		}
	}
	

	private void fetchReturnDetails(Method method)
	{
		logger.trace("Started method: fetchReturnDetails");
		
		this.returnType = method.getReturnType();

		if(void.class.equals(this.returnType))
		{
			throw new InvalidRepositoryException("Found void finder method '" + method.getName() + "' in repository: " + repositoryType.getName());
		}

		//TODO: Support map types
		if(Collection.class.isAssignableFrom(returnType))
		{
			if(returnType.isAssignableFrom(ArrayList.class))
			{
				this.collectionReturnType = ArrayList.class;
			}
			else if(returnType.isAssignableFrom(HashSet.class))
			{
				this.collectionReturnType = HashSet.class;
			}
			else
			{
				try
				{
					returnType.newInstance();
					this.collectionReturnType = returnType;
				}catch(Exception ex)
				{
					throw new InvalidRepositoryException("Unsupported collection return type found on finder '" 
								+ method.getName() + "' of repository: " + repositoryType.getName());
				}
			}
			
			ParameterizedType type = (ParameterizedType)method.getGenericReturnType();
			Type typeArgs[] = type.getActualTypeArguments();
			
			if(typeArgs.length != 1)
			{
				throw new InvalidRepositoryException("Unsupported collection return type (with mutliple type params) found on finder '" 
							+ method.getName() + "' of repository: " + repositoryType.getName());
			}
			
			this.returnType = (Class<?>)typeArgs[0];
		}
		
		//if return type matches with entity type, add all entity fields as result fields
		if(entityDetails.getEntityType().equals(this.returnType) || ICrudRepository.class.equals(method.getDeclaringClass()))
		{
			fetchEntityResultFields();
		}
		//if method is annotated with Field annotation use that only as return field
		else if(method.getAnnotation(Field.class) != null)
		{
			Field field = method.getAnnotation(Field.class);
			conditionQueryBuilder.addResultField(null, this.returnType, field.value(), methodDesc);
		}
		else if(method.getAnnotation(SearchResult.class) != null)
		{
			SearchResult searchResult = method.getAnnotation(SearchResult.class);
			ResultMapping mappings[] = searchResult.mappings();

			//if mappings are specified fetch field details from bean fields
			if(mappings == null || mappings.length == 0)
			{
				fetchResultFieldsFromObject(returnType);
			}
			//if mappings are specified, add specified mappings to query-builder
			else
			{
				try
				{
					PropertyDescriptor propertyDescriptor = null;
					Object returnSampleBean = this.returnType.newInstance();
					
					for(ResultMapping mapping : mappings)
					{
						propertyDescriptor = PropertyUtils.getPropertyDescriptor(returnSampleBean, mapping.property());
						conditionQueryBuilder.addResultField(mapping.property(), propertyDescriptor.getPropertyType(), mapping.entityField(), methodDesc);
					}
				}catch(Exception ex)
				{
					throw new InvalidMappingException("An error occurred while parsing @SearchResult mappings of " + methodDesc, ex);
				}
			}
		}
		else
		{
			throw new UnsupportedOperationException("Failed to determine return details of finder method: " + method.getName());
		}
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public Object execute(IDataStore dataStore, ConversionService conversionService, Object... params)
	{
		logger.trace("Started method: execute");
		
		queryLock.lock();
		
		try
		{
			FinderQuery finderQuery = new FinderQuery(entityDetails);

			//set the result fields, conditions and tables details on finder query
			conditionQueryBuilder.loadConditionalQuery(finderQuery, params);
			
			//execute the query and fetch records
			List<Record> records = dataStore.executeFinder(finderQuery, entityDetails);
			
			//if no results found
			if(records == null || records.isEmpty())
			{
				//if primitive return type is expected simply return default value
				if(collectionReturnType == null)
				{
					return returnType.isPrimitive() ? CCGUtility.getDefaultPrimitiveValue(returnType) : null;
				}
				
				return Collections.emptyList();
			}

			//if single element is expected as result
			if(collectionReturnType == null)
			{
				if(records.size() > 1)
				{
					throw new RecordCountMistmatchException("Multiple records found when single record is expected.");
				}
				
				ArrayList<Object> resLst = new ArrayList<>();
				conditionQueryBuilder.parseResults(Arrays.asList(records.get(0)), (Class)returnType, resLst, conversionService, persistenceExecutionContext);
				return resLst.get(0);
			}
	
			//if collection of objects are expected as result
			Collection<Object> lst = null;
			
			try
			{
				lst = (Collection)collectionReturnType.newInstance();
			}catch(Exception ex)
			{
				throw new IllegalStateException("An error occurred while creating return collection: " + collectionReturnType.getName(), ex);
			}
			
			//parse records into required types
			conditionQueryBuilder.parseResults(records, (Class)returnType, lst, conversionService, persistenceExecutionContext);
			
			return lst;
		}finally
		{
			queryLock.unlock();
		}
	}
	
	
}
