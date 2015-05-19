/**
 * 
 */
package com.fw.utils.test;

/**
 * This is mainly meant to enumerate the test case groups. But made as common code to make it available
 * for all probjects.
 * @author akiran
 */
public interface ITestGroups
{
	/**
	 * Unit test cases. Which are expected to be very fast. 
	 * And generally executed during checkin.
	 */
	public String UNIT_TESTS = "unit_test_cases";
	
	/**
	 * Function test cases. These test-cases are expected to be executed once per day
	 * to ensure no functionality is broken
	 */
	public String FUNCTIONAL_TESTS = "functional_test_cases";
}
