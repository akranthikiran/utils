package com.fw.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.Function;

import javax.persistence.CascadeType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.commons.lang.StringUtils;

public class ForeignConstraintDetails
{
	public static final String FOREIGN_CONSTRAINT_PREFIX = "FK_";
	
	/**
	 * Field name of target entity which maintains this relation (foreign key).
	 * And also indicates this entity does not maintain any direct relation
	 */
	private String mappedBy;

	/**
	 * Indicates whether child entities should be removed when parent is removed
	 * (controlled by delete cascade attribute while creating table).
	 */
	private boolean deleteCascaded;

	/**
	 * Provides details of the joining table, required for many-to-many relation
	 */
	private JoinTableDetails joinTableDetails;

	/**
	 * Defines the relation type
	 */
	private RelationType relationType;

	/**
	 * Target entity to which relation is targeted
	 */
	private EntityDetails targetEntityDetails;

	/**
	 * Field on which this relation end is defined
	 */
	private Field ownerField;

	/**
	 * Entity details in which this relation end is defined
	 */
	private EntityDetails ownerEntityDetails;
	
	public ForeignConstraintDetails(String mappedBy, boolean deleteCascaded, RelationType relationType, Field ownerField)
	{
		this.mappedBy = mappedBy;
		this.deleteCascaded = deleteCascaded;
		this.relationType = relationType;
		this.ownerField = ownerField;
	}
	
	/**
	 * Gets the preferred name of this constraint
	 * @return
	 */
	public String getConstraintName()
	{
		return FOREIGN_CONSTRAINT_PREFIX + targetEntityDetails.getTableName().toUpperCase();
	}

	/**
	 * Checks if DELETE cascade is specified in given options
	 * 
	 * @param cascadeOptions
	 * @return
	 */
	private static boolean isDeleteCascaded(CascadeType cascadeOptions[])
	{
		// if no cascade options are specified
		if(cascadeOptions == null)
		{
			// assume child entities should not be removed with parent
			return false;
		}

		// loop through specified options
		for(CascadeType type : cascadeOptions)
		{
			// if remove is specified
			if(type == CascadeType.REMOVE)
			{
				// assume child needs to be deleted with parent
				return true;
			}
		}

		// if remove is not specified, assume child should not be deleted with
		// parent
		return false;
	}

	/**
	 * Fetches the field type. If collection type is expected, then fetches the
	 * element type. Also ensures collection is specified when expected and also
	 * vice-versa.
	 * 
	 * @param entityType
	 * @param field
	 * @param collectionExpected
	 * @param mappingFieldName
	 * @return
	 */
	private static Class<?> getFieldType(Class<?> entityType, Field field, boolean collectionExpected, String mappingFieldName)
	{
		Class<?> fieldType = field.getType();

		// if collection is not expected
		if(!collectionExpected)
		{
			// but if field is collection type
			if(Collection.class.isAssignableFrom(field.getType()))
			{
				// if this is source field
				if(mappingFieldName == null)
				{
					throw new InvalidMappingException(String.format("Found collection field when as per entity-relation collection is not expected. Field '%s.%s'", entityType.getName(), field.getName()));
				}
				// if this is target field (relation other end)
				else
				{
					throw new InvalidMappingException(String.format("Found collection field when as per entity-relation collection is not expected. Field '%s.%s' mapping used by '%s'", entityType.getName(), field.getName(), mappingFieldName));
				}
			}

			return fieldType;
		}

		// if the field is not of collection type
		if(!Collection.class.isAssignableFrom(field.getType()))
		{
			// if this is source field
			if(mappingFieldName == null)
			{
				throw new InvalidMappingException(String.format("Found non-collection field when as per entity-relation collection is expected. Field '%1$s.%2$s'", entityType.getName(), field.getName()));
			}
			// if this is target field (relation other end)
			else
			{
				throw new InvalidMappingException(String.format("Found non-collection field when as per entity-relation collection is expected. Field '%1$s.%2$s' mapping used by '%3$s'", entityType.getName(), field.getName(), mappingFieldName));
			}
		}

		Type genericType = field.getGenericType();

		// if raw field type is used or type-variables are used for collection
		// declaration
		if(!(genericType instanceof ParameterizedType) || !(((ParameterizedType) genericType).getActualTypeArguments()[0] instanceof Class))
		{
			// if this is source field
			if(mappingFieldName == null)
			{
				throw new InvalidMappingException(String.format("For relation processing generic collection is expected without type-variables. Field '%1$s.%2$s'", entityType.getName(), field.getName()));
			}
			// if this is target field (relation other end)
			else
			{
				throw new InvalidMappingException(String.format("For relation processing generic collection is expected without type-variables. Field '%1$s.%2$s' mapping used by '%3$s'", entityType.getName(), field.getName(), mappingFieldName));
			}
		}

		fieldType = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];

