package com.singularity.clover.babysitter;

public class Global {
	public static int ID_INVALID = -1;
	public static int DATE_INVALID = -1;
	public static final int SQLITE3_FISRT_ID = 1;
	
	public static boolean DEBUG_MODE = false;
	
	public static String PREFIX_SMS = "#Schrodinger's Cat#";
	public static String SMS_SPLIT_INDICATOR_REGEX = "\\|";
	public static String SMS_SPLIT_INDICATOR = "|";
	public static String SUFFIX_SMS = "#end#";
	
	/*修改这些值要修改string.xml中对应值*/
	public static final String PREFIX_SMS_EN ="#Schrodinger's Cat#";
	public static final String SUFFIX_SMS_EN = "#tail#";
	
	public static final String PREFIX_SMS_ZH ="#宝贝乖#";
	public static final String SUFFIX_SMS_ZH = "#尾巴#";
	
	public static String DATE_FORMAT = "yyyy-MM-dd E HH:mm:ss";
	
	public static final String FONT_PATH ="fonts/sthupo_rip.ttf";
	
	/*
	 * SMS body = PREFIX_SMS[UniqueID]|TriggerDate|Description*/
	
	public static int uniqueID(int id){
		int result = (id&0xff)|0x17ff;
		return result;
	}
}
