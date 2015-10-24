package com.fw.persistence.repository.executors;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fw.persistence.FieldDetails;
import com.fw.persistence.ICrudRepository;
import com.fw.persistence.InvalidMappingException;
import com.fw.persistence.repository.InvalidRepositoryException;
import com.fw.persistence.repository.annotations.Field;
import com.fw.persistence.repository.annotations.ResultMapping;
import com.fw.persistence.repository.annotations.SearchResult;

/**
 * Provides common base functionality for search query type executors - Finder and Search queries
 * @author akiran
 */
public abstract class AbstractSearchQuery extends QueryExecutor
{
	private static Logger logger = LogManager.getLogger(AbstractSearchQuery.class);
	
	protected Class<?> returnType;
	protected Class<?> collectionReturnType = null;

	/**
	 * Keeps track of different parts required by query
	 */
	protected ConditionQueryBuilder conditionQueryBuilder;
	
	/**
	 * Description of the method
	 */
	protected String methodDesc;

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
	
	protected void fetchReturnDetails(Method method)
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
			
			//if type variables are used in return type
			if(!(typeArgs[0] instanceof Class))
			{
				//if this is not from ICrudRepository
				if(!ICrudRepository.class.equals(method.getDeclaringClass()))
				{
					throw new InvalidRepositoryException("Type variable are not supported which is found on finder '" 
							+ method.getName() + "' of repository: " + repositoryType.getName());
				}
				
				this.returnType = entityDetails.getEntityType();
			}
			else
			{
				this.returnType = (Class<?>)typeArgs[0];
			}
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

}
