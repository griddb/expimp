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

import java.util.ArrayList;
import java.util.List;

/**
 * 複数の条件が全て満たされるまで待つためのWatcher。
 * コンストラクタに渡した複数のWatcherが全て完了状態になるまで待つ。
 */
public class CompositeWatcher extends AbstractWatcher {
	private List<Watcher> watchers;

	public CompositeWatcher(List<Watcher> watchers) {
		this.watchers = new ArrayList<Watcher>(watchers);
	}

	@Override
	public boolean isCompleted() {
		for (int i = watchers.size() - 1; i >= 0; --i) {
			if (watchers.get(i).isCompleted()) {
				watchers.remove(i);
			}
		}
		return watchers.isEmpty();
	}
}