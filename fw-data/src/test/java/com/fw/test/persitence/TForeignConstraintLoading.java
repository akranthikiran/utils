package com.fw.test.persitence;

import org.testng.annotations.Test;

import com.fw.persistence.repository.RepositoryFactory;

/**
 * Contains test cases to test foreign constrain loading
 * @author akiran
 */
public class TForeignConstraintLoading extends TestSuiteBase
{
	private void test(Class<?> entityType, RepositoryFactory factory, String field)
	{
		
	}
	
	/**
	 * Tests positive cases for relation loading for all types
	 * 		One to One
	 * 		One to Many
	 * 		Many to One
	 * 		Many to Many
	 * 
	 * Also test cases where join table is specified.
	 * 
	 * Test cases where child is loaded first and then child and vise versa.
	 * 
	 * Join tables
	 * 		when column names are specified and when they are not specified
	 * 
	 * @param factory
	 */
	@Test(dataProvider = "repositoryFactories")
	public void testRelationLoading(RepositoryFactory factory)
	{
		
	}

	/**
	 * For each relation type, error test cases when collection is found when not expected
	 * and vise versa
	 * @param factory
	 */
	@Test(dataProvider = "repositoryFactories")
	public void testError_unexpectedCollection(RepositoryFactory factory)
	{
		
	}

	/**
	 * For each relation, error test cases when mappedBy is mentioned on both sides of relation 
	 * and cases where it is not specified on both sides
	 * @param factory
	 */
	@Test(dataProvider = "repositoryFactories")
	public void testError_doubleMapping(RepositoryFactory factory)
	{
		
	}

	/**
	 * Error test cases where reverse mapping is not proper
	 * Invalid mapping property
	 * @param factory
	 */
	@Test(dataProvider = "repositoryFactories")
	public void testError_invalidReverseMapping(RepositoryFactory factory)
	{
		
	}

	/**
	 * Error test cases where join tables are mentioned on both ends
	 * @param factory
	 */
	@Test(dataProvider = "repositoryFactories")
	public void testError_doubleJoinTables(RepositoryFactory factory)
	{
		
	}

	/**
	 * Test cases where join table is not specified for many-to-many relation
	 * @param factory
	 */
	@Test(dataProvider = "repositoryFactories")
	public void testError_missingJoinTable(RepositoryFactory factory)
	{
		
	}

	/**
	 * Error cases where non-entity is specified for collection relation
	 * @param factory
	 */
	@Test(dataProvider = "repositoryFactories")
	public void testError_nonEntityCollection(RepositoryFactory factory)
	{
		
	}

	/**
	 * Error cases where type variables are used in collection
	 * @param factory
	 */
	@Test(dataProvider = "repositoryFactories")
	public void testError_typeVariableCollection(RepositoryFactory factory)
	{
		
	}
}
