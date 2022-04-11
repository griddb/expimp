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

package com.toshiba.mwcloud.gs.tools.common.repository;

import com.toshiba.mwcloud.gs.tools.common.GSCluster;
import com.toshiba.mwcloud.gs.tools.common.GSNode;
import com.toshiba.mwcloud.gs.tools.common.GSUserInfo;
import com.toshiba.mwcloud.gs.tools.common.Repository;

public abstract class ClusterRepository {

	/**
	 * リポジトリ情報を返します。
	 * @return
	 * @throws Exception
	 */
	abstract public Repository readRepository() throws Exception;

	/**
	 * リポジトリ情報を保存します。（JSONのみ有効です。RDBは未サポートで、何も実行されません。）
	 *
	 * @param repository
	 * @throws Exception
	 */
	abstract public void saveRepository(Repository repository) throws Exception;

	/**
	 * クラスタ名を指定して、クラスタ情報を返します。
	 * <p>
	 * 指定されたクラスタが存在しない場合はnullを返します。
	 * @param clusterName
	 * @return
	 * @throws Exception
	 */
	abstract public GSCluster<GSNode> getGSCluster(String clusterName) throws Exception;

	/**
	 * クラスタ名、ホスト名、ポートを指定してノード情報を返します。
	 * <p>
	 * 指定されたクラスタまたはノードが存在しない場合はnullを返します。
	 * @param clusterName
	 * @param host
	 * @param port
	 * @return
	 * @throws Exception
	 */
	abstract public GSNode getGSNode(String clusterName, String host, int port) throws Exception;

	abstract GSUserInfo auth(String clusterName, String userId, String password) throws Exception;

}
