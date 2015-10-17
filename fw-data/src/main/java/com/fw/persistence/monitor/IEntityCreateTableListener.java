package com.fw.persistence.monitor;

import com.fw.persistence.EntityDetails;

/**
 * Listener to observe table creation of entity
 * @author akiran
 */
public interface IEntityCreateTableListener
{
	public void tableCreated(EntityDetails entityDetails);
}
