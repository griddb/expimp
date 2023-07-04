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
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
//import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toshiba.mwcloud.gs.tools.common.data.ToolConstants;
import com.toshiba.mwcloud.gs.tools.common.data.ToolConstants.RowFileType;
import com.toshiba.mwcloud.gs.tools.expimp.GSConstants.CMD_NAME;
import com.toshiba.mwcloud.gs.tools.expimp.GSConstants.TARGET_TYPE;
import com.toshiba.mwcloud.gs.tools.expimp.util.Utility;

/**
 * The command analyze class
 *
 */
public class cmdAnalyze {
	/**
	 *
	 */
	private boolean silent;

	/**
	 * Internationalized message resource
	 */
	private static ResourceBundle messageResource;

	/**
	 * Logger class settings
	 */
	private static final Logger log = LoggerFactory.getLogger(cmdAnalyze.class);

	/**
	 * Constructor
	 *
	 * @param silent_flag
	 *            The silent_flag parameter
	 * @param verbose_flag
	 *            The verbose_flag parameter
	 */
	public cmdAnalyze() {
		messageResource = Utility.getResource();
	}

	/**
	 * Parameter analysis
	 *
	 * @param cmdName Command character name
	 * @param args Command parameter string
	 * @return commandLineInfo Command parameter information class
	 */
	public commandLineInfo analyzeParameter(CMD_NAME cmdName, String[] args) {
		commandLineInfo cli = null;
		try {
			// Silent mode is processed with the highest priority.
			if ( silentCheck(args) ){
				silent = true;
			}

			// Initialization of option object
			Options opt = null;
			switch(cmdName){
			case GS_EXPORT:
				opt = setExportOptionParameter();
				break;

			case GS_IMPORT:
				opt = setImportOptionParameter();
				break;
			}

			try {
				// Parameter duplication check
				if (!dupParameterCheck(args, opt)) {
					// "An error occurred in parameter analysis"
					sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_6"));
					log.warn(messageResource.getString("MESS_COMM_ERR_CMD_6"));
					displayHelp(cmdName, opt);
					return null;
				}

				// Command line parameter analysis
				CommandLineParser parser = new GnuParser();
				CommandLine commandLine = parser.parse(opt, args);	// Default of 3rd argument is false

				if (commandLine.hasOption("version")) {
					if (commandLine.hasOption("h")) {
						// "[-h | --help] and --version cannot be set at the same time."
						sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_3"));
						log.warn(messageResource.getString("MESS_COMM_ERR_CMD_3"));
						displayHelp(cmdName, opt);
						return null;
					} else {
						DisplayVersion(cmdName);
						System.exit(0);
					}
				}

				if (commandLine.hasOption("h")) {
					if(commandLine.getOptions().length > 1){
						sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_8"));
						log.warn(messageResource.getString("MESS_COMM_ERR_CMD_8"));
						displayHelp(cmdName, opt);
						return null;
					}else{
						displayHelp(cmdName, opt);
						System.exit(0);
					}
				}

				cli = analyzeParameter_Body(cmdName, commandLine);
				if (cli == null) {
					// "An error occurred in parameter analysis. Processing will be canceled."
					sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_6"));
					log.info(messageResource.getString("MESS_COMM_ERR_CMD_6"));
					displayHelp(cmdName, opt);
					return null;
				}

				return cli;

			} catch (UnrecognizedOptionException e) {
				// "Invalid parameters have been set. Processing will be canceled."
				String errMsg = messageResource.getString("MESS_COMM_ERR_CMD_5")+ ":"+ e.toString();
				sysoutString(errMsg);
				log.warn(errMsg, e);
				displayHelp(cmdName, opt);

			} catch (org.apache.commons.cli.MissingArgumentException e) {
				// "No arguments are set for the parameter"
				String errMsg = messageResource.getString("MESS_COMM_ERR_CMD_37")+ ":"+ e.getMessage();
				sysoutString(errMsg);
				sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_6"));
				log.error(errMsg, e);
				displayHelp(cmdName, opt);

			} catch (ParseException e) {
				// "An error occurred in parameter analysis. Processing will be canceled."
				String errMsg = messageResource.getString("MESS_COMM_ERR_CMD_6")+ ":"+ e.toString();
				sysoutString(errMsg);
				log.error(errMsg, e);
				displayHelp(cmdName, opt);
			}

		} catch (Exception e) {
			// "An error occurred in the parameter analysis processing. The processing will be canceled."
			String errMsg = messageResource.getString("MESS_COMM_ERR_CMD_7")+ ":" + e.toString();
			sysoutString(errMsg);
			log.error(errMsg, e);
		}

		return null;
	}

