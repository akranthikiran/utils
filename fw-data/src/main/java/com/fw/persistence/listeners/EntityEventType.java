package com.fw.persistence.listeners;

/**
 * Type of entity events
 * @author akiran
 */
public enum EntityEventType
{
	/**
	 * Called before saving entity
	 */
	PRE_SAVE, 
	/**
	 * Called after save entity
	 */
	POST_SAVE,
	
	
	/**
	 * Called before entity update
	 */
	PRE_UPDATE, 
	/**
	 * Called after entity update
	 */
	POST_UPDATE,
	
	
	/**
	 * Called before entity delete
	 */
	PRE_DELETE, 
	/**
	 * Called after entity delete
	 */
	POST_DELETE
}
