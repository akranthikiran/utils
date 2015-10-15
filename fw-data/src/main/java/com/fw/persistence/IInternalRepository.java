package com.fw.persistence;

/**
 * Specifies repository methods meant for internal use by fw-data
 * @author akiran
 */
public interface IInternalRepository
{
	/**
	 * Drops corresponding entity table 
	 */
	public void dropEntityTable();
	
	/**
	 * Gets the actual repository type of this instance
	 * @return
	 */
	public Class<?> getRepositoryType();
}
