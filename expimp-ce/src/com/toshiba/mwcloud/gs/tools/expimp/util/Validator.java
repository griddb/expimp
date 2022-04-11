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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.toshiba.mwcloud.gs.tools.expimp.GSConstants;

/**
 * Class that validates character strings, etc.
 */
public class Validator {

	/**
	 * Make sure it is a valid container name.
	 * <p>
	 * The container name can be specified by concatenating the node affinity name
	 * with the at sign "@" after the base container name if necessary.
	 * Only ASCII alphanumeric characters and underscores ("_") can be used in
	 * the base container name and node affinity name.
	 * However, you cannot use a number at the beginning of the base container name.
	 * ASCII uppercase and lowercase letters are equated.
	 * @param name Container name
	 * @return true if valid, false if invalid
	 */
	public static boolean isValidGsContainerName(String name) {
		return matcher(GSConstants.REGEXP_VALID_CONTAINER_NAME, name, true);
	}

	/**
	 * Make sure it is a valid column name.
	 * <p>
	 * Only ASCII alphanumeric characters and underscores ("_") can be used in column names.
	 * However, you cannot use a number at the beginning.
	 * ASCII uppercase and lowercase letters are equated.
	 * @param name Column name
	 * @return true if valid, false if invalid
	 */
	public static boolean isValidGsColumnName(String name) {
		return matcher(GSConstants.REGEXP_VALID_COLUMN_NAME, name, true);
	}

	/**
	 * Matches the regular expression with the target string.
	 * @param regex Regular expression string
	 * @param target Target string
	 * @param isCaseInsensitive true if not case sensitive
	 * @return Whether it matches or not
	 */
	private static boolean matcher(String regex, String target, boolean isCaseInsensitive) {
		Pattern p;
		if (isCaseInsensitive) {
			p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		} else {
			p = Pattern.compile(regex);
		}
		Matcher m = p.matcher(target);
		return m.find();
	}
}