	/**
	 * Detailed parameter analysis
	 *
	 * @param cmdString Command character name
	 * @param commandLine Command parameter information after analysis
	 * @return commandLineInfo Command parameter information class
	 * @throws GSEIException
	 */
	private commandLineInfo analyzeParameter_Body(CMD_NAME cmdString, CommandLine commandLine) throws GSEIException {
		// Initialization of internal data
		List<String> dbList_param = null;
		List<String> container_param = null;
		int count_param = -1;
		String directory_param = ".";
		boolean list_flag_param = false;
		String optionMsg = "";
		TARGET_TYPE targetType = TARGET_TYPE.NONE;
		SimpleDateFormat sdf = new SimpleDateFormat(GSConstants.DATE_INTERVAL_FORMAT);//インターバルのデータフォーマット

		try {
			// Initialization of CommandLineInfo object
			commandLineInfo cli = new commandLineInfo();
			cli.setCmdName(cmdString.toString());

			// Whether the JDBC driver is valid
			try {
				Class.forName("com.toshiba.mwcloud.gs.sql.Driver");
			} catch (Exception e) {
				cli.setJdbcEnabled(false);
			}

			// Detection of invalid parameters
			if (commandLine.getArgs().length > 0) {
				// "Invalid parameter is set"
				StringBuilder sb = new StringBuilder(messageResource.getString("MESS_COMM_ERR_CMD_8")+ ":");
				List<String> tmp = Arrays.asList(commandLine.getArgs());
				sb.append(tmp.toString());
				sysoutString(sb.toString());
				log.warn(sb.toString());
				return null;
			}

			// Priority parameter (h/version)-Required parameters (u/c/all)-Exclusive parameter check

			// BOTH EXPORT IMPORT
			if (commandLine.hasOption("v")) {
				cli.setVerboseFlag(true);
				optionMsg += "--verbose ";
			}

			// BOTH EXPORT IMPORT
			// Initialize server information (initialize with property file)
			try {
				propertiesInfo pInfo = new propertiesInfo();

				// Read the value from the properties file and set it in commandLineInfo
				pInfo.loadConfig(cli);

			} catch (Exception e) {
				// "Failed to initialize from profile file."
				sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_9")+ ":" + e.toString());
				log.warn(messageResource.getString("MESS_COMM_ERR_CMD_9") + ":"+ e.toString(), e);
				return null;
			}

			//=======================================================
			// Required option
			//=======================================================
			//-------------------------
			// Processing target / function option
			//-------------------------
			// BOTH EXPORT IMPORT
			// Container name specification
			if (commandLine.hasOption("c")) {
				targetType = TARGET_TYPE.CONTAINER_NAME;
				container_param = Arrays.asList(commandLine.getOptionValues("c"));// The presence or absence of the container name string is checked first with the commandLine parser.
				optionMsg+=" --container="+container_param;

				// Deduplication in order (Deduplication case is performed in a container selection process)
				Set<String> set = new LinkedHashSet<String>(container_param);
				container_param = new ArrayList<String>(set);

				cli.setContainerNameList(container_param);
			}
			// BOTH EXPORT IMPORT
			// Container regular expression specification
			if (commandLine.hasOption("containerregex")) {	// container and container regex can be specified at the same time
				targetType = TARGET_TYPE.CONTAINER_NAME;
				container_param = Arrays.asList(commandLine.getOptionValues("containerregex"));
				optionMsg+=" --containerregex="+container_param;

				// Deduplication in order (Deduplication case is performed in a container selection process)
				Set<String> set = new LinkedHashSet<String>(container_param);
				container_param = new ArrayList<String>(set);

				// Regular expression compilation check
				if ( (container_param = getRegexContainerList(container_param)) == null ) return null;

				cli.setRegexContainerNameList(container_param);
			}
			// BOTH EXPORT IMPORT
			// All container settings
			if (commandLine.hasOption("all")) {
				if (targetType != TARGET_TYPE.NONE){
					sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_16"));// "--all, --db, --container cannot be set at the same time"
					log.warn(messageResource.getString("MESS_COMM_ERR_CMD_16"));
					return null;
				}
				targetType = TARGET_TYPE.ALL;
				optionMsg +=" --all";
			}
			// BOTH EXPORT IMPORT
			// Database specification
			if (commandLine.hasOption("db")) {
				if (targetType != TARGET_TYPE.NONE){
					sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_16"));// "--all, --db, --container cannot be set at the same time"
					log.warn(messageResource.getString("MESS_COMM_ERR_CMD_16"));
					return null;
				}
				targetType = TARGET_TYPE.DB;
				dbList_param = Arrays.asList(commandLine.getOptionValues("db"));
				optionMsg +=" --db="+dbList_param;

				// Deduplication in order (preserve case, only database name is not case-identified)
				Set<String> set = new LinkedHashSet<String>(dbList_param);
				dbList_param = new ArrayList<String>(set);

				// public database name normalization
				for ( String name : dbList_param ){
					if ( ToolConstants.PUBLIC_DB.equalsIgnoreCase(name)){
						dbList_param.set(dbList_param.indexOf(name), ToolConstants.PUBLIC_DB);
						break;
					}
				}
				cli.setDbNameList(dbList_param);
			}
			// IMPORT
			// List display
			if (commandLine.hasOption("l")){
				if ( targetType != TARGET_TYPE.NONE ){
					sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_44"));
					log.warn(messageResource.getString("MESS_COMM_ERR_CMD_44"));
					return null;
				}
				targetType = TARGET_TYPE.ALL;	// When gs_import --list, target all containers
				list_flag_param = true;
				cli.setListFlag(true);
				optionMsg += " --list";

				// When using --list, options other than -d | -f cannot be specified.
				for ( Option opt : commandLine.getOptions() ){
					if ( !opt.getLongOpt().equalsIgnoreCase("directory") && !opt.getLongOpt().equalsIgnoreCase("file")
							&& !opt.getLongOpt().equalsIgnoreCase("list") ){
						if ( commandLine.hasOption(opt.getLongOpt()) ){
							sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_33")+": [--"+opt.getLongOpt()+"]");
							log.warn(messageResource.getString("MESS_COMM_ERR_CMD_33")+": [--"+opt.getLongOpt()+"]");
							return null;
						}
					}
				}
			}

			// Check processing target options
			if (targetType == TARGET_TYPE.NONE){
				sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_41"));
				log.warn(messageResource.getString("MESS_COMM_ERR_CMD_41"));
				return null;
			}
			cli.setTargetType(targetType);

			//-------------------------
			// User
			//-------------------------
			// BOTH EXPORT IMPORT
			// User account information
			if (commandLine.hasOption("u")) {
				String param = commandLine.getOptionValue("u").trim();
				if ( commandLine.hasOption("password")){
					String password = commandLine.getOptionValue("password").trim();
					cli.setUserName(param);
					cli.setPassword(password);
				} else {
					if (param.indexOf("/") != -1) {
						String[] temp = param.split("/");
						if ( (temp.length != 2) || (temp[0].length()==0) || (temp[1].length()==0) ) {
							// The description format of "\" user/password \ "is incorrect"
							sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_15"));
							log.warn(messageResource.getString("MESS_COMM_ERR_CMD_15"));
							return null;
						}
						cli.setUserName(temp[0]);
						cli.setPassword(temp[1]);

					} else {
						cli.setUserName(param);
						log.info("analyzeParameter_Body():User:[" + param+ "],Password Not Set.");
					}
				}
				optionMsg += " --user=["+cli.getUserName()+"]";

			} else {
				if (!list_flag_param){
					// User specification is required except for --list
					sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_38"));
					log.warn(messageResource.getString("MESS_COMM_ERR_CMD_38"));
					return null;
				}
			}

			//=======================================================
			// Optional option
			//=======================================================
			// DB specification at the time of --container
			if ( commandLine.hasOption("prefixdb") ){
				if ( (targetType == TARGET_TYPE.ALL) || (targetType == TARGET_TYPE.DB) ){
					sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_42"));
					log.warn(messageResource.getString("MESS_COMM_ERR_CMD_42"));
					return null;
				}
				dbList_param = new ArrayList<String>();
				String prefix = commandLine.getOptionValue("prefixdb");
				optionMsg += " --prefixdb=["+prefix+"]";
				if ( prefix.equalsIgnoreCase(ToolConstants.PUBLIC_DB) ){
					prefix = ToolConstants.PUBLIC_DB;	// public database name normalization
				}
				dbList_param.add(prefix);
				cli.setDbNameList(dbList_param);

			} else {
				if ( targetType == TARGET_TYPE.CONTAINER_NAME ){
					// If prefixdb is not specified in the container name specification, connect to the default DB
					dbList_param = new ArrayList<String>();
					dbList_param.add(ToolConstants.PUBLIC_DB);
					cli.setDbNameList(dbList_param);
				}
			}


			// Both EXPORT IMPORT (import: commit interval / export: fetch size)
			if (commandLine.hasOption("count")) {
				String param = commandLine.getOptionValue("count").trim();
				try {
					count_param = Integer.parseInt(param);
				} catch (NumberFormatException e) {
					// "The setting of the number of acquisitions / the number of batch commits is incorrect"
					sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_17"));
					log.warn(messageResource.getString("MESS_COMM_ERR_CMD_17")+ ":[" + param + "]", e);
					return null;
				}
				if (cmdString == CMD_NAME.GS_IMPORT) {
					cli.setCommitCount(count_param);
					optionMsg += " --count=["+count_param+"]";
				}
			}
			// Both EXPORT IMPORT
			if (commandLine.hasOption("d")) {
				String param = commandLine.getOptionValue("d").trim();
				try {
					File f = new File(param);
					directory_param = f.getCanonicalPath();
				} catch (IOException e) {
					// "An error occurred while validating the directory path"
					sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_18"));
					log.warn(messageResource.getString("MESS_COMM_ERR_CMD_18")+ ":[" + param + "]", e);
					return null;
				} catch (SecurityException e) {
					// "A security error has occurred in the directory path"
					sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_19"));
					log.warn(messageResource.getString("MESS_COMM_ERR_CMD_19")+ ":[" + param + "]", e);
					return null;
				}
				cli.setDirectoryPath(directory_param);
				optionMsg += " --directory=["+param+"]";

			} else {
				cli.setDirectoryPath(new File(".").getCanonicalPath());
			}

			// BOTH EXPORT IMPORT
			if (commandLine.hasOption("acl")) {
				if ( targetType != TARGET_TYPE.ALL ){
					sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_46")); //Use [-acl] at the same time as the [--all] option
					log.warn(messageResource.getString("MESS_COMM_ERR_CMD_46"));
					return null;
				}
				cli.setAclFlag(true);
				optionMsg +=" --acl";
			}

			// BOTH EXPORT IMPORT
			if (commandLine.hasOption("silent")) {
				cli.setSilentFlag(true);
				optionMsg +=" --slient";
			}

			// BOTH EXPORT IMPORT
			if (commandLine.hasOption("force")) {
				cli.setForceFlag(true);
				optionMsg += " --force";
			}

			// BOTH EXPORT IMPORT
			// エクスポートでファイル出力する期間intervalsを設定する
			// インポートでファイル入力する期間intervalsを設定する
			if (commandLine.hasOption("intervals")) {
				String param = commandLine.getOptionValue("intervals");
				if ( param != null ) {
					// "[--intervals]と[--filterfile]は同時に設定できません"
					if (commandLine.hasOption("filterfile")) {
						sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_58"));
						log.warn(messageResource.getString("MESS_COMM_ERR_CMD_58"));
						return null;
					}
					
					try {
						if ( param.contains(":") ) {
							String[] params = param.split(":");
							String timeZoneId = cli.getIntervalTimeZoneId().replaceAll("UTC", "GMT");// SimpleDateFormatではUTC+HH:MMは使えないのでGMT+HH:MMに変換
							try {
								sdf.setTimeZone(TimeZone.getTimeZone(timeZoneId));// --intervalsのTimeZone(intervalTimeZone)を設定
							} catch (Exception e) {
								sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_57"));// "プロパティ[intervalTimeZone]の値が不正です (タイムゾーン名またはGMT+HH:mm形式で指定してください)"
								log.warn(messageResource.getString("MESS_COMM_ERR_CMD_57")+ ":[" + cli.getIntervalTimeZoneId() + "]", e);
								return null;
							}
							Date[] intervals = new Date[]{ sdf.parse(params[0]), sdf.parse(params[1])};
							if (sdf.parse(params[0]).getTime() > sdf.parse(params[1]).getTime()) {
								sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_56"));// "[--intervals]の値が不正です(yyyyMMdd（始点）:yyyyMMdd（終点）形式で指定して、始点 < 終点となるように指定してください)"
								log.warn(messageResource.getString("MESS_COMM_ERR_CMD_56"));
								return null;
							}
							SimpleDateFormat utcSdf = new SimpleDateFormat(GSConstants.DATE_FORMAT_NOT_TIMEZONE);
							utcSdf.setTimeZone(TimeZone.getTimeZone("UTC"));// GridDBのTimeZoneはUTCのため、UTCに変換
							SimpleDateFormat df = new SimpleDateFormat(GSConstants.DATE_FORMAT_NOT_TIMEZONE);
							Date from = df.parse(utcSdf.format(intervals[0]));
							Date to = df.parse(utcSdf.format(intervals[1]));
							cli.setIntervals(new Date[]{ from, to });
						} else {
							sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_55"));// "[--intervals]の値が不正です(yyyyMMdd:yyyyMMdd形式で指定してください)"
							log.warn(messageResource.getString("MESS_COMM_ERR_CMD_55"));
							return null;
						}
					} catch (Exception e) {
						sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_55"));// "[--intervals]の値が不正です(yyyyMMdd:yyyyMMdd形式で指定してください)"
						log.warn(messageResource.getString("MESS_COMM_ERR_CMD_55")+ ":[" + param + "]", e);
						return null;
					}
				}
				optionMsg += " --intervals=["+param+"]";
			}

			
			// Export-Only option check
			if (cmdString == CMD_NAME.GS_EXPORT) {

				// EXPORT-ONLY Output file name specification
				if (commandLine.hasOption("out")) {
					cli.setOutFlag(true);
					optionMsg += " --out";
					String param = commandLine.getOptionValue("out");
					if ( param != null ){
						File f = new File(param.trim());
						String out_file_param = null;
						if (f.getParent() == null) {
							out_file_param = f.getName();
						} else {
							// You cannot set the folder path in the "--out" parameter.
							sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_20")+": "+param);
							log.warn(messageResource.getString("MESS_COMM_ERR_CMD_20")+": "+param);
							return null;
						}
						// Character limit
						if ( param.length() > 20 ){
							// You can specify up to 20 characters for the [--out] option.
							sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_54")+": "+param);
							log.warn(messageResource.getString("MESS_COMM_ERR_CMD_54")+": "+param);
							return null;
						}

						optionMsg += "=["+out_file_param+"]";
						cli.setOutFileName(out_file_param);
					}
				}

				// EXPORT-ONLY Extract definition file name specification
				if (commandLine.hasOption("filterfile")) {
					String param = commandLine.getOptionValue("filterfile");
					String filterfile_param = param.trim();
					cli.setFilterFileName(filterfile_param);
					cli.setFilterFileFlag(true);
					optionMsg += " --filterfile=["+filterfile_param+"]";
				}

				// EXPORT ONLY
				//if (commandLine.hasOption("compatible")) {
				//	cli.setCompatibleFlag(true);
				//	optionMsg +=" --compatible";
				//}

				// EXPORT ONLY
				if (commandLine.hasOption("test")) {
					cli.setTestFlag(true);
					optionMsg +=" --test";
				}

				// EXPORT ONLY  (Binary mode)
				if (commandLine.hasOption("binary")){
					cli.setRowFileType(RowFileType.BINARY);
					optionMsg +=" --binary";

					String param = commandLine.getOptionValue("binary");
					if ( param != null ){
						param = param.trim();
						boolean sts = true;
						int fileSizeLimit_param = 0;
						try {
							fileSizeLimit_param = Integer.parseInt(param);
						} catch ( NumberFormatException e ){
							sts = false;
						}
						if ( (fileSizeLimit_param <= 0) || (fileSizeLimit_param > 1000)){
							sts = false;
						}
						if ( !sts ){
							// The value of the [--binary] parameter is invalid.
							sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_39")+ ":[" + param + "]");
							log.warn(messageResource.getString("MESS_COMM_ERR_CMD_39")+ ":[" + param + "]");
							return null;
						}
						optionMsg +="=["+fileSizeLimit_param+"]";
						cli.setFileSizeLimit(fileSizeLimit_param);
					}
				}

				// EXPORT ONLY Parallel number
				if (commandLine.hasOption("parallel")){
					boolean sts = true;
					String param = commandLine.getOptionValue("parallel").trim();
					int parallel_param = 0;
					try {
						parallel_param = Integer.parseInt(param);
					} catch ( NumberFormatException e ){
						sts = false;
					}
					if ( (parallel_param < GSConstants.PARALLEL_MIN) || (parallel_param > GSConstants.PARALLEL_MAX) ){
						sts = false;
					}
					if ( !sts ){
						sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_45")+ ":[" + param + "]");
						log.warn(messageResource.getString("MESS_COMM_ERR_CMD_45")+ ":[" + param + "]");
						return null;
					}

					// Specify at the same time as the binary multi-output format
					if ( (cli.getRowFileType() != RowFileType.BINARY) || (!cli.getOutFlag()) ){
						sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_47")+ ":[" + param + "]");
						log.warn(messageResource.getString("MESS_COMM_ERR_CMD_47")+ ":[" + param + "]");
						return null;
					}

					cli.setParallelCount(parallel_param);
					optionMsg +=" --parallel=["+parallel_param+"]";
				}

				// EXPORT ONLY Export only container definitions
				if (commandLine.hasOption("schemaOnly")) {
					cli.setSchemaOnlyFlag(true);
					optionMsg +=" --schemaOnly";
				}

			// Import-only option check
			} else if (cmdString == CMD_NAME.GS_IMPORT) {

				// IMPORT ONLY
				if (commandLine.hasOption("f")) {
					List<String> file_param = Arrays.asList(commandLine.getOptionValues("f"));
					optionMsg +=" --file="+file_param;
					cli.setFileNameList(file_param);
				}

				// IMPORT ONLY
				if (commandLine.hasOption("append")) {
					if (commandLine.hasOption("replace")) {
						// "[--append] and [--replase] cannot be set at the same time"
						sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_34"));
						log.warn(messageResource.getString("MESS_COMM_ERR_CMD_34"));
						return null;
					}
					cli.setAppendFlag(true);
					optionMsg += " --append";
				}

				// IMPORT ONLY
				if (commandLine.hasOption("replace")) {
					cli.setReplaceFlag(true);
					optionMsg += " --replace";
				}

				// IMPORT ONLY
				if (commandLine.hasOption("schemaCheckSkip")) {
					cli.setSchemaCheckSkipFlag(true);
					optionMsg += " --schemaCheckSkip";
				}
			}


			/*
			// コンテナ名リストの解析
			if ( targetType == TARGET_TYPE.CONTAINER_NAME ) {
				// 順序を保って重複除去
				Set<String> contList = new LinkedHashSet<String>();
				contList.addAll(container_param);
				container_param = new ArrayList<String>(contList);

				// 正規表現の抽出
				List<String> regexList = getRegexContainerList(container_param);
				if ( regexList == null ) return null;	// 正規表現の文法エラー
				if ( regexList.size() != 0 ){
					// 正規表現あり
					cli.setRegexContainerName(regexList);
					container_param.removeAll(regexList);
					if ( container_param.size() == 0 ){
						targetType = TARGET_TYPE.CONTAINER_EXPR;
					} else {
						targetType = TARGET_TYPE.CONTAINER_NAME_EXPR;
					}
				}
				if ( container_param.size() != 0 ) cli.setContainerName(container_param);

				if ( (cli.getRegexContainerName() == null) && (cli.getContainerName() == null) ) {
					// "有効なコンテナ名が設定されていません"
					sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_21"));
					log.warn(messageResource.getString("MESS_COMM_ERR_CMD_21"));
					return null;
				}
			}
			cli.setTargetType(targetType);
			*/

			log.info("Parameter :"+optionMsg);

			return cli;

		} catch (Exception e) {
			// An error has occurred.
			sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_22")+ ":" + e.getMessage());
			log.error(messageResource.getString("MESS_COMM_ERR_CMD_22") + ":"+ e.toString(), e);
			return null;
		}
	}

