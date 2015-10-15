package com.fw.persistence;

/**
 * Enumeration of entity relation types.
 * @author akiran
 */
public enum RelationType
{
	ONE_TO_ONE(false, false),
	MANY_TO_ONE(true, false),
	ONE_TO_MANY(false, true),
	MANY_TO_MANY(true, true);
	
	/**
	 * Is the field where relation is defined is expected to be collection
	 */
	private boolean collectionExpected;
	
	/**
	 * Is the mapped field (from target entity) is expected to be collection
	 */
	private boolean collectionTargetExpected;

	private RelationType(boolean collectionExpected, boolean collectionTargetExpected)
	{
		this.collectionExpected = collectionExpected;
		this.collectionTargetExpected = collectionTargetExpected;
	}

	/**
	 * @return the {@link #collectionExpected collectionExpected}
	 */
	public boolean isCollectionExpected()
	{
		return collectionExpected;
	}

	/**
	 * @return the {@link #collectionTargetExpected collectionTargetExpected}
	 */
	public boolean isCollectionTargetExpected()
	{
		return collectionTargetExpected;
	}
	
	
}
