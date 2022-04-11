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

import java.text.SimpleDateFormat;
import java.util.Date;

public class Report {
	Date time;
	String taskId;
	String jobId;
	String containerName;
	public long rowNum;
	public long estimatedAmount;

	public Report(Date time, String taskId, String jobId,
			String containerName, long rowNum, long estimatedAmount) {
		super();
		this.time = time;
		this.taskId = taskId;
		this.jobId = jobId;
		this.containerName = containerName;
		this.rowNum = rowNum;
		this.estimatedAmount = estimatedAmount;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		//SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
		sb.append("[");
		sb.append(taskId);
		sb.append("] ");
		//sb.append(sdf.format(time));
		sb.append("[");
		sb.append(jobId);
		sb.append("]");
		sb.append(" has ");
		sb.append(rowNum);
		sb.append(" rows. (");
		sb.append(estimatedAmount);
		sb.append(" bytes)");
		return sb.toString();
	}

}