	/**
	 * Export command option Parameter initialization
	 *
	 * @return Option information class
	 */
	private Options setExportOptionParameter() {
		return setExportOptionParameter(false);
	}
	private Options setExportOptionParameter(boolean showHelp) {
		Options opt = new Options();
		try {
			OptionBuilder.hasArgs(1);
			OptionBuilder.withArgName("username[/password]");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Server Account Username & Password (specify only the username if it is used with --password option.)");
			OptionBuilder.withLongOpt("user");
			opt.addOption(OptionBuilder.create("u"));

			OptionBuilder.hasArgs(1);
			OptionBuilder.withArgName("password");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Server Account Password");
			OptionBuilder.withLongOpt("password");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.hasArgs();
			OptionBuilder.withArgName("containername...");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Export Container Names");
			OptionBuilder.withLongOpt("container");
			opt.addOption(OptionBuilder.create("c"));

			OptionBuilder.hasArgs();
			OptionBuilder.withArgName("regex ...");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Regular Expressions");
			OptionBuilder.withLongOpt("containerregex");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Get All Containers");
			OptionBuilder.withLongOpt("all");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.hasArgs();
			OptionBuilder.withArgName("databasename...");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Get database");
			OptionBuilder.withLongOpt("db");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.hasArgs(1);
			OptionBuilder.withArgName("databasename");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("prefix database");
			OptionBuilder.withLongOpt("prefixdb");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.hasArgs(1);
			OptionBuilder.withArgName("directorypath");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Output Directory Path");
			OptionBuilder.withLongOpt("directory");
			opt.addOption(OptionBuilder.create("d"));

			OptionBuilder.hasOptionalArg();
			OptionBuilder.withArgName("[filename]");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Switch Single/Multi Output Mode");
			OptionBuilder.withLongOpt("out");
			opt.addOption(OptionBuilder.create());

			// --filterfilename filename
			OptionBuilder.hasArgs(1);
			OptionBuilder.withArgName("filterfilename");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("filter filename");
			OptionBuilder.withLongOpt("filterfile");
			opt.addOption(OptionBuilder.create());

			/*
			// --compatible
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("compatible option");
			OptionBuilder.withLongOpt("compatible");
			opt.addOption(OptionBuilder.create());
			*/

			if ( !showHelp ){
				OptionBuilder.hasArgs(1);
				OptionBuilder.withArgName("maxFetchCount");
				OptionBuilder.isRequired(false);
				OptionBuilder.withDescription("Max Fetch Count");
				OptionBuilder.withLongOpt("count");
				opt.addOption(OptionBuilder.create());
			}

			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Silent Mode");
			OptionBuilder.withLongOpt("silent");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.hasOptionalArg();
			OptionBuilder.withArgName("[fileSizeLimit]");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Binary File Mode");
			OptionBuilder.withLongOpt("binary");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Force Mode");
			OptionBuilder.withLongOpt("force");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Display Tool Version");
			OptionBuilder.withLongOpt("version");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.hasArgs(1);
			OptionBuilder.withArgName("parallelCount");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Parallel Count");
			OptionBuilder.withLongOpt("parallel");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Database/User/Acl");
			OptionBuilder.withLongOpt("acl");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Export no data");
			OptionBuilder.withLongOpt("schemaOnly");
			opt.addOption(OptionBuilder.create());
			
			OptionBuilder.hasArgs(1);
			OptionBuilder.withArgName("intervals...");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Intervals");
			OptionBuilder.withLongOpt("intervals");
			opt.addOption(OptionBuilder.create());

			opt.addOption("v", "verbose", false, "Verbose Mode");
			opt.addOption("t", "test", false, "Test Mode");
			opt.addOption("h", "help", false, "This text");

		} catch (Exception e) {
			// "An error occurred while setting the export parameter analysis information"
			sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_23")+ ":" + e.toString());
			log.error(messageResource.getString("MESS_COMM_ERR_CMD_23") + ":"+ e.toString(), e);
			System.exit(1);
		}
		return opt;
	}

