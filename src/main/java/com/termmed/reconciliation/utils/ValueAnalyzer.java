/**
 * Copyright (c) 2015 TermMed SA
 * Organization
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */
package com.termmed.reconciliation.utils;


/**
 * The class ValueAnalyzer.
 *
 * @author Alejandro Rodriguez.
 * @version 1.0
 */
public class ValueAnalyzer{

	/**
	 * The Enum OPERATOR.
	 */
	public static enum OPERATOR {/** The greater. */
GREATER,/** The greater equal. */
GREATER_EQUAL,/** The lower. */
LOWER,/** The lower equal. */
LOWER_EQUAL,/** The equal. */
EQUAL,/** The not equal. */
NOT_EQUAL};

	/** The operator. */
	private OPERATOR operator;
	
	/** The filter value. */
	private String filterValue;
	
	/**
	 * Instantiates a new value analyzer.
	 *
	 * @param operator the operator
	 * @param filterValue the filter value
	 */
	public ValueAnalyzer(OPERATOR operator,String filterValue){
		this.operator=operator;
		this.filterValue=filterValue;
	}
	
	/**
	 * String analyze.
	 *
	 * @param value the value
	 * @return true, if successful
	 */
	public boolean StringAnalyze(String value){
		switch (operator){
		case EQUAL:
			return value.equals(filterValue);
		case GREATER:
			return value.compareTo(filterValue)>0;
		case GREATER_EQUAL:
			return value.compareTo(filterValue)>=0;
		case LOWER:
			return value.compareTo(filterValue)<0;
		case LOWER_EQUAL:
			return value.compareTo(filterValue)<=0;
		case NOT_EQUAL:
			return value.compareTo(filterValue)!=0;
			
		}
		return false;
	}
}