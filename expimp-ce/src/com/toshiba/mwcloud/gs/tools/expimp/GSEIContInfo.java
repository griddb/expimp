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
 * Export Class used for input / output of execution information file
 *
 * <p>V3.5 Delete ContainerAttribute</p>
 *
 */
public class GSEIContInfo {

	private String m_dbName;
	private String m_containerName;
	private String m_metaFileName;
	private String m_filterCondition;

	public GSEIContInfo(String dbName, String containerName, String metaFileName){
		m_dbName = dbName;
		m_containerName = containerName;
		m_metaFileName = metaFileName;
	}

	public String getContainerName(){
		return m_containerName;
	}
	public String getDbName(){
		return m_dbName;
	}
	public String getMetaFileName(){
		return m_metaFileName;
	}

	public String getFilterCondition(){
		return m_filterCondition;
	}
	public void setFilterCondition(String cond){
		m_filterCondition = cond;
	}

	/**
	 * Check if the full names of the containers match.
	 */
	@Override
	public boolean equals(Object obj){
		if ( this == obj ){
			return true;
		}
		if ( obj == null ){
			return false;
		}
		if ( getClass() != obj.getClass() ){
			return false;
		}
		GSEIContInfo other = (GSEIContInfo)obj;

		if ( m_dbName == null ){
			if ( other.getDbName() != null ){
				return false;
			}
		} else if ( !m_dbName.equalsIgnoreCase(other.getDbName())){
			return false;
		}

		if ( m_containerName == null ){
			if ( other.getContainerName() != null ){
				return false;
			}
		} else if ( !m_containerName.equalsIgnoreCase(other.getContainerName())){
			return false;
		}

		return true;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_dbName==null)? 0: m_dbName.hashCode());
		result = prime * result + ((m_containerName==null)? 0: m_containerName.hashCode());
		return result;
	}
}