	/**
	 * Import command option Parameter initialization
	 *
	 * @return Option information class
	 */
	private Options setImportOptionParameter() {
		Options opt = new Options();
		try {
			OptionBuilder.hasArgs(1);
			OptionBuilder.withArgName("username/password");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Server Account Username & Password (specify only the username if it is used with --password option.)");
			OptionBuilder.withLongOpt("user");
			opt.addOption(OptionBuilder.create("u"));

			OptionBuilder.hasArgs(1);
			OptionBuilder.withArgName("password");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Server Account Password");
			OptionBuilder.withLongOpt("password");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.hasArgs();
			OptionBuilder.withArgName("containername...");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Import Container Names");
			OptionBuilder.withLongOpt("container");
			opt.addOption(OptionBuilder.create("c"));

			OptionBuilder.hasArgs();
			OptionBuilder.withArgName("regex ...");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Regular Expressions");
			OptionBuilder.withLongOpt("containerregex");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Import All Containers");
			OptionBuilder.withLongOpt("all");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.hasArgs();
			OptionBuilder.withArgName("databasename...");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("import database");
			OptionBuilder.withLongOpt("db");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.hasArgs(1);
			OptionBuilder.withArgName("databasename");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("prefix database");
			OptionBuilder.withLongOpt("prefixdb");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.hasArgs(1);
			OptionBuilder.withArgName("directorypath");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Input Directory Path");
			OptionBuilder.withLongOpt("directory");
			opt.addOption(OptionBuilder.create("d"));

			OptionBuilder.hasArgs();
			OptionBuilder.withArgName("filename...");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Input File Names");
			OptionBuilder.withLongOpt("file");
			opt.addOption(OptionBuilder.create("f"));

			OptionBuilder.hasArgs(1);
			OptionBuilder.withArgName("maxCommitCount");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Max Commit Count");
			OptionBuilder.withLongOpt("count");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Silent Mode");
			OptionBuilder.withLongOpt("silent");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Append/Update Row");
			OptionBuilder.withLongOpt("append");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Replace Container");
			OptionBuilder.withLongOpt("replace");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Same Schema Check Skip");
			OptionBuilder.withLongOpt("schemaCheckSkip");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Database/User/Acl");
			OptionBuilder.withLongOpt("acl");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Force Mode");
			OptionBuilder.withLongOpt("force");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Display Tool Version");
			OptionBuilder.withLongOpt("version");
			opt.addOption(OptionBuilder.create());

			OptionBuilder.hasArgs(1);
			OptionBuilder.withArgName("intervals...");
			OptionBuilder.isRequired(false);
			OptionBuilder.withDescription("Intervals");
			OptionBuilder.withLongOpt("intervals");
			opt.addOption(OptionBuilder.create());

			opt.addOption("l", "list", false, "Display Container List in Local File");
			opt.addOption("v", "verbose", false, "Verbose Mode");
			opt.addOption("t", "test", false, "Test Mode");
			opt.addOption("h", "help", false, "This text");

		} catch (Exception e) {
			// "An error occurred while setting the import parameter analysis information"
			sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_24")+ ":" + e.toString());
			log.error(messageResource.getString("MESS_COMM_ERR_CMD_24") + ":"+ e.toString(), e);
			System.exit(1);
		}
		return opt;
	}

