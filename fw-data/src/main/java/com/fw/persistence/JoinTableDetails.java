package com.fw.persistence;

import java.lang.reflect.Field;

import com.fw.persistence.annotations.DataType;

/**
 * Represents join table (intermediate table) that is needed for many-to-many
 * relation
 * 
 * @author akiran
 */
public class JoinTableDetails
{
	/**
	 * Join table name
	 */
	private String tableName;

	/**
	 * Join column name. Used to link with the owner entity table
	 */
	private String joinColumn;
	
	/**
	 * Data type of join column
	 */
	private DataType joinColumnType;

	/**
	 * Inverse join column name. Used to link with non-owner entity table
	 * (target table)
	 */
	private String inverseJoinColumn;
	
	/**
	 * Data type of inverse join column
	 */
	private DataType inverseJoinColumnType;
	
	/**
	 * Represents this join table as entity details
	 */
	private EntityDetails entityDetailsForm;
	
	/**
	 * Entity details owning this join table
	 */
	private EntityDetails ownerEntityDetails;
	
	/**
	 * Target Entity details by target 
	 */
	private EntityDetails targetEntityDetails;
	
	public JoinTableDetails()
	{}
	
	public JoinTableDetails(String tableName, String joinColumn, DataType joinColumnType, String inverseJoinColumn, DataType inverseJoinColumnType,
			EntityDetails ownerEntityDetails, EntityDetails targetEntityDetails)
	{
		this.tableName = tableName;
		this.joinColumn = joinColumn;
		this.inverseJoinColumn = inverseJoinColumn;
		
		this.joinColumnType = joinColumnType;
		this.inverseJoinColumnType = inverseJoinColumnType;
		
		this.ownerEntityDetails = ownerEntityDetails;
		this.targetEntityDetails = targetEntityDetails;
	}

	/**
	 * @return the {@link #tableName tableName}
	 */
	public String getTableName()
	{
		return tableName;
	}

	/**
	 * @param tableName
	 *            the {@link #tableName tableName} to set
	 */
	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	/**
	 * @return the {@link #joinColumn joinColumn}
	 */
	public String getJoinColumn()
	{
		return joinColumn;
	}

	/**
	 * @param joinColumn
	 *            the {@link #joinColumn joinColumn} to set
	 */
	public void setJoinColumn(String joinColumn)
	{
		this.joinColumn = joinColumn;
	}

	/**
	 * @return the {@link #inverseJoinColumn inverseJoinColumn}
	 */
	public String getInverseJoinColumn()
	{
		return inverseJoinColumn;
	}

	/**
	 * @param inverseJoinColumn
	 *            the {@link #inverseJoinColumn inverseJoinColumn} to set
	 */
	public void setInverseJoinColumn(String inverseJoinColumn)
	{
		this.inverseJoinColumn = inverseJoinColumn;
	}
	
	/**
	 * @return the {@link #joinColumnType joinColumnType}
	 */
	public DataType getJoinColumnType()
	{
		return joinColumnType;
	}

	/**
	 * @param joinColumnType the {@link #joinColumnType joinColumnType} to set
	 */
	public void setJoinColumnType(DataType joinColumnType)
	{
		this.joinColumnType = joinColumnType;
	}

	/**
	 * @return the {@link #inverseJoinColumnType inverseJoinColumnType}
	 */
	public DataType getInverseJoinColumnType()
	{
		return inverseJoinColumnType;
	}

	/**
	 * @param inverseJoinColumnType the {@link #inverseJoinColumnType inverseJoinColumnType} to set
	 */
	public void setInverseJoinColumnType(DataType inverseJoinColumnType)
	{
		this.inverseJoinColumnType = inverseJoinColumnType;
	}

	/**
	 * Converts this join table details to entity details
	 * @return
	 */
	public EntityDetails toEntityDetails()
	{
		//if entity details was already created, reuse it
		if(entityDetailsForm != null)
		{
			return entityDetailsForm;
		}
		
		try
		{
			Class<?> entityType = JoinTableEntity.class;
			
			Field joinColumnField = entityType.getDeclaredField("joinColumn");
			Field invJoinColField = entityType.getDeclaredField("inverseJoinColumn");
			
			//create entity details
			EntityDetails entityDetails = new EntityDetails(tableName, entityType);
			
			FieldDetails joinFieldDetails = new FieldDetails(joinColumnField, joinColumn, joinColumnType);
			joinFieldDetails.setOverriddenColumnName(joinColumn);
			
			FieldDetails invJoinFieldDetails = new FieldDetails(invJoinColField, inverseJoinColumn, inverseJoinColumnType);
			invJoinFieldDetails.setOverriddenColumnName(inverseJoinColumn);
			
			entityDetails.addFieldDetails(joinFieldDetails);
			entityDetails.addFieldDetails(invJoinFieldDetails);
			
			entityDetails.addUniqueKeyConstraint(new UniqueConstraintDetails("JOIN_INV_JOIN", 
					new String[]{"joinColumn", "inverseJoinColumn"}, "Specified relation already exist", true));
		
			entityDetails.addForeignConstraintDetails(new ForeignConstraintDetails(ownerEntityDetails, joinColumnField, entityDetails));
			entityDetails.addForeignConstraintDetails(new ForeignConstraintDetails(targetEntityDetails, invJoinColField, entityDetails));
			
			entityDetails.addIndexDetails(new IndexDetails(tableName.toUpperCase() + "_JOIN_COL", new String[]{"joinColumn"}));
			entityDetails.addIndexDetails(new IndexDetails(tableName.toUpperCase() + "_INV_JOIN_COL", new String[]{"inverseJoinColumn"}));
			
			this.entityDetailsForm = entityDetails;
			
			return entityDetails;
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occurred while converting JoinTableDetails to EntityDetails", ex);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[");

		builder.append("Table: ").append(tableName);
		builder.append(",").append("Join Column: ").append(joinColumn);
		builder.append(",").append("Inverse Join Column: ").append(inverseJoinColumn);

		builder.append("]");
		return builder.toString();
	}

}
