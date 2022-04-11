/*
    Copyright (c) 2021 TOSHIBA Digital Solutions Corporation.
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
        http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.toshiba.mwcloud.gs.tools.expimp.util;

import java.util.ResourceBundle;

import com.toshiba.mwcloud.gs.tools.expimp.GSConstants;

public class Utility {

	static ResourceBundle  messageResource;

	public static ResourceBundle getResource(){
		if ( messageResource == null ){
			messageResource = ResourceBundle.getBundle(GSConstants.MESSAGE_RESOURCE);
		}
		return messageResource;
	}


	/** Returns true if the specified string is null or empty. */
	public static boolean isNullOrEmpty(String str) {
		return (str == null || str.length() == 0);
	}

	/*
	public static void compileRegexx(String regex, String prefixDbName){

		String[] tmp = regex.split("\\\\"+ToolConstants.DB_DELIMITER);//正規表現の場合.はエスケープされている

		String dbName = prefixDbName;
		if ( tmp.length == 1 ){
			regex = (dbName==null ?
						"$[^\\.]*"+regex : dbName+"\\"+ToolConstants.DB_DELIMITER+regex);
		} else {
			dbName = tmp[0];
		}
		if ( (dbName != null) && dbName.equalsIgnoreCase(ToolConstants.PUBLIC_DB) ){
			dbName = null;
		}

	}
	*/

}