	/**
	 * Help display
	 *
	 * @param cmdString
	 *            Command string
	 * @param opt
	 *            Option information class
	 */
	private void displayHelp(CMD_NAME cmdString, Options opt) {
		try {
			if (!silent) {
				switch(cmdString){
				case GS_EXPORT:
					Options exportOpt = setExportOptionParameter(true);
					displayExportHelp(exportOpt);
					break;
				case GS_IMPORT:
					displayImportHelp(opt);
					break;
				}
			}
		} catch (Exception e) {
			// Since it is help, the error is ignored and only log collection is performed.
			log.error("displayHelp Exception:" + e.toString());
		}
	}

	/**
	 * Export command help display
	 *
	 * @param opt Option information class
	 * @throws Exception that occurs in commons-cli
	 */
	private void displayExportHelp(Options opt) throws Exception {
		HelpFormatter f = new HelpFormatter();
		f.printHelp(HelpFormatter.DEFAULT_WIDTH,
				messageResource.getString("MESS_EXPORT_HELP_1"), "Parameters:",
				opt, messageResource.getString("MESS_EXPORT_HELP_2"));
	}

	/**
	 * Export command help display
	 *
	 * @param opt Option information class
	 * @throws Exception that occurs in commons-cli
	 */
	private void displayImportHelp(Options opt) throws Exception {
		HelpFormatter f = new HelpFormatter();
		f.printHelp(HelpFormatter.DEFAULT_WIDTH,
				messageResource.getString("MESS_IMPORT_HELP_1"), null, new Options(), null);
		f.printHelp(HelpFormatter.DEFAULT_WIDTH,
				messageResource.getString("MESS_IMPORT_HELP_2"), null, new Options(), null);
		f.printHelp(HelpFormatter.DEFAULT_WIDTH,
				messageResource.getString("MESS_IMPORT_HELP_3"), "Parameters:", opt, null);
	}

