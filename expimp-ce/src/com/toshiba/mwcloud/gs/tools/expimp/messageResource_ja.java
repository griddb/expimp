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
import java.util.ListResourceBundle;

/**
 * 国際化メッセージリソース（ja_JP）
 *
 */
public class messageResource_ja extends ListResourceBundle {




	/**
	 * 国際化リソース
	 */
	static final Object[][] contents = {
			{ "MESS_TOOL_VERSION", messageResource.VERSION_NUM },//
			{ "MESS_GS_EXPORT_CE", messageResource.GS_EXPORT_CE },//
			{ "MESS_GS_IMPORT_CE", messageResource.GS_IMPORT_CE },//
			{ "MESS_IMPORT_PROC_IMPORTMAIN_1", "インポートを開始します" },//
			{ "MESS_IMPORT_PROC_IMPORTMAIN_2", "インポートを開始します（追記モード）" },//
			{ "MESS_IMPORT_PROC_IMPORTMAIN_3", "インポートを開始します（再配置モード）" },//
			{ "MESS_IMPORT_PROC_IMPORTMAIN_4", "インポートを終了しました" },//
			{ "MESS_IMPORT_PROC_IMPORTMAIN_5", "エクスポートデータファイルのコンテナ一覧を表示します" },//

			{ "MESS_TEST_MODE",  "[テストモード]"},

			{ "MESS_TABLE_PARTITION", "[テーブルパーティション]"},
			{ "MESS_TABLE_PARTITION_SUCCESS", "正常終了"},
			{ "MESS_TABLE_PARTITION_ERROR", "一部のデータが未処理"},

			{ "MESS_IMPORT_ERR_IMPORTMAIN_1", "D00101: インポート処理でエラーが発生しました" },//
			{ "MESS_IMPORT_ERR_IMPORTMAIN_2", "D00102: クライアントAPI(gridstore.jar)のバージョンが古い可能性があります"},
			{ "MESS_IMPORT_ERR_IMPORTMAIN_3", "D00103: 必要なクラスが見つかりません"},
			{ "MESS_IMPORT_ERR_IMPORTMAIN_4", "D00104: インポート処理でエラーが発生しました"},

			{ "MESS_IMPORT_PROC_IMPORTPROC_1", "次のコンテナを処理します" },//
			{ "MESS_IMPORT_PROC_IMPORTPROC_2", "コンテナ[" },//
			{ "MESS_IMPORT_PROC_IMPORTPROC_3", "]のインポートに成功しました" },//
			{ "MESS_IMPORT_PROC_IMPORTPROC_4", "]のインポートに失敗しました" },//
			{ "MESS_IMPORT_PROC_IMPORTPROC_5", "intervalsで指定した条件のロウデータは存在しませんでした  : " },

			{ "MESS_IMPORT_ERR_IMPORTPROC_1", "D00201: コンテナ名の取得処理でエラーが発生しました" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_2", "D00202: 指定されたディレクトリにファイルがありません" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_3",
					"D00203: コンテナ名を指定のローカルファイルから取得する処理でエラーが発生しました" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_4",
					"D00204: コンテナ名をすべてのローカルファイルから取得する処理でエラーが発生しました" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_5", "D00205: コンテナ名が設定されていません" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_6", "D00206: ローカルファイルからコンテナ名が検索できませんでした" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_7",
					"D00207: 指定されたコンテナ名、正規表現からコンテナ名が検索できませんでした" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_8", "D00208: インポート処理でエラーが発生しました" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_9", "D00209: インポート処理でエラーが発生しました" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_10", "D00210: スキーマ情報が異なる同一名コンテナがすでに登録されています。スキーマ不一致の箇所はログをご確認ください。" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_11", "D00211: 対応できないコンテナタイプが指定されました" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_12", "D00212: ロウデータを格納するバッファが取得できませんでした" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_13", "D00213: 対応できないロウデータ格納形式が指定されました" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_14", "D00214: 時系列データでは使用できない索引情報が指定されました" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_15", "D00215: ファイルI/O処理でエラーが発生しました" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_16", "D00216: JSONファイルの判定処理でエラーが発生しました" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_17",
					"D00217: JSONファイルからコンテナ名を取得する処理でエラーが発生しました" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_18", "D00218: UTF-8のBOM処理でエラーが発生しました" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_19",
					"D00219: JSONオブジェクトからコンテナ名を取得する処理でエラーが発生しました" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_20", "D00220: 次のファイルは見つからないかJSONファイルではありません" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_21",
					"D00221: 指定されたコンテナ名がないかコンテナ情報をもつファイルがありませんでした" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_22", "D00222: コンテナ情報リストの取得処理でエラーが発生しました" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_23", "D00223: 時系列のロウキーが設定されたカラムに索引は設定できません" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_24", "D00224: 指定のコンテナは既に存在します。操作を実施するためには[--append]または[--replace]パラメタが必要です" },//
			{ "MESS_IMPORT_ERR_IMPORTPROC_25", "D00225: -dオプションで指定されたディレクトリが存在しません" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_26", "D00226: -dオプションで指定されたパスはディレクトリではありません" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_27", "D00227: 指定されたファイルがメタ情報ファイル(*.json)ではありません" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_28", "D00228: -fオプションで指定されたファイルが存在しません" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_29", "D00229: -fオプションで指定された名前はファイルではありません" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_30", "D00230: -fオプションに指定できるのは、ディレクトリを含まないファイル名のみです" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_31", "D00231: -fオプションで指定されたファイルが不正です" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_32", "D00232: 対象コンテナがファイルに存在しません" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_33", "D00233: ロウデータのインポート処理でエラーが発生しました" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_35", "D00235: ACLファイルが存在していません。" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_36", "D00236: 同じユーザ名で異なる設定のユーザが存在します" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_37", "D00237: 同じデータベース名で異なる設定のデータベースが存在します" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_38", "D00238: データベース・ユーザ・ACL設定でエラーが発生しました" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_39", "D00239: インポート先のデータベースが存在しません。管理者ユーザでデータベースを作成してください。" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_3A", "D0023A: インポート先データベースの存在確認・作成でエラーが発生しました" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_3B", "D0023B: gs_export_acl.jsonファイルの読み込みでエラーが発生しました" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_3C", "D0023C: ユーザ作成でエラーが発生しました" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_3D", "D0023D: データベース作成でエラーが発生しました" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_3E", "D0023E: アクセス権設定でエラーが発生しました" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_3F", "D0023F: テーブルパーティションで一部のデータが未処理です" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_40", "D00240: バイナリ形式はV2.7.0以降のファイルを指定してください。" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_41", "D00241: NewSQLのデータが存在するためインポートできません。" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_42", "D00242: NewSQLのデータが既に存在します。forceオプションが指定されているので、NewSQLテーブルの登録はすべてスキップします。" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_43", "D00243: NewSQLデータの事前チェックでエラーが発生しました。" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_44", "D00244: NewSQLのテーブルは--containerオプションに指定できません。" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_45", "D00245: 同じ名前のコンテナが存在します。分割指定のコンテナの場合は、既存コンテナへのデータ追加・再配置は行えません。" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_46", "D00246: メタ情報ファイルの読み込みでエラーが発生しました。" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_47", "D00247: 同名のパーティショニングコンテナが存在するためインポートできません。" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_48", "D00248: パーティショニングテーブルの作成に失敗しました。" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_49", "D00249: パーティショニングテーブルの削除に失敗しました。" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_4A", "D0024A: パーティショニングテーブルの取得に失敗しました。" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_4B", "D0024B: テーブルパーティショニング情報の比較に失敗しました。" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_4C", "D0024C: 期限解放情報の比較に失敗しました。" },
			{ "MESS_IMPORT_ERR_IMPORTPROC_4D", "D0024D: ビュー定義ファイルの読込処理でエラーが発生しました。"},
			{ "MESS_IMPORT_ERR_IMPORTPROC_4E", "D0024E: 同名のコンテナが存在するため、ビューのインポート処理をスキップします。"},
			{ "MESS_IMPORT_ERR_IMPORTPROC_4F", "D0024F: 同名のビューが存在するため、インポート処理をスキップします。"},
			{ "MESS_IMPORT_ERR_IMPORTPROC_50", "D00250: 同名のビューが既に存在します。"},
			{ "MESS_IMPORT_ERR_IMPORTPROC_51", "D00251: ビューの作成または置換処理でエラーが発生しました。"},
			{ "MESS_IMPORT_ERR_IMPORTPROC_52", "D00252: ビューの取得処理でエラーが発生しました。"},
			{ "MESS_IMPORT_ERR_IMPORTPROC_53", "D00253: ビューの削除処理でエラーが発生しました。"},
			{ "MESS_IMPORT_ERR_IMPORTPROC_54", "D00254: ビューの作成処理でエラーが発生しました。"},

			{ "MESS_IMPORT_ERR_GRIDSTORE_1", "D00301: GridDBへのコンテナ作成処理でエラーが発生しました" },

			{ "MESS_EXPORT_PROC_EXPORTMAIN_1", "エクスポートを開始します" },
			{ "MESS_EXPORT_PROC_EXPORTMAIN_2", "エクスポートを終了しました" },

			{ "MESS_EXPORT_ERR_EXPORTMAIN_1", "D00401: エクスポート処理でエラーが発生しました" },
			{ "MESS_EXPORT_ERR_EXPORTMAIN_2", "D00402: クライアントAPI(gridstore.jar)のバージョンが古い可能性があります"},
			{ "MESS_EXPORT_ERR_EXPORTMAIN_3", "D00403: 必要なクラスが見つかりません"},
			{ "MESS_EXPORT_ERR_EXPORTMAIN_4", "D00404: エクスポート処理でエラーが発生しました"},

			{ "MESS_EXPORT_PROC_EXPORTPROC_1", "サーバに接続しました" },//
			{ "MESS_EXPORT_PROC_EXPORTPROC_2", "GridDBからコンテナ名一覧を取得しました" },//
			{ "MESS_EXPORT_PROC_EXPORTPROC_3", "処理対象のコンテナ名一覧を取得しました" },//
			{ "MESS_EXPORT_PROC_EXPORTPROC_4", "コンテナ情報をメタ情報ファイルに出力します" },
			{ "MESS_EXPORT_PROC_EXPORTPROC_5", "コンテナ情報をメタ情報ファイルに出力しました" },
			{ "MESS_EXPORT_PROC_EXPORTPROC_6", "コンテナ情報からロウデータを取得します" },//
			{ "MESS_EXPORT_PROC_EXPORTPROC_7", "ロウデータの取得を完了しました" },
			{ "MESS_EXPORT_PROC_EXPORTPROC_8", "GridDBからコンテナ名一覧を取得しました" },
			{ "MESS_EXPORT_PROC_EXPORTPROC_9", "GridDBからコンテナ名一覧を取得しました" },
			{ "MESS_EXPORT_PROC_EXPORTPROC_10", "GridDBからコンテナ名一覧を取得しました" },
			{ "MESS_EXPORT_PROC_EXPORTPROC_11", "コンテナ情報を格納するファイル名リストを作成しました" },
			{ "MESS_EXPORT_PROC_EXPORTPROC_12", "コンテナ[" },//
			{ "MESS_EXPORT_PROC_EXPORTPROC_13", "]のエクスポートに成功しました" },//
			{ "MESS_EXPORT_PROC_EXPORTPROC_14", "]のエクスポートに失敗しました" },//
			{ "MESS_EXPORT_PROC_EXPORTPROC_15",
					"ロウデータを次のCSV形式ファイルに出力します［テストモード］" },//
			{ "MESS_EXPORT_PROC_EXPORTPROC_16",
					"ロウデータを次のバイナリ形式ファイルに出力します［テストモード］" },//
			{ "MESS_EXPORT_PROC_EXPORTPROC_17", "コンテナ情報を次のメタ情報ファイルに出力します［テストモード］" },//
			{ "MESS_EXPORT_PROC_EXPORTPROC_18", "Export管理情報をファイルに出力します［テストモード］" },
			{ "MESS_EXPORT_PROC_EXPORTPROC_19", "出力ディレクトリ : " },
			{ "MESS_EXPORT_PROC_EXPORTPROC_20", "対象コンテナ数  : " },
			{ "MESS_EXPORT_PROC_EXPORTPROC_21", "ユーザ数        : " },
			{ "MESS_EXPORT_PROC_EXPORTPROC_22", "データベース数  : " },
			{ "MESS_EXPORT_PROC_EXPORTPROC_23", "対象コンテナ数  : %d  スキップ数  : %d" },
			{ "MESS_EXPORT_PROC_EXPORTPROC_24", "対象ビュー数  : %d" },
			{ "MESS_EXPORT_PROC_EXPORTPROC_25", "intervalsで指定した条件のロウデータは存在しませんでした  : "},

			{ "MESS_EXPORT_ERR_EXPORTPROC_1", "D00501: サーバーへの接続が失敗しました" },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_2", "D00502: コンテナ名一覧がGridDBから取得できません" },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_3", "D00503: 処理対象のコンテナ名がないかコンテナ名が一致しません" },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_4", "D00504: エクスポート処理の実行中にエラーが発生しました" },
			{ "MESS_EXPORT_ERR_EXPORTPROC_5", "D00505: ロウファイル処理の実行中にエラーが発生しました" },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_6", "D00506: メタ情報ファイルの作成処理でエラーが発生しました" },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_7", "D00507: GridDB接続処理の実行中にエラーが発生しました" },
			{ "MESS_EXPORT_ERR_EXPORTPROC_8",
					"D00508: 全パーティション情報取得からコンテナ情報を取得する処理の実行中にエラーが発生しました" },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_9", "D00509: コンテナ名の抽出処理の実行中にエラーが発生しました" },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_10", "D00510: コンテナ情報の取得処理の実行中にエラーが発生しました" },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_11", "D00511: 処理の対象となるコンテナが見つかりませんでした" },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_12", "D00512: GridDB上に指定のコンテナが存在しません" },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_13", "D00513: GridDB上のコンテナ情報が取得できませんでした" },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_14",
					"D00514: コンテナ情報からJSONファイルの作成処理でエラーが発生しました" },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_15", "D00515: ファイル名の重複チェック処理の実行中にエラーが発生しました" },//
			{ "MESS_EXPORT_ERR_EXPORTPROC_16", "D00516: 出力ディレクトリの作成処理でエラーが発生しました。"},
			{ "MESS_EXPORT_ERR_EXPORTPROC_17", "D00517: エクスポート対象コンテナ名の取得でエラーが発生しました。"},
			{ "MESS_EXPORT_ERR_EXPORTPROC_18", "D00518: エクスポート対象として該当するコンテナが存在しません。"},
			{ "MESS_EXPORT_ERR_EXPORTPROC_19", "D00519: 指定されたディレクトリには既にExport結果が存在します。"},
			{ "MESS_EXPORT_ERR_EXPORTPROC_20", "D00520: ディレクトリ・ファイルの事前チェックでエラーが発生しました。"},
			{ "MESS_EXPORT_ERR_EXPORTPROC_21", "D00521: GridDBへのコンテナ一覧取得処理がタイムアウトしました。"},
			{ "MESS_EXPORT_ERR_EXPORTPROC_22", "D00522: --filterfileオプションで指定した検索式が誤っている可能性があります。"},
			{ "MESS_EXPORT_ERR_EXPORTPROC_23", "D00523: 検索でエラーが発生しました。"},
			{ "MESS_EXPORT_ERR_EXPORTPROC_24", "D00524: データベース・ユーザ・ACLの取得でエラーが発生しました。"},
			{ "MESS_EXPORT_ERR_EXPORTPROC_25", "D00525: 一般ユーザの場合は--aclは指定できません。"},
			{ "MESS_EXPORT_ERR_EXPORTPROC_26_1", "D00526: パーティション(ID=\""},
			{ "MESS_EXPORT_ERR_EXPORTPROC_26_2", "\")のコンテナ一覧取得でエラーが発生しました。(該当パーティションの全コンテナは処理対象外になります)"},
			{ "MESS_EXPORT_ERR_EXPORTPROC_27", "D00527: テーブルは[--all]または[--db]オプションでのみエクスポートできます。"},
			{ "MESS_EXPORT_ERR_EXPORTPROC_28", "D00528: パーティショニングテーブルはスキップします。"},
			{ "MESS_EXPORT_ERR_EXPORTPROC_29", "D00529: JDBC接続処理の実行中にエラーが発生しました。"},
			{ "MESS_EXPORT_ERR_EXPORTPROC_2A", "D0052A: ビューのエクスポート処理でエラーが発生しました。"},
			{ "MESS_EXPORT_ERR_EXPORTPROC_2B", "D0052B: ビュー定義ファイルの作成処理でエラーが発生しました" },
			{ "MESS_EXPORT_ERR_EXPORTPROC_2C", "D0052C: ビューの取得処理でエラーが発生しました。"},

			{ "MESS_COMM_ERR_PROPINFO_1", "D00601: プロパティ読込処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_PROPINFO_2", "D00602: プロパティファイルが見つかりませんでした。プロパティファイルを作成します。" },
			{ "MESS_COMM_ERR_PROPINFO_3", "D00603: プロパティファイルの書き込みでエラーが発生しました。" },

			{ "MESS_COMM_PROC_ROWCSV_1", "CSV形式ロウデータファイル出力処理を開始します" },
			{ "MESS_COMM_PROC_ROWCSV_2", "次のコンテナを処理します" },
			{ "MESS_COMM_PROC_ROWCSV_3", "次のロウを処理します" },
			{ "MESS_COMM_PROC_ROWCSV_4", "ロウデータをCSV形式ファイルに出力します" },

			{ "MESS_COMM_ERR_ROWCSV_1", "D00701: ロウデータのマルチCSV形式ファイル出力処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_2", "D00702: ロウデータのCSV形式ファイル出力処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_3", "D00703: ロウデータ文字列作成処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_4", "D00704: 処理できないデータ型が検出されました" },
			{ "MESS_COMM_ERR_ROWCSV_5", "D00705: カラムデータの取得処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_6", "D00706: 外部データファイルの出力処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_7", "D00707: 外部BLOBファイルの出力処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_8", "D00708: CSV形式ヘッダ出力処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_9", "D00709: CSV形式メタ情報ファイル出力処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_10", "D00710: CSV形式ロウデータ出力処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_11",
					"D00711: CSV形式ロウデータ出力処理でエラーが発生しました。次のコンテナを処理します" },
			{ "MESS_COMM_ERR_ROWCSV_12", "D00712: GridDBからロウデータを取得できませんでした" },
			{ "MESS_COMM_ERR_ROWCSV_13",
					"D00713: CSV形式ロウデータ出力処理でエラーが発生しました。次のコンテナを処理します" },

			{ "MESS_COMM_ERR_ROWCSV_15", "D00715: CSV形式ファイルからロウデータの読込み処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_16", "D00716: スキーマのカラム数と一致しないロウデータを検出しました" },
			{ "MESS_COMM_ERR_ROWCSV_17", "D00717: 指定されたコンテナ情報のタグが見つかりません" },

			{ "MESS_COMM_ERR_ROWCSV_18", "D00718: GridDBへのロウデータの設定処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_19", "D00719: 本バージョンでは処理できないデータタイプが検出されました" },
			{ "MESS_COMM_ERR_ROWCSV_20", "D00720: 外部データファイルの存在確認処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_21", "D00721: 外部データファイルのGridDB種別が取得できませんでした" },
			{ "MESS_COMM_ERR_ROWCSV_22", "D00722: 外部データファイルのファイル名が取得できませんでした" },
			{ "MESS_COMM_ERR_ROWCSV_23", "D00723: 外部データファイルからデータ読込処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_24", "D00724: ファイルの書出処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_25", "D00725: BLOBの読込処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_26", "D00726: 外部ファイルにアクセスできません" },
			{ "MESS_COMM_ERR_ROWCSV_27", "D00727: CSVファイル作成でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_28", "D00728: CSVファイル作成でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_29", "D00729: Rowデータの書き込みでエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_30", "D00730: 指定されたコンテナのデータが存在しません" },
			{ "MESS_COMM_ERR_ROWCSV_31", "D00731: CSVファイル読み込みでエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_32", "D00732: CSVファイル読み込みでエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_33", "D00733: CSVファイル読み込みでエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWCSV_34", "D00734: BLOBタイプは外部ファイルで指定してください" },
			{ "MESS_COMM_ERR_ROWCSV_35", "D00735: 外部ファイル名指定のカラムタイプが不正です" },
			{ "MESS_COMM_ERR_ROWCSV_36", "D00736: 外部ファイル名の指定が不正です" },
			{ "MESS_COMM_ERR_ROWCSV_37", "D00737: カラムデータの文字列変換処理でエラーが発生しました" },

			{ "MESS_COMM_ERR_ROWBNY_1", "D00801: ロウデータのマルチバイナリ形式ファイル出力処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWBNY_2", "D00802: ロウデータのバイナリ形式ファイル出力処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWBNY_3", "D00803: ZIPファイルチェック処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWBNY_4", "D00804: ZIPファイル作成処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWBNY_5", "D00805: ロウデータファイル作成処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWBNY_6", "D00806: 個別ロウデータファイル処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWBNY_7", "D00807: バイナリ形式ファイルの形式チェック処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWBNY_8",
					"D00808: バイナリ形式ロウデータファイルの作成でエラーがありました。次のコンテナを処理します" },
			{ "MESS_COMM_ERR_ROWBNY_9",  "D00809: バイナリファイル読み込みでエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWBNY_10", "D00810: バイナリ形式ファイルの出力処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWBNY_11", "D00811: GridDBからロウデータを取得できませんでした" },
			{ "MESS_COMM_ERR_ROWBNY_12", "D00812: 処理できないデータ型が検出されました" },
			{ "MESS_COMM_ERR_ROWBNY_13", "D00813: ロウデータへの変換処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWBNY_14",
					"D00814: ロウデータからオブジェクトへの変換処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWBNY_15", "D00815: ZIPファイル圧縮処理で処理するファイルがありませんでした" },
			{ "MESS_COMM_ERR_ROWBNY_16", "D00816: ZIPファイル圧縮処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWBNY_17", "D00817: ZIPファイル解凍処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWBNY_18",
					"D00818: シングルバイナリ(.SC)形式またはマルチバイナリ(.MC)形式のファイル以外のファイルが設定されています" },
			{ "MESS_COMM_ERR_ROWBNY_19", "D00819: 指定されたコンテナのロウデータが見つかりません" },
			{ "MESS_COMM_ERR_ROWBNY_20", "D00820: バイナリ形式のロウデータファイルが見つかりません" },
			{ "MESS_COMM_ERR_ROWBNY_21", "D00821: ファイル削除処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWBNY_22", "D00822: バイナリファイル出力でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWBNY_23", "D00823: バイナリファイル出力でエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWBNY_24", "D00824: バイナリファイル読み込みでエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWBNY_25", "D00825: バイナリファイル読み込みでエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWBNY_26", "D00826: バイナリファイル読み込みでエラーが発生しました" },
			{ "MESS_COMM_ERR_ROWBNY_27", "D00827: バイナリファイル読み込みでエラーが発生しました" },

			{ "MESS_COMM_PROC_ROWBNY_1", "個別のシングルバイナリ形式ロウデータファイルを出力します" },
			{ "MESS_COMM_PROC_ROWBNY_2", "個別シングルバイナリ形式ファイルを結合してファイルに出力します" },
			{ "MESS_COMM_PROC_ROWBNY_3", "シングルバイナリ形式ファイルを次の単位でバッファに出力します" },
			{ "MESS_COMM_PROC_ROWBNY_4", "バッファサイズをバイナリ形式ロウデータファイルに出力します" },
			{ "MESS_COMM_PROC_ROWBNY_5", "バッファのCRCをバイナリ形式ロウデータファイルに出力します" },
			{ "MESS_COMM_PROC_ROWBNY_6", "ファイル情報をバイナリ形式ロウデータファイルに出力します" },
			{ "MESS_COMM_PROC_ROWBNY_7", "バッファデータをバイナリ形式ロウデータファイルに出力します" },
			{ "MESS_COMM_PROC_ROWBNY_8", "バイナリ形式ファイルのファイル名" },

			{ "MESS_COMM_PROC_METAINFO_1", "マルチコンテナのメタ情報ファイル作成処理を開始します" },
			{ "MESS_COMM_PROC_METAINFO_2", "マルチコンテナのメタ情報ファイル作成処理を終了しました" },
			{ "MESS_COMM_PROC_METAINFO_3", "シングルコンテナのメタ情報ファイル作成処理を開始します" },
			{ "MESS_COMM_PROC_METAINFO_4", "シングルコンテナのメタ情報ファイル作成処理を終了しました" },

			{ "MESS_COMM_ERR_METAINFO_1", "D00901: メタ情報ファイル作成処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_METAINFO_2", "D00902: メタ情報ファイル作成処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_METAINFO_3", "D00903: 同一名のファイルがすでに存在しています" },
			{ "MESS_COMM_ERR_METAINFO_4", "D00904: メタ情報ファイル出力処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_METAINFO_5", "D00905: コンテナ情報のJSON化処理で変換エラーが発生しました" },
			{ "MESS_COMM_ERR_METAINFO_6", "D00906: コンテナ情報のJSON化処理でエラーが発生しました" },
			{ "MESS_COMM_ERR_METAINFO_7", "D00907: UTF-8のBOM処理でエラーが発生しました" },//
			{ "MESS_COMM_ERR_METAINFO_9", "D00909: 時系列プロパティ情報の解析処理でエラーが発生しました" },//
			{ "MESS_COMM_ERR_METAINFO_11", "D00911: 索引情報の解析処理でエラーが発生しました" },//
			{ "MESS_COMM_ERR_METAINFO_12", "D00912: カラム情報の解析処理でエラーが発生しました" },//
			{ "MESS_COMM_ERR_METAINFO_13", "D00913: カラム種別の解析処理でエラーが発生しました" },//
			{ "MESS_COMM_ERR_METAINFO_14", "D00914: コンテナ情報の解析処理でエラーが発生しました" },//
			{ "MESS_COMM_ERR_METAINFO_15", "D00915: メタ情報ファイルから指定コンテナ読込処理でエラーが発生しました" },//
			{ "MESS_COMM_ERR_METAINFO_16", "D00916: メタ情報ファイルの全コンテナ情報読込処理でエラーが発生しました" },//

			{ "MESS_COMM_ERR_METAINFO_17", "D00917: カラム情報が設定されていません" },//
			{ "MESS_COMM_ERR_METAINFO_18", "D00918: カラム情報のカラム名が設定されていません" },//
			{ "MESS_COMM_ERR_METAINFO_19", "D00919: カラム情報のカラム種別が設定されていません" },//
			{ "MESS_COMM_ERR_METAINFO_20", "D00920: 索引情報のカラム名が設定されていません" },//
			{ "MESS_COMM_ERR_METAINFO_21", "D00921: 索引情報の索引種別が設定されていません" },//
			{ "MESS_COMM_ERR_METAINFO_22", "D00922: 索引情報のカラム名はカラム情報に登録されていません" },//
			{ "MESS_COMM_ERR_METAINFO_24", "D00924: 誤差あり間引き圧縮情報のカラム名はカラム情報に登録されていません" },//
			{ "MESS_COMM_ERR_METAINFO_25", "D00925: コンテナ情報の検査処理でエラーが発生しました" },//

			{ "MESS_COMM_ERR_METAINFO_26", "D00926: JSONファイルからコンテナ情報が正しく読込めませんでした" },//
			{ "MESS_COMM_ERR_METAINFO_27", "D00927: JSONファイルに指定のコンテナ情報がありませんでした" },//
			{ "MESS_COMM_ERR_METAINFO_28", "D00928: JSONファイルからコンテナ情報が正しく読込めませんでした" },//

			{ "MESS_COMM_ERR_METAINFO_29", "D00929: 時系列データに使用できない索引種別が設定されています" },//
			{ "MESS_COMM_ERR_METAINFO_30", "D00930: カラム種別の変換処理でエラーが発生しました" },//
			{ "MESS_COMM_ERR_METAINFO_31", "D00931: ファイルリストの拡張子が一致していません" },
			{ "MESS_COMM_ERR_METAINFO_32", "D00932: ファイルの拡張子が不正です" },
			{ "MESS_COMM_ERR_METAINFO_33", "D00933: Export管理ファイルの書き込みでエラーが発生しました" },
			{ "MESS_COMM_ERR_METAINFO_34", "D00934: メタ情報ファイルでコンテナ名の値が空です" },
			{ "MESS_COMM_ERR_METAINFO_35", "D00935: メタ情報ファイルに同じコンテナ名の情報が含まれています" },
			{ "MESS_COMM_ERR_METAINFO_36", "D00936: 指定されたコンテナがファイルに含まれていません" },
			{ "MESS_COMM_ERR_METAINFO_37", "D00937: メタ情報ファイルの読み込みでエラーが発生しました" },
			{ "MESS_COMM_ERR_METAINFO_38", "D00938: Export管理ファイル(gs_export.json)が存在しません" },
			{ "MESS_COMM_ERR_METAINFO_39", "D00939: V1.5以前のエクスポートデータ、ユーザが作成したファイルまたは長期アーカイブデータをインポートする場合は、オプション-fでメタ情報ファイルを指定して実行してください" },
			{ "MESS_COMM_ERR_METAINFO_40", "D00940: Export管理ファイルのコンテナの(name, file)データが正しく設定されていません" },
			{ "MESS_COMM_ERR_METAINFO_41", "D00941: Export管理ファイルにコンテナ情報が格納されていません" },
			{ "MESS_COMM_ERR_METAINFO_42", "D00942: Export管理ファイルの読み込みでエラーが発生しました" },
			{ "MESS_COMM_ERR_METAINFO_43", "D00943: --filterfileで指定されたファイルが存在しません。" },
			{ "MESS_COMM_ERR_METAINFO_44", "D00944: --filterfileで指定されたファイルの定義に誤りがあります。" },
			{ "MESS_COMM_ERR_METAINFO_45", "D00945: --filterfileで指定されたファイルの読み込みでエラーが発生しました。" },
			{ "MESS_COMM_ERR_METAINFO_46", "D00946: V4より前のエクスポート形式で出力されたパーティショニングコンテナはスキップします。" },
			{ "MESS_COMM_ERR_METAINFO_47", "D00947: (interval_worker_groupまたはinterval_worker_group_position)を指定する場合はインターバルパーティショニングテーブルが必須です。" },

			{ "MESS_COMM_PROC_CMD_1", "コマンド重複チェック処理を開始します" },
			{ "MESS_COMM_PROC_CMD_2", "コマンドラインパラメタ解析処理を開始します" },
			{ "MESS_COMM_PROC_CMD_3", "コマンドラインパラメタ解析処理を開始します" },
			{ "MESS_COMM_PROC_CMD_4", "コマンドラインパラメタ解析処理を終了します" },
			{ "MESS_COMM_PROC_CMD_5", "--container指定で次の文字列を正規表現として認識しました" },//
			{ "MESS_COMM_PROC_CMD_6", "次の文字列はコンテナ名文字列として認識されました" },//

			{ "MESS_COMM_ERR_CMD_1", "D00A01: 実行コマンドが正しく設定されていません" },//
			{ "MESS_COMM_ERR_CMD_2", "D00A02: パラメタを重複して設定できません" },//
			{ "MESS_COMM_ERR_CMD_3", "D00A03: [-h|--help]と--versionは同時に設定できません" },//
			{ "MESS_COMM_ERR_CMD_4", "D00A04: 有効なパラメタが設定されていません" },//
			{ "MESS_COMM_ERR_CMD_5", "D00A05: 無効なパラメタが設定されています" },//
			{ "MESS_COMM_ERR_CMD_6", "D00A06: パラメタ解析でエラーが発生しました" },//
			{ "MESS_COMM_ERR_CMD_7", "D00A07: パラメタ解析処理でエラーが発生しました" },//
			{ "MESS_COMM_ERR_CMD_8", "D00A08: 無効なパラメタが設定されています" },//
			{ "MESS_COMM_ERR_CMD_9", "D00A09: プロパティファイルからの初期化に失敗しました" },//
			{ "MESS_COMM_ERR_CMD_10", "D00A10: サーバー情報の「:」が重複しています" },//
			{ "MESS_COMM_ERR_CMD_11", "D00A11: 不明なホストのためIPアドレスが取得できませんでした" },//
			{ "MESS_COMM_ERR_CMD_12", "D00A12: ポート番号が解析できませんでした" },//
			{ "MESS_COMM_ERR_CMD_13", "D00A13: ポート設定が重複しています" },//
			{ "MESS_COMM_ERR_CMD_14", "D00A14: ユーザーアカウント情報が設定されていません" },//
			{ "MESS_COMM_ERR_CMD_15", "D00A15: \"ユーザー/パスワード\"の記述形式が誤っています。ユーザ名に'/'を含む場合は、--passwordオプションを使用してください。" },//
			{ "MESS_COMM_ERR_CMD_16", "D00A16: [--all],[--db],[--container]または[--containerregex]は同時に設定できません" },//
			{ "MESS_COMM_ERR_CMD_17", "D00A17: 取得数／一括コミット数の設定が正しくありません" },//
			{ "MESS_COMM_ERR_CMD_18", "D00A18: ディレクトリパスの検証でエラーが発生しました" },//
			{ "MESS_COMM_ERR_CMD_19", "D00A19: ディレクトリパスについてセキュリティエラーが発生しました" },//
			{ "MESS_COMM_ERR_CMD_20", "D00A20: 「--out」パラメタにフォルダーパスを設定することはできません" },//
			{ "MESS_COMM_ERR_CMD_21", "D00A21: 有効なコンテナ名が設定されていません" },//
			{ "MESS_COMM_ERR_CMD_22", "D00A22: パラメタ解析でエラーが発生しました" },//
			{ "MESS_COMM_ERR_CMD_23", "D00A23: エクスポートパラメタ解析情報の設定でエラーが発生しました" },//
			{ "MESS_COMM_ERR_CMD_24", "D00A24: インポートパラメタ解析情報の設定でエラーが発生しました" },//
			{ "MESS_COMM_ERR_CMD_25", "D00A25: 実行コマンドが正しく設定されていません" },//
			{ "MESS_COMM_ERR_CMD_26", "D00A26: パラメタを重複して設定できません" },//
			{ "MESS_COMM_ERR_CMD_27", "D00A27: 不正なパラメタが設定されています" },//
			{ "MESS_COMM_ERR_CMD_28", "D00A28: パラメタ重複チェック処理でエラーが発生しました" },//
			{ "MESS_COMM_ERR_CMD_29", "D00A29: コンテナ名の正規表現文字列処理でエラーが発生しました" },//
			{ "MESS_COMM_ERR_CMD_30", "D00A30: コンテナ名チェック処理でエラーが発生しました" },//
			{ "MESS_COMM_ERR_CMD_31",
					"D00A31: Exportでは[-f|--file][--append][--replace]パラメタは設定できません" },//
			{ "MESS_COMM_ERR_CMD_32",
					"D00A32: Importでは[--out][-t|--test]パラメタは設定できません" },//
			{ "MESS_COMM_ERR_CMD_33",
					"D00A33: [--list]パラメタ利用時には、[--directory][--file]以外のパラメタを設定できません" },//
			{ "MESS_COMM_ERR_CMD_34", "D00A34: [--append]と[--replace]は同時に設定できません" },//
			{ "MESS_COMM_ERR_CMD_35", "D00A35: [-f|--file]パラメタではファイル名を必ず設定する必要があります" },//
			{ "MESS_COMM_ERR_CMD_36", "D00A36: 次の文字列は正規表現文字列として利用できません" },//
			{ "MESS_COMM_ERR_CMD_37", "D00A37: パラメタに引数が設定されていません" },//
			{ "MESS_COMM_ERR_CMD_38", "D00A38: ユーザとパスワードの指定は必須です。(-u USER/PASSWORD)" },//
			{ "MESS_COMM_ERR_CMD_39", "D00A39: [--binary]パラメタの値が不正です。" },//
			{ "MESS_COMM_ERR_CMD_40", "D00A40: [--binary]オプションにパラメタは不要です。" },//
			{ "MESS_COMM_ERR_CMD_41", "D00A41: [--all], [--db], [--container], [--containerregex]のうちひとつは必須です" },//
			{ "MESS_COMM_ERR_CMD_42", "D00A42: [--prefixdb]は、[--container]または[--containerregex]オプションと同時に使用してください" },//
			{ "MESS_COMM_ERR_CMD_44", "D00A44: [--list]と[--all][--db][--container][--containerregex]は同時に設定できません" },//
			{ "MESS_COMM_ERR_CMD_45", "D00A45: [--parallel]の値が不正です (2以上16以下の整数を指定してください)" },//
			{ "MESS_COMM_ERR_CMD_46", "D00A46: [--acl]は、[--all]オプション、または[--db]オプションと同時に使用してください" },//
			{ "MESS_COMM_ERR_CMD_47", "D00A47: [--parallel]は、[--binary][--out]と同時に使用してください" },//
			{ "MESS_COMM_ERR_CMD_49", "D00A49: プロパティ[load.output.threadNum]の値が不正です (1以上16以下の整数を指定してください)" },//
			{ "MESS_COMM_ERR_CMD_48", "D00A48: プロパティ[load.input.threadNum]の値が不正です (1以上128以下の整数を指定してください)" },//
			{ "MESS_COMM_ERR_CMD_50", "D00A50: [--parallel]の値が不正です (1以上16以下の整数を指定してください)" },//
			{ "MESS_COMM_ERR_CMD_51", "D00A51: プロパティ[load.input.threadNum],[load.output.threadNum]は両方指定してください" },//
			{ "MESS_COMM_ERR_CMD_54", "D00A54: [--out]オプションに指定できる値は20文字までです。" },
			{ "MESS_COMM_ERR_CMD_55", "D00A55: [--intervals]の値が不正です(yyyyMMdd:yyyyMMdd形式で指定してください)" },//
			{ "MESS_COMM_ERR_CMD_56", "D00A56: [--intervals]の値が不正です(yyyyMMdd（始点）:yyyyMMdd（終点）形式で指定して、始点 < 終点となるように指定してください)" },//
			{ "MESS_COMM_ERR_CMD_57", "D00A57: プロパティ[intervalTimeZone]の値が不正です (タイムゾーン名またはGMT+HH:mm形式で指定してください)" },//
			{ "MESS_COMM_ERR_CMD_58", "D00A58: [--intervals]と[--filterfile]は同時に設定できません" },//
      { "MESS_COMM_ERR_CMD_59", "D00A59: [--progress]の値が不正です。正の整数を指定してください" },//

			{ "MESS_COMM_PROC_PROCINFO_1", "処理されないコンテナ名が検出されました" },//
			{ "MESS_COMM_PROC_PROCINFO_2", "(%d/%d)コンテナ　%s　のインポートに%sしました。（%s）" },//
			{ "MESS_COMM_PROC_PROCINFO_3", "成功" },//
			{ "MESS_COMM_PROC_PROCINFO_4", "失敗" },//
			{ "MESS_COMM_PROC_PROCINFO_5", "対象コンテナ数 : %d ( 成功:%d  失敗:%d )" },//
			{ "MESS_COMM_PROC_PROCINFO_6", "(%d/%d)コンテナ　%s　のエクスポートに%sしました。（%s）" },//
			{ "MESS_COMM_PROC_PROCINFO_7", "対象コンテナ数 : %d ( 成功:%d  失敗:%d  未処理:%d )" },
			{ "MESS_COMM_PROC_PROCINFO_8", "[エラー一覧]" },
			{ "MESS_COMM_PROC_PROCINFO_A", "コンテナ数 : 成功:%d  失敗:%d" },//
			{ "MESS_COMM_PROC_PROCINFO_B", "コンテナ数 : 成功:%d  失敗:%d  処理途中:%d" },//
			{ "MESS_COMM_PROC_PROCINFO_C", "[スキップ一覧]" },
			{ "MESS_COMM_PROC_PROCINFO_D", "対象ビュー数 : %d" },
			{ "MESS_COMM_PROC_PROCINFO_E", "[無効なビュー一覧]" },


			{ "MESS_LOAD_ERR_INPUT_0", "D00E00: 取得処理スレッド内でエラーが発生しました。"},

			{ "MESS_LOAD_ERR_OUTPUT_0", "D00F00: 登録処理スレッド内でエラーが発生しました。"},


			{
					"MESS_EXPORT_HELP_1",
					"gs_export --user username/password [--password password]"
							+ System.getProperty("line.separator")
							+ "--all [--acl]|--db|--container name... [--prefixdb db]|--containerregex regex... [--prefixdb db]"
							+ System.getProperty("line.separator")
							+ "[--directory directorypath]"
							+ System.getProperty("line.separator")
							+ "[--out [filename]][--binary [fileSizeLimit]]"
							+ System.getProperty("line.separator")
							+ /*"[--count maxFetchCount]*/"[--filterfile filename]"
							+ System.getProperty("line.separator")
							+ "[--intervals YYYYMMdd:YYYYMMdd]"
							+ System.getProperty("line.separator")
							+ "[--test][--force]"
							+ System.getProperty("line.separator")
							+ "[--silent][--verbose]"
							+ System.getProperty("line.separator")
			},
			{
					"MESS_EXPORT_HELP_2",
					System.getProperty("line.separator") },
			{
					"MESS_IMPORT_HELP_1",
					"gs_import --user username/password [--password password]"
							+ System.getProperty("line.separator")
							+ "--all [--acl]|--db|--container name... [--prefixdb db]|--containerregex regex... [--prefixdb db]"
							+ System.getProperty("line.separator")
							+ "[--directory directorypath]"
							+ System.getProperty("line.separator")
							+ "[--file filename...]"
							+ System.getProperty("line.separator")
							+ "[--append | --replace][--count maxCommitCount]"
							+ System.getProperty("line.separator")
							+ "[--intervals YYYYMMdd:YYYYMMdd]"
							+ System.getProperty("line.separator")
							+ "[--force]"
							+ System.getProperty("line.separator")
							+ "[--silent][--verbose]"
							+ System.getProperty("line.separator")
							+ System.getProperty("line.separator")
			},
			{
					"MESS_IMPORT_HELP_2",
					"gs_import --user username/password [--password password]"
							+ System.getProperty("line.separator")
							+ "[--append | --replace]"
							+ System.getProperty("line.separator")
							+ "[--force]"
							+ System.getProperty("line.separator")
			},
			{
					"MESS_IMPORT_HELP_3",
					"gs_import- --list"
							+ System.getProperty("line.separator")
							+ "[--directory directorypath]"
							+ System.getProperty("line.separator")
							+ "[--file filename...]"
			},
			{
					"MESS_EXPORT_VERSION",
					" ["+messageResource.VERSION+"]"
							+ System.getProperty("line.separator") },
			{ "MESSAGE_EXPORT_19", " " } };

	/**
	 * 国際化リソース取得メソッド
	 */
	public Object[][] getContents() {
		return contents;
	}
}
