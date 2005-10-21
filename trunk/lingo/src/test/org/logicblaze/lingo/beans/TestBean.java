/*
 *	$Id$
 */

/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.logicblaze.lingo.beans;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Simple test bean used for testing bean factories,
 * AOP framework etc.
 * @author  Rod Johnson
 * @since 15 April 2001
 */
public class TestBean implements BeanFactoryAware, ITestBean, IOther, Comparable {

	private BeanFactory beanFactory;

	private boolean postProcessed;

	private String name;

	private int age;

	private ITestBean spouse;

	private String touchy;

	private String[] stringArray;

	private Date date = new Date();

	private Float myFloat = new Float(0.0);

	private Collection friends = new LinkedList();

	private Set someSet = new HashSet();

	private Map someMap = new HashMap();

	private INestedTestBean doctor = new NestedTestBean();

	private INestedTestBean lawyer = new NestedTestBean();

	private IndexedTestBean nestedIndexedBean;


	public TestBean() {
	}

	public TestBean(String name, int age) {
		this.name = name;
		this.age = age;
	}


	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setPostProcessed(boolean postProcessed) {
		this.postProcessed = postProcessed;
	}

	public boolean isPostProcessed() {
		return postProcessed;
	}

	public String getName() {
        System.out.println("### getting name: " + name + " for: " + this);
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
        System.out.println("### getting age: " + age + " for: " + this);
		return age;
	}

	public void setAge(int age) {
        System.out.println("### setting age to: " + age + " for: " + this);
		this.age = age;
	}

	public ITestBean getSpouse() {
		return spouse;
	}

	public void setSpouse(ITestBean spouse) {
		this.spouse = spouse;
	}

	public String getTouchy() {
		return touchy;
	}

	public void setTouchy(String touchy) throws Exception {
		if (touchy.indexOf('.') != -1) {
			throw new Exception("Can't contain a .");
		}
		if (touchy.indexOf(',') != -1) {
			throw new NumberFormatException("Number format exception: contains a ,");
		}
		this.touchy = touchy;
	}

	public String[] getStringArray() {
		return stringArray;
	}

	public void setStringArray(String[] stringArray) {
		this.stringArray = stringArray;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Float getMyFloat() {
		return myFloat;
	}

	public void setMyFloat(Float myFloat) {
		this.myFloat = myFloat;
	}

	public Collection getFriends() {
		return friends;
	}

	public void setFriends(Collection friends) {
		this.friends = friends;
	}

	public Set getSomeSet() {
		return someSet;
	}

	public void setSomeSet(Set someSet) {
		this.someSet = someSet;
	}

	public Map getSomeMap() {
		return someMap;
	}

	public void setSomeMap(Map someMap) {
		this.someMap = someMap;
	}

	public INestedTestBean getDoctor() {
		return doctor;
	}

	public INestedTestBean getLawyer() {
		return lawyer;
	}

	public void setDoctor(INestedTestBean bean) {
		doctor = bean;
	}

	public void setLawyer(INestedTestBean bean) {
		lawyer = bean;
	}

	public IndexedTestBean getNestedIndexedBean() {
		return nestedIndexedBean;
	}

	public void setNestedIndexedBean(IndexedTestBean nestedIndexedBean) {
		this.nestedIndexedBean = nestedIndexedBean;
	}


	/**
	 * @see org.logicblaze.lingo.beans.ITestBean#exceptional(Throwable)
	 */
	public void exceptional(Throwable t) throws Throwable {
		if (t != null)
			throw t;
	}

	/**
	 * @see org.logicblaze.lingo.beans.ITestBean#returnsThis()
	 */
	public Object returnsThis() {
		return this;
	}

	/**
	 * @see IOther#absquatulate()
	 */
	public void absquatulate() {
	}
	
	public int haveBirthday() {
		return age++;
	}


	public boolean equals(Object other) {
		if (this == other)
			return true;

		if (other == null || !(other instanceof TestBean))
			return false;

		TestBean tb2 = (TestBean) other;
		if (tb2.age != age)
			return false;

		if (name == null)
			return tb2.name == null;

		if (!tb2.name.equals(name))
			return false;

		return true;
	}

	public int compareTo(Object other) {
		if (this.name != null && other instanceof TestBean) {
			return this.name.compareTo(((TestBean) other).getName());
		}
		else {
			return 1;
		}
	}

	public String toString() {
		String s = "name=" + name + "; age=" + age + "; touchy=" + touchy;
		s += "; spouse={" + (spouse != null ? spouse.getName() : null) + "}";
		return s;
	}

}