	/**
	 * Version information display Common to export / import commands
	 *
	 * @param cmdString Command string
	 */
	private void DisplayVersion(CMD_NAME cmdString) {
		if (cmdString.equals(CMD_NAME.GS_EXPORT)) {
			sysoutString(messageResource.getString("MESS_GS_EXPORT_CE") + messageResource.getString("MESS_EXPORT_VERSION"));
		} else {
			sysoutString(messageResource.getString("MESS_GS_IMPORT_CE") + messageResource.getString("MESS_EXPORT_VERSION"));
		}
	}

	/**
	 * Check for duplicate parameters.
	 *
	 * @param param Command parameter string
	 * @param opts Parameter definition
	 * @return true No parameter duplication, false There is a parameter duplication
	 */
	private boolean dupParameterCheck(String[] param, Options opts) {

		try {
			// List of duplicate parameters
			List<String> dup_arg = new ArrayList<String>();

			// List of undefined parameters
			List<String> illigal_arg = new ArrayList<String>();

			// Set of duplicate checks
			HashSet<String> set = new HashSet<String>();

			for (String s : param) {
				boolean match = false;
				@SuppressWarnings("unchecked")
				Collection<Option> options = opts.getOptions();
				for ( Option o : options ){
					String longOpt = o.getLongOpt();
					String shortOpt = o.getOpt();

					if ( s.equalsIgnoreCase("--"+longOpt) || ((shortOpt != null) && s.equalsIgnoreCase("-"+shortOpt)) ){
						if ( !set.add(longOpt) ){
							dup_arg.add(s);
						}
						match = true;
						break;
					}
				}

				if ( !match && s.startsWith("-")){
					illigal_arg.add(s);
				}

			}
			if ( dup_arg.size() != 0 ) {
				// If there is a Hash collision, an error will occur as a parameter duplication
				// "Duplicate parameters cannot be set"
				StringBuilder sb = new StringBuilder(messageResource.getString("MESS_COMM_ERR_CMD_26")+ ":" + " ");
				sb.append(dup_arg.toString());
				sysoutString(sb.toString());
				log.error(sb.toString());
				return false;
			}
			if ( illigal_arg.size() != 0 ) {
				// "Invalid parameters are set"
				StringBuilder sb = new StringBuilder(messageResource.getString("MESS_COMM_ERR_CMD_27")+ ":" + " ");
				sb.append(illigal_arg);
				sysoutString(sb.toString());
				log.error(sb.toString());
				return false;
			}

		} catch (Exception e) {
			// "An error occurred during parameter duplication check processing"
			sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_28")+ e.toString());
			log.error(messageResource.getString("MESS_COMM_ERR_CMD_28")+ e.toString(), e);
			return false;
		}
		return true;
	}

