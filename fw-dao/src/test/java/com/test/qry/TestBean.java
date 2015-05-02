package com.test.qry;

public class TestBean
{
	private String name;
	private Integer age;

		public TestBean()
		{
			super();
		}
	
		public TestBean(String name,Integer age)
		{
			super();
			this.name=name;
			this.age=age;
		}
	
		public String getName()
		{
			return name;
		}
	
		public void setName(String name)
		{
			this.name=name;
		}
	
		public Integer getAge()
		{
			return age;
		}
	
		public void setAge(Integer age)
		{
			this.age=age;
		}

}