		return fieldType;
	}

	/**
	 * Fetches the join table details if any. Fields with mapped relation should
	 * not have {@link JoinTable} annotation. For one-to-many and many-to-many
	 * mappedBy or join-table info is mandatory.
	 * 
	 * @param sourceEntityDetails
	 * @param sourceField
	 * @param mappedBy
	 * @param relationType
	 * @param targetEntityDetails
	 * @return
	 */
	private static JoinTableDetails getJoinTableDetails(EntityDetails sourceEntityDetails, Field sourceField, String mappedBy, RelationType relationType, EntityDetails targetEntityDetails)
	{
		// get join table details
		JoinTable joinTable = sourceField.getAnnotation(JoinTable.class);

		// if @JoinTable is specified on mapped field throw error
		if(StringUtils.isNotEmpty(mappedBy) && joinTable != null)
		{
			throw new InvalidMappingException(String.format("'@JoinTable' is specified on relation-mapped field '%1$s.%2$s' ", sourceEntityDetails.getEntityType().getName(), sourceField.getName()));
		}

		// if join-table is also not specified
		if(joinTable == null)
		{
			// if relation is one-to-many or many-to-many and mappedBy is not
			// specified
			if(relationType.isCollectionTargetExpected() && StringUtils.isEmpty(mappedBy))
			{
				throw new InvalidMappingException(String.format("Both 'mappedBy' and '@JoinTable' is missing for %3$s relation for field '%1$s.%2$s' ", sourceEntityDetails.getEntityType().getName(), sourceField.getName(), relationType));
			}

			return null;
		}

		// get join table column names if specified
		String joinColumn = (joinTable.joinColumns().length > 0) ? joinTable.joinColumns()[0].name() : null;
		String inverseJoinColumn = (joinTable.inverseJoinColumns().length > 0) ? joinTable.inverseJoinColumns()[0].name() : null;

		if(joinColumn == null)
		{
			joinColumn = sourceEntityDetails.getTableName() + "_ID";
		}

		if(inverseJoinColumn == null)
		{
			inverseJoinColumn = targetEntityDetails.getTableName() + "_ID";
		}

		return new JoinTableDetails(joinTable.name(), joinColumn, inverseJoinColumn);
	}

	/**
	 * Fetches foreign constrain details for specified details. And also
	 * validates the field types and mappings
	 * 
	 * @param mappedBy
	 * @param delCascaded
	 * @param relationType
	 * @param sourceEntityDetails
	 * @param sourceField
	 * @param entityDetailsProvider
	 * @return
	 */
	private static ForeignConstraintDetails getForeignConstraint(String mappedBy, boolean delCascaded, RelationType relationType, EntityDetails sourceEntityDetails, Field sourceField, Function<Class<?>, EntityDetails> entityDetailsProvider)
	{
		// if mapped by is not defined (by default it is empty string), make it
		// into null
		mappedBy = StringUtils.isBlank(mappedBy) ? null : mappedBy.trim();

		ForeignConstraintDetails details = new ForeignConstraintDetails(mappedBy, delCascaded, relationType, sourceField);

		Class<?> sourceFieldType = getFieldType(sourceEntityDetails.getEntityType(), sourceField, relationType.isCollectionExpected(), null);

		// fetch target entity details
		EntityDetails targetEntityDetails = entityDetailsProvider.apply(sourceFieldType);

		// ensure target represents an entity type
		if(targetEntityDetails == null)
		{
			throw new InvalidMappingException(String.format("Invalid entity relation-target type '%1' specified for field '%2.%3'", sourceFieldType.getName(), sourceEntityDetails.getEntityType().getName(), sourceField.getName()));
		}

		// set target entity details on result
		details.targetEntityDetails = targetEntityDetails;

		// if mappedBy is used, validate target entity's target field
		if(mappedBy != null)
		{
			FieldDetails targetFieldDetails = targetEntityDetails.getFieldDetailsByField(mappedBy);

			// if mapped field is incorrect
			if(targetFieldDetails == null)
			{
				throw new InvalidMappingException(String.format("Invalid mapping field '%1' specified for field '%2.%3'", mappedBy, sourceEntityDetails.getEntityType().getName(), sourceField.getName()));
			}

			Field targetField = targetFieldDetails.getField();
			Class<?> targetFieldType = getFieldType(targetEntityDetails.getEntityType(), targetField, relationType.isCollectionTargetExpected(), sourceEntityDetails.getEntityType().getName() + "." + sourceField.getName());

			// ensure target field type is matching source entity type
			if(!targetFieldType.equals(sourceEntityDetails.getEntityType()))
			{
				throw new InvalidMappingException(String.format("Entity relation target field's type '%1.%2' is not matching with source entity type '%3'. Relation source field '%3.%4'", targetEntityDetails.getEntityType().getName(), targetField.getName(), sourceEntityDetails.getEntityType().getName(), sourceField.getName()));
			}
		}

		// validate and get join table details, if any
		details.joinTableDetails = getJoinTableDetails(sourceEntityDetails, sourceField, mappedBy, relationType, targetEntityDetails);

		return details;
	}

	/**
	 * Fetches foreign constrain details, if one is specified
	 * 
	 * @param entityDetails
	 * @param field
	 * @param entityDetailsProvider
	 * @return
	 */
	public static ForeignConstraintDetails fetchForeignConstraint(EntityDetails entityDetails, Field field, Function<Class<?>, EntityDetails> entityDetailsProvider)
	{
		// check if field has one to one mapping
		OneToOne oneToOneMapping = field.getAnnotation(OneToOne.class);

		if(oneToOneMapping != null)
		{
			return getForeignConstraint(oneToOneMapping.mappedBy(), isDeleteCascaded(oneToOneMapping.cascade()), RelationType.ONE_TO_ONE, entityDetails, field, entityDetailsProvider);
		}

		// check if the field has many to one mapping
		ManyToOne manyToOneMapping = field.getAnnotation(ManyToOne.class);

		if(manyToOneMapping != null)
		{
			return getForeignConstraint(null, isDeleteCascaded(manyToOneMapping.cascade()), RelationType.MANY_TO_ONE, entityDetails, field, entityDetailsProvider);
		}

		// check if the field has one to many mapping
		OneToMany oneToManyMapping = field.getAnnotation(OneToMany.class);

		if(oneToManyMapping != null)
		{
			return getForeignConstraint(oneToManyMapping.mappedBy(), isDeleteCascaded(oneToManyMapping.cascade()), RelationType.ONE_TO_MANY, entityDetails, field, entityDetailsProvider);
		}

		// check if the field has many to many mapping
		ManyToMany manyToManyMapping = field.getAnnotation(ManyToMany.class);

		if(manyToManyMapping != null)
		{
			return getForeignConstraint(manyToManyMapping.mappedBy(), isDeleteCascaded(manyToManyMapping.cascade()), RelationType.MANY_TO_MANY, entityDetails, field, entityDetailsProvider);
		}

		return null;
	}

	/**
	 * @return the {@link #mappedBy mappedBy}
	 */
	public String getMappedBy()
	{
		return mappedBy;
	}

	/**
	 * @return the {@link #deleteCascaded deleteCascaded}
	 */
	public boolean isDeleteCascaded()
	{
		return deleteCascaded;
	}

	/**
	 * @return the {@link #joinTableDetails joinTableDetails}
	 */
	public JoinTableDetails getJoinTableDetails()
	{
		return joinTableDetails;
	}

	/**
	 * @return the {@link #relationType relationType}
	 */
	public RelationType getRelationType()
	{
		return relationType;
	}

	/**
	 * @return the {@link #targetEntityDetails targetEntityDetails}
	 */
	public EntityDetails getTargetEntityDetails()
	{
		return targetEntityDetails;
	}

	/**
	 * @return the {@link #ownerField ownerField}
	 */
	public Field getOwnerField()
	{
		return ownerField;
	}

	/**
	 * Indicates this is mapped relation. And actual foreign key relation is
	 * maintained by target entity and not this entity.
	 * 
	 * @return
	 */
	public boolean isMappedRelation()
	{
		return (mappedBy != null);
	}

	/**
	 * @return the {@link #ownerEntityDetails ownerEntityDetails}
	 */
	public EntityDetails getOwnerEntityDetails()
	{
		return ownerEntityDetails;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[");

		builder.append("Mapped By: ").append(mappedBy);
		builder.append(",").append("Delete-cascaded: ").append(deleteCascaded);
		builder.append(",").append("Relation Type: ").append(relationType);
		builder.append(",").append("Join Table: ").append(joinTableDetails);
		builder.append(",").append("Target Entity: ").append(targetEntityDetails.getEntityType().getName());

		builder.append("]");
		return builder.toString();
	}

}
