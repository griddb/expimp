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

package com.toshiba.mwcloud.gs.tools.common;

/**
 * 一切の待ちを行わないWatcher(NullObjectパターン)。
 * インスタンス化せずに {@code NullWatcher.INSTANCE} を使うこと。
 */
public class NullWatcher implements Watcher {
	public static NullWatcher INSTANCE = new NullWatcher();
	private NullWatcher() {}
	
	@Override
	public boolean isCompleted() {
		return true;
	}
	@Override
	public boolean waitCompletion(int waitSeconds) {
		return true;
	}
}