	/**
	 * Extraction of regular expression string entered in container name parameter
	 *
	 * @param str
	 *            List of container name parameters
	 * @return List of regular expression strings
	 */
	private List<String> getRegexContainerList(List<String> containerList) {
		// Compile non-container strings as regular expressions
		for (String s : containerList) {
			try {
				Pattern.compile(s);
			} catch (PatternSyntaxException e) {
				// "The following strings are not available as regular expression strings:"
				sysoutString(messageResource.getString("MESS_COMM_ERR_CMD_36") + ":" + s);
				log.error(messageResource.getString("MESS_COMM_ERR_CMD_36")+ ":[" + s + "]", e);
				return null;
			}
		}

		// "The following string was recognized as a regular expression string:"
		//log.info(messageResource.getString("MESS_COMM_PROC_CMD_5")+ "：" + regtemp.toString());

		return containerList;
	}


	/**
	 * Silent parameter check
	 *
	 * @param args Input parameter string
	 * @return true - with silent parameter, false - without silent parameter
	 */
	boolean silentCheck(String[] args) throws Exception {
		for (String s : args) {
			if (s.equalsIgnoreCase("--silent")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 詳細ログ表示パラメタチェック
	 *
	 * @param args 入力パラメタ文字列
	 * @return true - 詳細ログ表示パラメタあり,false - 詳細ログ表示パラメタなし
	 */
	//boolean verboseCheck(String[] args) throws Exception {
	//	for (String s : args) {
	//		if (s.equalsIgnoreCase("--verbose") || s.equalsIgnoreCase("-v")) {
	//			return true;
	//		}
	//	}
	//	return false;
	//}


	/**
	 * Normal operation log display
	 *
	 * @param message Normal operation log message string
	 */
	private void sysoutString(String message) {
		try {
			if (!silent) {
				System.out.println(message);
			}
		} catch (Exception e) {
		}
	}
}
