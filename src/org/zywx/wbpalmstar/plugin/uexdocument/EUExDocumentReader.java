package org.zywx.wbpalmstar.plugin.uexdocument;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import java.io.File;

public class EUExDocumentReader extends EUExBase {

	public EUExDocumentReader(Context arg0, EBrowserView arg1) {
		super(arg0, arg1);
	}

	public void openDocumentReader(String[] args) {
		if (args == null || args.length < 1) {
			return;
		}
		String filePath = args[0];
		openDocument(FileUtils.getAbsPath(filePath, mBrwView));
	}

	public void close(String[] args) {

	}

	private FileTask fileTask = null;

	private void openDocument(String filePath) {
		if (fileTask == null) {
			fileTask = new FileTask(filePath);
			fileTask.execute();
		}
	}

	private void openDocumentByThrid(final File file) {
		if (!file.exists()) {
	        ((Activity) mContext).runOnUiThread(new Runnable() {
	            @Override
	            public void run() {
	                Toast.makeText(mContext,
	                        EUExUtil.getString("plugin_uexDocumentReader_file_not_exist"),
	                        Toast.LENGTH_SHORT).show();
	            }
	        });
			return;
		}

		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		String type = DocumentUtils.getMIMEType(file);
		Uri uri;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//			 uri = FileProvider.getUriForFile(mContext, "app的包名.fileProvider", photoFile);
			uri = FileProvider.getUriForFile(
					mContext,
					mContext.getPackageName()+".fileprovider",
					file);
		} else {
			 uri = Uri.fromFile(file);
		}
		intent.setDataAndType(uri, type);
		try {
			intent.setAction(Intent.ACTION_VIEW);
			intent.addCategory("android.intent.category.DEFAULT");
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			Intent chooser = Intent.createChooser(intent, null);
			startActivity(chooser);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}

	}

	class FileTask extends AsyncTask<Void, Void, String> {
		String filePath;
		ProgressDialog dialog;

		public FileTask(String path) {
			filePath = path;
		}

		@Override
		protected void onPreExecute() {
			dialog = FileUtils.showLoadDialog(mContext);
		}

		@Override
		protected String doInBackground(Void... params) {
			return FileUtils.makeFile(mContext, filePath);
		}

		@Override
		protected void onPostExecute(String result) {

			if (dialog != null) {
				dialog.dismiss();
			}

			if (result != null)
			{
				File file = new File(result);
				if (file.exists()) {
				    openDocumentByThrid(file);
				} else {
				    FileUtils.showToast((Activity) mContext,
				            EUExUtil.getString("plugin_uexDocumentReader_file_not_exist"));
				}
			}
			fileTask = null;
		}
	}

	@Override
	protected boolean clean() {

		return false;
	}
}