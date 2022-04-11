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

package com.toshiba.mwcloud.gs.tools.expimp;

/**
 * Class used for Export / Import of view definition
 *
 * @since 4.2
 */
public class GSEIViewInfo {

	private String m_dbName;
	private String m_viewName;
	private String m_definition;

	public GSEIViewInfo() {}

	public GSEIViewInfo(String dbName, String viewName, String definition){
		m_dbName = dbName;
		m_viewName = viewName;
		m_definition = definition;
	}

	public String getDbName(){
		return m_dbName;
	}
	public void setDbName(String dbName) {
		m_dbName = dbName;
	}
	public String getViewName(){
		return m_viewName;
	}
	public void setViewName(String viewName) {
		m_viewName = viewName;
	}
	public String getDefinition(){
		return m_definition;
	}
	public void setDefinition(String definition) {
		m_definition = definition;
	}
}
