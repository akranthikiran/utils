package com.fw.persistence.utils;

import org.apache.commons.lang.StringUtils;
import org.jasypt.util.password.StrongPasswordEncryptor;

/**
 * Utility to encrypt password and to validate plain password against encrypted password 
 * @author akiran
 */
public class PasswordEncryptor
{
	/**
	 * Password encryption algorithm
	 */
	private static StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
	
	/**
	 * Encrypts the specified password
	 * @param password plain text password
	 * @return Encrypted password
	 */
	public static String encryptPassword(String password)
	{
		if(StringUtils.isEmpty(password))
		{
			return null;
		}
		
		return passwordEncryptor.encryptPassword(password);
	}
	
	/**
	 * Checks and returns specified plain text password is same as encrypted password
	 * @param encryptedPassword
	 * @param plainPassword
	 * @return True is encrypted password same as plain text password
	 */
	public static boolean isSamePassword(String encryptedPassword, String plainPassword)
	{
		//if either of passwords are empty
		if(StringUtils.isEmpty(encryptedPassword) || StringUtils.isEmpty(plainPassword))
		{
			return false;
		}
		
		return passwordEncryptor.checkPassword(plainPassword, encryptedPassword);
	}
}
